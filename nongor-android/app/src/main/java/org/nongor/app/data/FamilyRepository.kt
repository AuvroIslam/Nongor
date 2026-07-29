package org.nongor.app.data

import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * One family member's most recent "encounter" — the last time this phone heard theirs over the
 * mesh. There is no server and no GPS network involved: two phones simply have to come within
 * radio range once, and that opportunistic meeting is what gets recorded.
 */
data class FamilyMember(
    val name: String,
    val lastSeenTs: Long,
    val lat: Double? = null,
    val lon: Double? = null,
    val hops: Int = 0,
    /** "drill" for practice data, null for a real encounter. */
    val source: String? = null,
) {
    val isDrill: Boolean get() = source == "drill"
}

/** Persisted log of family encounters, newest per member. */
class FamilyRepository(private val persistFile: File? = null) {

    private val gson = Gson()

    private val _members = MutableStateFlow<List<FamilyMember>>(emptyList())
    val members: StateFlow<List<FamilyMember>> = _members

    init {
        val f = persistFile
        if (f != null && f.exists()) {
            runCatching { gson.fromJson(f.readText(), Persisted::class.java) }.getOrNull()?.let {
                // `members` is nullable because Gson skips Kotlin defaults: a store file
                // written by an older build, or a truncated one, leaves it null.
                _members.value = it.members ?: emptyList()
            }
        }
    }

    private data class Persisted(val members: List<FamilyMember>? = null)

    /**
     * Upsert by name, always keeping the most recent sighting.
     *
     * The first real encounter clears any practice data, for the same reason the SOS store
     * does: nobody should ever go looking for a relative who only ever existed in a drill.
     */
    fun record(m: FamilyMember) {
        if (!m.isDrill && _members.value.any { it.isDrill }) {
            _members.value = _members.value.filterNot { it.isDrill }
        }
        val existing = _members.value.firstOrNull { it.name.equals(m.name, ignoreCase = true) }
        if (existing != null && existing.lastSeenTs >= m.lastSeenTs) return
        _members.value = (_members.value.filterNot { it.name.equals(m.name, ignoreCase = true) } + m)
            .sortedByDescending { it.lastSeenTs }
        persist()
    }

    /**
     * Seed practice encounters so the drill can show what a reunion actually looks like on a
     * single phone.
     *
     * Positions are offset from wherever the user actually is, so the radar shows real
     * bearings rather than a canned picture. One member is deliberately seeded with no
     * position at all — that is the common case in the field, and the drill should show the
     * honest fallback rather than an idealised screen where everyone has GPS.
     */
    fun seedDrill(around: Pair<Double, Double>?) {
        if (_members.value.any { !it.isDrill }) return   // never displace real encounters
        if (_members.value.any { it.isDrill }) return    // already seeded
        val now = System.currentTimeMillis()

        fun offset(bearingDeg: Double, metres: Double): Pair<Double?, Double?> {
            val (lat, lon) = around ?: return null to null
            val rad = Math.toRadians(bearingDeg)
            val dLat = metres * kotlin.math.cos(rad) / 111_320.0
            val dLon = metres * kotlin.math.sin(rad) /
                (111_320.0 * kotlin.math.cos(Math.toRadians(lat)))
            return (lat + dLat) to (lon + dLon)
        }

        val (aLat, aLon) = offset(45.0, 120.0)
        val (bLat, bLon) = offset(215.0, 380.0)
        val (cLat, cLon) = offset(95.0, 1400.0)

        listOf(
            FamilyMember("Amina", now - 2 * 60_000, aLat, aLon, hops = 1, source = "drill"),
            FamilyMember("Rahim", now - 24 * 60_000, bLat, bLon, hops = 2, source = "drill"),
            FamilyMember("Nani", now - 95 * 60_000, cLat, cLon, hops = 3, source = "drill"),
            // No position: heard over the mesh, never close enough to fix.
            FamilyMember("Shefali", now - 8 * 60_000, null, null, hops = 2, source = "drill"),
        ).forEach { m ->
            _members.value = _members.value + m
        }
        _members.value = _members.value.sortedByDescending { it.lastSeenTs }
        persist()
    }

    fun clear() {
        _members.value = emptyList(); persist()
    }

    private fun persist() {
        val f = persistFile ?: return
        runCatching { f.writeText(gson.toJson(Persisted(_members.value))) }
    }
}
