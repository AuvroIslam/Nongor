package org.nongor.app.ui.family

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import org.nongor.app.NongorApplication
import org.nongor.app.core.Gis
import org.nongor.app.data.FamilyMember
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/** A family member plus how far/which way they were last heard from. */
data class SeenMember(
    val member: FamilyMember,
    val distanceM: Int?,       // null when we don't have both positions
    val direction: String?,    // 8-point compass label, null when unknown
    val minutesAgo: Long,
    /**
     * True bearing in degrees, 0 = north. Kept alongside the rounded [direction] label
     * because the radar places people by angle: snapping to the nearest of eight points
     * would move someone by up to 22 degrees, which at 500 m is most of a village.
     */
    val bearingDeg: Float? = null,
) {
    /** Everything needed to draw this person on the radar. */
    val hasFix: Boolean get() = distanceM != null && bearingDeg != null
}

/** What a blip on the radar represents. Each layer answers a different question. */
enum class BlipKind { FAMILY, SOS, VOLUNTEER }

/**
 * One thing plotted on the radar.
 *
 * Family, people calling for help, and people offering it all reduce to the same three
 * facts - who, how far, which way - so they share one shape and are told apart by colour.
 */
data class RadarBlip(
    val name: String,
    val kind: BlipKind,
    val distanceM: Int?,
    val bearingDeg: Float?,
    val minutesAgo: Long,
    val drill: Boolean = false,
) {
    val hasFix: Boolean get() = distanceM != null && bearingDeg != null
}

data class FamilyUiState(
    val familyCode: String = "",
    val myName: String = "",
    val configured: Boolean = false,
    val started: Boolean = false,
    val peers: Int = 0,
    val members: List<SeenMember> = emptyList(),
    val togetherCount: Int = 0,      // members last seen close together
    val lastBeaconTs: Long = 0L,
    /** People calling for help, from signed SOS reports that carried a position. */
    val sosBlips: List<RadarBlip> = emptyList(),
    /** People offering help, from "rescue available" reports on the board. */
    val volunteerBlips: List<RadarBlip> = emptyList(),
)

/**
 * AI Family Reunion. Every phone quietly announces itself to its own family over the mesh — a
 * hashed family tag plus an AES-sealed name, so only relatives can recognise it. When two phones
 * come within radio range even once, that opportunistic encounter is logged, and this screen turns
 * it into "last seen ~400 m east, 12 minutes ago". No server, no cell network, no GPS network
 * required — position is only used to describe an encounter that already happened.
 */
class FamilyViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as NongorApplication
    private val hub = app.meshHub
    private var acquired = false
    private var beacon: Job? = null

    private val _ui = MutableStateFlow(FamilyUiState())
    val ui: StateFlow<FamilyUiState> = _ui

    init {
        viewModelScope.launch { app.prefs.familyCode.collect { c ->
            _ui.update { it.copy(familyCode = c, configured = c.isNotBlank()) } } }
        viewModelScope.launch { app.prefs.memberName.collect { n -> _ui.update { it.copy(myName = n) } } }
        viewModelScope.launch { hub.started.collect { v -> _ui.update { it.copy(started = v) } } }
        viewModelScope.launch { hub.peers.collect { v -> _ui.update { it.copy(peers = v) } } }
        viewModelScope.launch { hub.familyMembers.collect { refresh(it) } }
        viewModelScope.launch { app.sosRepository.entries.collect { refreshOthers() } }
        viewModelScope.launch { app.communityRepository.entries.collect { refreshOthers() } }
    }

    fun saveFamily(code: String, name: String) {
        app.prefs.setFamily(code, name)
        if (code.isNotBlank() && name.isNotBlank()) startBeacon()
    }

    /**
     * Wipe what is plotted, without wiping what matters.
     *
     * Clears the family encounters this phone has logged and any leftover drill cases, which is
     * what actually clutters the radar. Real SOS reports stay: they feed the triage queue and
     * the briefing, and a tidy-up button is not a place to lose them.
     */
    fun clearRadar() {
        app.familyRepository.clear()
        app.sosRepository.clearDrills()
        refreshOthers()
    }

    fun forgetFamily() {
        app.prefs.clearFamily()
        app.familyRepository.clear()
        beacon?.cancel(); beacon = null
    }

    fun enter() {
        if (!acquired) { acquired = true; hub.acquire() }
        startBeacon()
    }

    fun leave() {
        beacon?.cancel(); beacon = null
        if (acquired) { acquired = false; hub.release() }
    }

    /** Announce ourselves periodically so relatives who come into range will hear us. */
    private fun startBeacon() {
        if (beacon != null) return
        if (app.prefs.familyCode.value.isBlank() || app.prefs.memberName.value.isBlank()) return
        beacon = viewModelScope.launch {
            while (isActive) {
                runCatching { hub.sendPresence() }
                _ui.update { it.copy(lastBeaconTs = System.currentTimeMillis()) }
                delay(BEACON_INTERVAL_MS)
            }
        }
    }

    /** Turn raw encounters into "how far, which way, how long ago" — all computed in code. */
    private fun refresh(raw: List<FamilyMember>) {
        viewModelScope.launch {
            val me = runCatching { hub.myLocation() }.getOrNull()
            val now = System.currentTimeMillis()
            val seen = raw.map { m ->
                val d = if (me != null && m.lat != null && m.lon != null)
                    Gis.haversineM(me.first, me.second, m.lat, m.lon).toInt() else null
                val deg = if (me != null && m.lat != null && m.lon != null)
                    bearingDegrees(me.first, me.second, m.lat, m.lon) else null
                SeenMember(
                    member = m,
                    distanceM = d,
                    direction = deg?.let { POINTS[(((it + 22.5) / 45).toInt()) % 8] },
                    minutesAgo = (now - m.lastSeenTs) / 60_000,
                    bearingDeg = deg?.toFloat(),
                )
            }
            // "Three phones from your family were together": members last seen close in space+time.
            val together = seen.count { a ->
                a.member.lat != null && seen.any { b ->
                    b !== a && b.member.lat != null && b.member.lon != null && a.member.lon != null &&
                        Gis.haversineM(a.member.lat, a.member.lon!!, b.member.lat!!, b.member.lon) < TOGETHER_M &&
                        kotlin.math.abs(a.member.lastSeenTs - b.member.lastSeenTs) < TOGETHER_MS
                }
            }
            _ui.update { it.copy(members = seen, togetherCount = together) }
        }
    }

    /**
     * Plot everyone else: who is calling for help, and who is offering it.
     *
     * Both come from stores the mesh already fills, so nothing new goes on the air. Anything
     * without a position is dropped rather than placed at a guess - a blip pointing the wrong
     * way is worse than no blip, because someone will walk towards it.
     */
    private fun refreshOthers() {
        viewModelScope.launch {
            val me = runCatching { hub.myLocation() }.getOrNull() ?: return@launch
            val now = System.currentTimeMillis()

            val sos = app.sosRepository.entries.value.mapNotNull { entry ->
                val lat = entry.report.lat ?: return@mapNotNull null
                val lon = entry.report.lon ?: return@mapNotNull null
                RadarBlip(
                    name = entry.triage.priority.uppercase(),
                    kind = BlipKind.SOS,
                    distanceM = Gis.haversineM(me.first, me.second, lat, lon).toInt(),
                    bearingDeg = bearingDegrees(me.first, me.second, lat, lon).toFloat(),
                    minutesAgo = ((now - entry.ts) / 60_000L).coerceAtLeast(0),
                    drill = entry.source == "drill",
                )
            }

            val volunteers = app.communityRepository.entries.value
                .filter { it.kind == "rescue_here" }
                .mapNotNull { r ->
                    val lat = r.lat ?: return@mapNotNull null
                    val lon = r.lon ?: return@mapNotNull null
                    RadarBlip(
                        name = if (r.mine) "You" else r.sender,
                        kind = BlipKind.VOLUNTEER,
                        distanceM = Gis.haversineM(me.first, me.second, lat, lon).toInt(),
                        bearingDeg = bearingDegrees(me.first, me.second, lat, lon).toFloat(),
                        minutesAgo = ((now - r.ts) / 60_000L).coerceAtLeast(0),
                    )
                }

            _ui.update { it.copy(sosBlips = sos, volunteerBlips = volunteers) }
        }
    }

    /** True bearing from us to them, 0 = north, clockwise. */
    private fun bearingDegrees(fromLat: Double, fromLon: Double, toLat: Double, toLon: Double): Double {
        val dLon = Math.toRadians(toLon - fromLon)
        val p1 = Math.toRadians(fromLat); val p2 = Math.toRadians(toLat)
        val y = sin(dLon) * cos(p2)
        val x = cos(p1) * sin(p2) - sin(p1) * cos(p2) * cos(dLon)
        return (Math.toDegrees(atan2(y, x)) + 360) % 360
    }

    override fun onCleared() { leave() }

    companion object {
        private const val BEACON_INTERVAL_MS = 20_000L
        private const val TOGETHER_M = 120.0
        private const val TOGETHER_MS = 10 * 60_000L
        private val POINTS = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    }
}
