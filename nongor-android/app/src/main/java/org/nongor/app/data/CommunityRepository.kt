package org.nongor.app.data

import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.util.UUID

/**
 * One community situation report — "every phone is a sensor". Neighbours tag what they see (a road
 * flooded, a bridge down, a pharmacy open, supplies available) and it spreads over the mesh, so the
 * whole area builds a shared picture with no internet. Each report is tied to a district so one
 * area's reports never flood another's board.
 */
data class CommunityReport(
    val id: String = UUID.randomUUID().toString(),
    val kind: String,                 // see CommunityKinds.ALL
    val note: String,
    val districtEn: String,
    val lat: Double? = null,
    val lon: Double? = null,
    val sender: String,
    val verified: Boolean = true,     // Ed25519-verified over the mesh
    val mine: Boolean = false,
    val ts: Long = System.currentTimeMillis(),
)

/**
 * The fixed set of report types, with bilingual labels — kept small so it works for
 * low-literacy use.
 *
 * [icon] names a drawn vector rather than an emoji. Emoji look different on every phone,
 * render as a blank box on some cheap ones, and are read out unhelpfully by screen readers —
 * none of which you want on the tag that says a bridge is down.
 */
object CommunityKinds {
    data class Kind(
        val id: String,
        val en: String,
        val bn: String,
        val icon: String,
        val danger: Boolean,
    )

    val ALL = listOf(
        Kind("road_flooded", "Road flooded", "রাস্তা প্লাবিত", "flood", true),
        Kind("bridge_down", "Bridge / road blocked", "সেতু / রাস্তা বন্ধ", "blocked", true),
        Kind("shelter_full", "Shelter full", "আশ্রয় পূর্ণ", "shelter_full", true),
        Kind("danger", "Danger here", "এখানে বিপদ", "danger", true),
        Kind("supplies", "Food / water / medicine", "খাবার / পানি / ওষুধ", "supplies", false),
        Kind("pharmacy_open", "Pharmacy / shop open", "ফার্মেসি / দোকান খোলা", "pharmacy", false),
        Kind("safe_route", "Safe route / dry road", "নিরাপদ পথ / শুকনো রাস্তা", "safe_route", false),
        Kind("rescue_here", "Rescue available", "উদ্ধার সহায়তা আছে", "rescue", false),
    )

    fun byId(id: String): Kind = ALL.firstOrNull { it.id == id } ?: ALL.first()
}

/**
 * Live store of community reports this device has posted or received over the mesh. Persisted so a
 * background-kill mid-incident doesn't lose the shared picture. Unverified (forged) reports are
 * quarantined, never merged into the trusted board.
 */
class CommunityRepository(private val persistFile: File? = null) {

    private val gson = Gson()

    private val _entries = MutableStateFlow<List<CommunityReport>>(emptyList())
    val entries: StateFlow<List<CommunityReport>> = _entries

    private val _quarantine = MutableStateFlow<List<CommunityReport>>(emptyList())
    val quarantine: StateFlow<List<CommunityReport>> = _quarantine

    /**
     * Who has confirmed or disputed each report, keyed by report id.
     *
     * Sets of voter ids rather than counters, because a vote relayed over two hops arrives twice
     * and a report's credibility must not be inflatable by bouncing it around the mesh.
     */
    private val _confirms = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val confirms: StateFlow<Map<String, Set<String>>> = _confirms

    private val _disputes = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val disputes: StateFlow<Map<String, Set<String>>> = _disputes

    /** This phone's own vote per report, so the UI can show what you already said. */
    private val _myVotes = MutableStateFlow<Map<String, String>>(emptyMap())
    val myVotes: StateFlow<Map<String, String>> = _myVotes

    /**
     * Record a vote. [up] true = "I can see this too", false = "this is not what I see".
     *
     * One vote per voter per report, and a voter can change their mind — the later vote wins
     * rather than counting twice on opposite sides.
     */
    fun recordVote(reportId: String, voter: String, up: Boolean): Boolean {
        if (reportId.isBlank() || voter.isBlank()) return false
        val c = _confirms.value[reportId].orEmpty()
        val d = _disputes.value[reportId].orEmpty()
        if ((up && voter in c) || (!up && voter in d)) return false
        _confirms.value = _confirms.value + (reportId to if (up) c + voter else c - voter)
        _disputes.value = _disputes.value + (reportId to if (up) d - voter else d + voter)
        persist()
        return true
    }

    /** Remember that this phone voted, so the buttons can show the current state. */
    fun setMyVote(reportId: String, up: Boolean) {
        _myVotes.value = _myVotes.value + (reportId to if (up) "up" else "down")
        persist()
    }

    fun confirmCount(reportId: String): Int = _confirms.value[reportId].orEmpty().size
    fun disputeCount(reportId: String): Int = _disputes.value[reportId].orEmpty().size

    init {
        val f = persistFile
        if (f != null && f.exists()) {
            runCatching { gson.fromJson(f.readText(), Persisted::class.java) }.getOrNull()?.let {
                _entries.value = it.entries
                _confirms.value = it.confirms?.mapValues { (_, v) -> v.toSet() } ?: emptyMap()
                _disputes.value = it.disputes?.mapValues { (_, v) -> v.toSet() } ?: emptyMap()
                _myVotes.value = it.myVotes ?: emptyMap()
                _quarantine.value = it.quarantine
            }
        }
    }

    private data class Persisted(
        val entries: List<CommunityReport> = emptyList(),
        val quarantine: List<CommunityReport> = emptyList(),
        // Nullable: a store written before voting existed leaves these null, and Gson does not
        // run Kotlin constructors, so a non-null default would arrive as null and crash.
        val confirms: Map<String, List<String>>? = null,
        val disputes: Map<String, List<String>>? = null,
        val myVotes: Map<String, String>? = null,
    )


    fun add(r: CommunityReport) {
        if (_entries.value.any { it.id == r.id }) return          // dedup relayed copies
        _entries.value = (_entries.value + r).sortedByDescending { it.ts }
        persist()
    }

    fun addQuarantined(r: CommunityReport) {
        if (_quarantine.value.any { it.id == r.id }) return
        _quarantine.value = _quarantine.value + r
        persist()
    }

    /** Reports for one district only — this is how one area's board stays separate from another's. */
    fun forDistrict(districtEn: String): List<CommunityReport> =
        _entries.value.filter { it.districtEn.equals(districtEn, ignoreCase = true) }

    fun clear() {
        _entries.value = emptyList(); _quarantine.value = emptyList()
        _confirms.value = emptyMap(); _disputes.value = emptyMap(); _myVotes.value = emptyMap()
        persist()
    }

    private fun persist() {
        val f = persistFile ?: return
        runCatching {
            f.writeText(
                gson.toJson(
                    Persisted(
                        _entries.value,
                        _quarantine.value,
                        _confirms.value.mapValues { (_, v) -> v.toList() },
                        _disputes.value.mapValues { (_, v) -> v.toList() },
                        _myVotes.value,
                    ),
                ),
            )
        }
    }
}
