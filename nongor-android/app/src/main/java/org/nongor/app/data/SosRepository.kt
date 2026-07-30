package org.nongor.app.data

import org.nongor.app.core.SosReport
import org.nongor.app.core.TriageResult
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/** One real field report the app has actually handled (triaged locally or relayed over mesh). */
data class SosEntry(
    val report: SosReport,
    val triage: TriageResult,
    val source: String,               // "triage" | "mesh_sent" | "mesh_recv" | "drill"
    val verified: Boolean = true,
    val hops: Int = 0,
    val ts: Long = System.currentTimeMillis(),
)

private data class PersistedSos(
    val entries: List<SosEntry> = emptyList(),
    val quarantine: List<SosEntry> = emptyList(),
    // Nullable and defaulted: Gson populates fields reflectively without running the Kotlin
    // constructor, so a file written before this field existed leaves it null, not empty.
    val seenBy: Map<String, List<String>>? = null,
    val acked: List<String>? = null,
)

/**
 * Live, shared store of the SOS reports this device has genuinely handled. Triage adds cases it
 * assesses; Mesh adds SOS it sends or receives. The Coordinator Summary reads only from here, so
 * the briefing reflects the real situation on this device rather than any bundled sample data.
 *
 * Mesh envelopes that fail signature verification never land in [entries] — a forged or corrupted
 * report must not be able to distort rescue priority. They go to [quarantine] instead, visible to
 * an operator but excluded from every aggregate count and from the coordinator briefing.
 *
 * The onboarding flood-drill seeds sample entries (source == "drill") so first-time screens have
 * something to show. Those must never linger into a real emergency: the moment any genuine report
 * arrives, [add] purges leftover drill entries first, so a real briefing is never mixed with
 * practice data — and drill entries are never persisted to disk.
 *
 * Real reports are persisted (when [persistFile] is set) so a background-kill mid-incident doesn't
 * silently lose the coordinator's collected cases. [persistFile] is null in unit tests, keeping the
 * store's logic pure and file-free there.
 */
class SosRepository(private val persistFile: File? = null) {

    private val gson = Gson()

    private val _entries = MutableStateFlow<List<SosEntry>>(emptyList())
    val entries: StateFlow<List<SosEntry>> = _entries

    private val _quarantine = MutableStateFlow<List<SosEntry>>(emptyList())
    val quarantine: StateFlow<List<SosEntry>> = _quarantine

    /**
     * Who has confirmed seeing each SOS we sent, keyed by the message id.
     *
     * This is the read receipt. It says a copy of the message reached a phone and was put in
     * front of a person - nothing more. It is deliberately NOT evidence that anyone is coming,
     * and the UI that renders it has to say so, because a person on a roof reading "seen by 3"
     * will otherwise stop looking for another way out.
     */
    private val _seenBy = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val seenBy: StateFlow<Map<String, Set<String>>> = _seenBy

    /**
     * SOS ids this device has already acknowledged.
     *
     * Persisted, so a restart mid-incident does not re-acknowledge everything still in the
     * store and hand the sender a pile of duplicate receipts from one phone.
     */
    private val _acked = MutableStateFlow<Set<String>>(emptySet())

    private var lastBriefedCount = 0

    init {
        val f = persistFile
        if (f != null && f.exists()) {
            runCatching {
                gson.fromJson(f.readText(), PersistedSos::class.java)
            }.getOrNull()?.let { saved ->
                _entries.value = saved.entries
                _quarantine.value = saved.quarantine
                _seenBy.value = saved.seenBy?.mapValues { (_, v) -> v.toSet() } ?: emptyMap()
                _acked.value = saved.acked?.toSet() ?: emptySet()
            }
        }
    }

    fun add(entry: SosEntry) {
        val base = if (entry.source != "drill") _entries.value.filter { it.source != "drill" }
        else _entries.value
        _entries.value = base + entry
        persist()
    }

    /** Holds a report that failed mesh signature verification — kept for review, not trusted. */
    fun addQuarantined(entry: SosEntry) {
        _quarantine.value = _quarantine.value + entry
        persist()
    }

    /**
     * Drop practice data only.
     *
     * Real reports are what the triage queue and the coordinator briefing are counted from, so
     * "tidy up the radar" must never be a way to quietly delete somebody's call for help.
     */
    fun clearDrills() {
        val before = _entries.value.size
        _entries.value = _entries.value.filterNot { it.source == "drill" }
        if (_entries.value.size != before) persist()
    }

    fun clear() {
        _entries.value = emptyList()
        _quarantine.value = emptyList()
        _seenBy.value = emptyMap()
        _acked.value = emptySet()
        lastBriefedCount = 0
        persist()
    }

    /**
     * Record that [by] has seen the SOS with id [msgId].
     *
     * A set, not a counter: the same phone re-broadcasting its acknowledgement after a relay
     * hop must not inflate the number. Returns true only when this is genuinely new, so the
     * caller can decide whether anything on screen needs to change.
     */
    fun recordSeen(msgId: String, by: String): Boolean {
        if (msgId.isBlank() || by.isBlank()) return false
        val current = _seenBy.value[msgId].orEmpty()
        if (by in current) return false
        _seenBy.value = _seenBy.value + (msgId to (current + by))
        persist()
        return true
    }

    /** Everyone known to have seen this SOS. */
    fun seenBy(msgId: String): Set<String> = _seenBy.value[msgId].orEmpty()

    /**
     * Claim the right to acknowledge [msgId], exactly once on this device.
     *
     * Returns true the first time and false forever after, so the ack is sent on the first
     * viewing and never again - including across restarts.
     */
    fun claimAck(msgId: String): Boolean {
        if (msgId.isBlank() || msgId in _acked.value) return false
        _acked.value = _acked.value + msgId
        persist()
        return true
    }

    fun hasAcked(msgId: String): Boolean = msgId in _acked.value

    /** How many reports arrived since the last briefing was generated. */
    fun newSince(): Int = (_entries.value.size - lastBriefedCount).coerceAtLeast(0)

    fun markBriefed() {
        lastBriefedCount = _entries.value.size
    }

    fun reports(): List<SosReport> = _entries.value.map { it.report }
    fun triageResults(): List<TriageResult> = _entries.value.map { it.triage }

    private fun persist() {
        val f = persistFile ?: return
        runCatching {
            // Drill data is a practice scaffold — never let it survive a restart as "real" data.
            val real = _entries.value.filter { it.source != "drill" }
            f.writeText(
                gson.toJson(
                    PersistedSos(
                        real,
                        _quarantine.value,
                        _seenBy.value.mapValues { (_, v) -> v.toList() },
                        _acked.value.toList(),
                    ),
                ),
            )
        }
    }
}
