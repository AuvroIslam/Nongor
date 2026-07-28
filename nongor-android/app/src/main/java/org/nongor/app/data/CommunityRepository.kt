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

/** The fixed set of report types, with bilingual labels — kept small so it works for low-literacy use. */
object CommunityKinds {
    data class Kind(val id: String, val en: String, val bn: String, val emoji: String, val danger: Boolean)

    val ALL = listOf(
        Kind("road_flooded", "Road flooded", "রাস্তা প্লাবিত", "🚧", true),
        Kind("bridge_down", "Bridge / road blocked", "সেতু / রাস্তা বন্ধ", "⛔", true),
        Kind("shelter_full", "Shelter full", "আশ্রয় পূর্ণ", "🏠", true),
        Kind("danger", "Danger here", "এখানে বিপদ", "⚠️", true),
        Kind("supplies", "Food / water / medicine", "খাবার / পানি / ওষুধ", "📦", false),
        Kind("pharmacy_open", "Pharmacy / shop open", "ফার্মেসি / দোকান খোলা", "💊", false),
        Kind("safe_route", "Safe route / dry road", "নিরাপদ পথ / শুকনো রাস্তা", "✅", false),
        Kind("rescue_here", "Rescue available", "উদ্ধার সহায়তা আছে", "🚤", false),
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

    init {
        val f = persistFile
        if (f != null && f.exists()) {
            runCatching { gson.fromJson(f.readText(), Persisted::class.java) }.getOrNull()?.let {
                _entries.value = it.entries
                _quarantine.value = it.quarantine
            }
        }
    }

    private data class Persisted(
        val entries: List<CommunityReport> = emptyList(),
        val quarantine: List<CommunityReport> = emptyList(),
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
        _entries.value = emptyList(); _quarantine.value = emptyList(); persist()
    }

    private fun persist() {
        val f = persistFile ?: return
        runCatching { f.writeText(gson.toJson(Persisted(_entries.value, _quarantine.value))) }
    }
}
