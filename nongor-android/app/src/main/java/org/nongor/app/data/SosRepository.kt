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

    private var lastBriefedCount = 0

    init {
        val f = persistFile
        if (f != null && f.exists()) {
            runCatching {
                gson.fromJson(f.readText(), PersistedSos::class.java)
            }.getOrNull()?.let { saved ->
                _entries.value = saved.entries
                _quarantine.value = saved.quarantine
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

    fun clear() {
        _entries.value = emptyList()
        _quarantine.value = emptyList()
        lastBriefedCount = 0
        persist()
    }

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
            f.writeText(gson.toJson(PersistedSos(real, _quarantine.value)))
        }
    }
}
