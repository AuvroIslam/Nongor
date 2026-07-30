package org.nongor.app.ui.gis

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import org.nongor.app.NongorApplication
import org.nongor.app.core.Gis
import org.nongor.app.core.Prompts
import org.nongor.app.core.Safety
import org.nongor.app.core.Shelter
import org.nongor.app.data.BdGeo
import org.nongor.app.data.CommunityKinds
import org.nongor.app.data.PublicShelterHit
import org.nongor.app.data.PublicShelters
import org.nongor.app.data.RegionAssets
import org.nongor.app.data.Regions
import org.nongor.app.data.download.HfDownloadRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class GisUiState(
    val userLat: Double = 0.0,
    val userLon: Double = 0.0,
    val usingGps: Boolean = false,
    val hasLocation: Boolean = false,        // true once we have a real point (GPS or a dropped pin)
    val manualPin: Boolean = false,          // the point came from tapping the map
    val locating: Boolean = false,
    val districtEn: String = "",
    val districtBn: String = "",
    val detailed: Boolean = false,           // true = full pack with flood-avoiding routing
    val shelters: List<Shelter> = emptyList(),
    val floodPolys: List<List<DoubleArray>> = emptyList(),
    val graph: Gis.PedGraph? = null,
    val ranked: List<Gis.RankedShelter> = emptyList(),
    val nearbyPublic: List<PublicShelterHit> = emptyList(),
    val route: Gis.Route? = null,
    val naiveCrossesFlood: Boolean = false,
    /** True when we asked the GPS and it could not answer — indoors, no signal, radio off. */
    val gpsFailed: Boolean = false,
    val computed: Boolean = false,
    val selectedShelterId: String? = null,   // detailed-region selection
    val selectedPublicIdx: Int? = null,      // nationwide selection
    val overviewRegion: String = "",         // region shown in overview / chosen in the area picker
    // ---- Map Assistant ----
    val assistantBusy: Boolean = false,
    val assistantAnswer: String? = null,
)

/** Everything computed from one point, without touching UI state — reused by the map and the assistant. */
private data class Computed(
    val detailed: Boolean, val packId: String, val districtEn: String, val districtBn: String,
    val shelters: List<Shelter>, val flood: List<List<DoubleArray>>, val graph: Gis.PedGraph?,
    val ranked: List<Gis.RankedShelter>, val nearbyPublic: List<PublicShelterHit>,
    val route: Gis.Route?, val naive: Boolean,
)

class GisViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as NongorApplication
    private val ctx = application

    private val _ui = MutableStateFlow(GisUiState())
    val ui: StateFlow<GisUiState> = _ui

    init {
        viewModelScope.launch {
            // Draw *something* immediately so the screen is never blank while GPS warms up.
            loadOverview(app.prefs.activeRegion.value)

            // Then correct it to wherever the user actually is. Opening on a stored region was
            // quietly wrong for most of the country: someone standing in Khulna was shown
            // Chattogram's shelters with no hint that the map was not about them. The overview
            // is a placeholder, not an answer, so it should be replaced the moment we can.
            if (app.locationProvider.hasPermission()) {
                _ui.value = _ui.value.copy(locating = true)
                val here = runCatching { app.locationProvider.current() }.getOrNull()
                if (here != null && !_ui.value.hasLocation) {
                    located(here.first, here.second, usedGps = true, manual = false)
                } else {
                    _ui.value = _ui.value.copy(locating = false, gpsFailed = here == null)
                }
            }
        }
        // Warm the model so the Map Assistant can use Gemma even if this is the first screen opened.
        viewModelScope.launch {
            if (!app.engineHolder.isReady()) {
                val model = HfDownloadRepository.modelFile(getApplication())
                if (model.exists()) app.engineHolder.loadModel(model)
            }
        }
    }

    /** The 3 detailed regions offered in the area picker. */
    fun regions() = Regions.ALL

    /** Switch the area shown in overview (used by the map's area dropdown). */
    fun setRegion(regionId: String) {
        viewModelScope.launch { loadOverview(regionId) }
    }

    /** The button: use GPS if available; otherwise keep the honest overview and prompt for a pin. */
    fun findNearestShelter() {
        if (_ui.value.locating) return
        viewModelScope.launch {
            _ui.value = _ui.value.copy(locating = true)
            val loc = app.locationProvider.current()
            if (loc != null) located(loc.first, loc.second, usedGps = true, manual = false)
            else _ui.value = _ui.value.copy(locating = false, gpsFailed = true)
        }
    }

    /** User tapped the map to place their own location (works with GPS off). */
    fun setManualLocation(lat: Double, lon: Double) {
        if (_ui.value.locating) return
        viewModelScope.launch {
            _ui.value = _ui.value.copy(locating = true)
            located(lat, lon, usedGps = false, manual = true)
        }
    }

    /** Region overview: all shelters on the map, no "you", no route, until a location is set. */
    private suspend fun loadOverview(regionId: String) {
        val region = Regions.byId(regionId)
        val shelters = runCatching { RegionAssets.loadShelters(ctx, regionId) }.getOrDefault(emptyList())
        val flood = runCatching { RegionAssets.loadFloodPolys(ctx, regionId) }.getOrDefault(emptyList())
        val graph = runCatching { RegionAssets.loadGraph(ctx, regionId) }.getOrNull()
        _ui.value = _ui.value.copy(
            userLat = region.centerLat, userLon = region.centerLon,   // framing centre only, not "you"
            usingGps = false, hasLocation = false, manualPin = false, locating = false, gpsFailed = false,
            districtEn = region.nameEn, districtBn = region.nameBn, detailed = true,
            shelters = shelters, floodPolys = flood, graph = graph,
            ranked = emptyList(), nearbyPublic = emptyList(),
            route = null, naiveCrossesFlood = false, computed = true,
            selectedShelterId = null, selectedPublicIdx = null, overviewRegion = regionId,
        )
    }

    /** We have a real point (GPS or pin): compute nearest shelter + route and show "you" on the map. */
    private suspend fun located(lat: Double, lon: Double, usedGps: Boolean, manual: Boolean) {
        val c = computeCore(lat, lon)
        _ui.value = _ui.value.copy(
            userLat = lat, userLon = lon, usingGps = usedGps, hasLocation = true,
            manualPin = manual, locating = false, gpsFailed = false,
            districtEn = c.districtEn, districtBn = c.districtBn, detailed = c.detailed,
            shelters = c.shelters, floodPolys = c.flood, graph = c.graph,
            ranked = c.ranked, nearbyPublic = c.nearbyPublic,
            route = c.route, naiveCrossesFlood = c.naive, computed = true,
            selectedShelterId = c.ranked.firstOrNull()?.shelterId,
            selectedPublicIdx = if (c.nearbyPublic.isNotEmpty()) 0 else null,
            overviewRegion = if (c.detailed) c.packId else _ui.value.overviewRegion,
        )
        // Remember where they were, so the next launch frames the right part of the country
        // even before GPS answers.
        if (c.detailed) app.prefs.setActiveRegion(c.packId)
    }

    /** Pure computation from a point — no UI mutation. */
    private suspend fun computeCore(lat: Double, lon: Double): Computed {
        val district = BdGeo.nearestDistrict(ctx, lat, lon)
        val (pack, distToPack) = BdGeo.nearestPack(lat, lon)
        val detailed = distToPack <= DETAIL_RADIUS_M
        return if (detailed) {
            val shelters = RegionAssets.loadShelters(ctx, pack.id)
            val flood = RegionAssets.loadFloodPolys(ctx, pack.id)
            val graph = RegionAssets.loadGraph(ctx, pack.id)
            val ranked = Gis.findNearestShelter(lat, lon, shelters, emptyList())
            val top = ranked.firstOrNull()
            val route = top?.let { Gis.safeRoute(lat, lon, it.lat, it.lon, graph, flood) }
            val naive = top?.let {
                Gis.segmentCrossesFlood(doubleArrayOf(lat, lon), doubleArrayOf(it.lat, it.lon), flood)
            } ?: false
            Computed(true, pack.id, district.name, district.bn, shelters, flood, graph, ranked,
                emptyList(), route, naive)
        } else {
            val hits = withContext(Dispatchers.Default) { PublicShelters.nearest(ctx, lat, lon, 6) }
            Computed(false, pack.id, district.name, district.bn, emptyList(), emptyList(), null,
                emptyList(), hits, null, false)
        }
    }

    /** Detailed region: pick a different shelter → recompute the flood-avoiding route from you. */
    fun selectShelter(s: Gis.RankedShelter) {
        val cur = _ui.value
        val graph = cur.graph ?: return
        if (!cur.hasLocation || s.shelterId == cur.selectedShelterId) return
        viewModelScope.launch {
            val route = withContext(Dispatchers.Default) {
                Gis.safeRoute(cur.userLat, cur.userLon, s.lat, s.lon, graph, cur.floodPolys)
            }
            val naive = Gis.segmentCrossesFlood(
                doubleArrayOf(cur.userLat, cur.userLon), doubleArrayOf(s.lat, s.lon), cur.floodPolys)
            _ui.value = _ui.value.copy(
                selectedShelterId = s.shelterId, route = route, naiveCrossesFlood = naive)
        }
    }

    /** Nationwide: pick a different shelter → the map draws the line + distance from you to it. */
    fun selectPublicShelter(idx: Int) {
        val cur = _ui.value
        if (idx !in cur.nearbyPublic.indices || !cur.hasLocation) return
        _ui.value = cur.copy(selectedPublicIdx = idx)
    }

    fun clearAssistant() {
        _ui.value = _ui.value.copy(assistantAnswer = null)
    }

    /**
     * The Map Assistant. The core precomputes the real map facts (nearest shelter + ranked options,
     * safe route, nearby hospitals/clinics, flooded-road count) from the best available point. Then
     * ONE Gemma call reads the free-text question (Bangla or English) and writes the answer using
     * ONLY those facts — it understands intent and speaks fluently but can never invent a place or
     * distance. Deterministic rendering is only the offline fallback when the model isn't loaded.
     */
    fun ask(question: String) {
        val q = question.trim()
        if (q.isEmpty() || _ui.value.assistantBusy || _ui.value.locating) return
        viewModelScope.launch {
            _ui.value = _ui.value.copy(assistantBusy = true, assistantAnswer = null, locating = true)
            try {
                // Best available point: existing pin/GPS → a fresh GPS fix → region centre (approx).
                val cur = _ui.value
                val (lat, lon, real) = when {
                    cur.hasLocation -> Triple(cur.userLat, cur.userLon, true)
                    else -> {
                        val gps = app.locationProvider.current()
                        if (gps != null) Triple(gps.first, gps.second, true)
                        else Regions.byId(cur.overviewRegion.ifEmpty { app.prefs.activeRegion.value })
                            .let { Triple(it.centerLat, it.centerLon, false) }
                    }
                }
                if (real && !cur.hasLocation) located(lat, lon, usedGps = true, manual = false)
                else _ui.value = _ui.value.copy(locating = false)

                val c = computeCore(lat, lon)
                val facts = gatherFacts(c, lat, lon, real)
                val answer = if (app.engineHolder.isReady()) {
                    app.engineHolder.respondInBangla = app.prefs.isBangla
                    app.engineHolder.generateWith(
                        Prompts.GIS_ASSISTANT_SYSTEM,
                        "USER QUESTION:\n${Safety.wrapAsData(q)}\n\nMAP FACTS:\n$facts",
                        0.3,
                    ).trim().ifBlank { offlineAnswer(c, real) }
                } else {
                    offlineAnswer(c, real)
                }
                _ui.value = _ui.value.copy(assistantBusy = false, assistantAnswer = answer)
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(assistantBusy = false, locating = false,
                    assistantAnswer = t("Sorry — could not answer that just now.",
                        "দুঃখিত — এখন উত্তর দেওয়া গেল না।"))
            }
        }
    }

    /** Grounded map facts for the model, computed from an explicit point. */
    private fun gatherFacts(c: Computed, lat: Double, lon: Double, real: Boolean): String = buildString {
        append("District: ${c.districtEn}.\n")
        if (!real) append("NOTE: The user's exact location is not set (GPS off). These are computed " +
            "from the centre of ${c.districtEn} and are approximate — tell them to enable GPS or tap " +
            "the map to set their location for exact distances.\n")
        if (c.detailed) {
            append("Coverage: detailed offline map with flood-avoiding routing.\n")
            c.ranked.take(4).forEachIndexed { i, s ->
                append("Shelter ${i + 1}: ${s.name}, ${dist(s.distM)}, ${s.capacityLeft} spaces free")
                append(if (s.onHighGround) ", on high ground.\n" else ".\n")
            }
            c.ranked.firstOrNull()?.let { append("Recommended (best-ranked) shelter: ${it.name}.\n") }
            c.route?.let {
                append("Walking route to the recommended shelter: ${dist(it.distM)}, ")
                append(if (it.crossesFlood) "no route avoids the sample flood zone — it crosses it.\n"
                else "avoids the sample flood zone.\n")
            }
            val fac = runCatching { RegionAssets.loadFacilities(ctx, c.packId) }.getOrDefault(emptyList())
            listOf("hospital" to "Hospitals", "clinic" to "Clinics", "relief" to "Relief points").forEach { (type, label) ->
                val hits = Gis.nearbyFacilities(lat, lon, type, fac, 3)
                if (hits.isNotEmpty()) append("$label nearby: " + hits.joinToString(", ") {
                    "${it.name} (${dist(Gis.haversineM(lat, lon, it.lat, it.lon).toInt())})"
                } + ".\n")
            }
            c.graph?.let { g ->
                val flooded = g.edges.count { (a, b) ->
                    Gis.segmentCrossesFlood(g.nodes.getValue(a), g.nodes.getValue(b), c.flood)
                }
                append("Flooded road segments in the sample flood zone: $flooded.\n")
            }
        } else {
            append("Coverage: basic nationwide only — nearest shelters known, but no detailed routing, " +
                "hospital, or road data for this district.\n")
            c.nearbyPublic.take(4).forEach {
                append("Shelter (school/college): ${it.shelter.name}, ${dist(it.distM)}.\n")
            }
        }

        // ---- What the neighbours have said ----
        // The shipped map knows where roads and shelters are; only the board knows which of
        // them are under water or already full an hour ago. Feeding it in is what lets the
        // assistant answer "is that route safe" rather than just "that route exists". They are
        // eyewitness claims, not survey data, so they are labelled as such and attributed —
        // the model is told to pass that uncertainty on rather than launder it into fact.
        val board = app.communityRepository.entries.value
        if (board.isNotEmpty()) {
            append("\nNEIGHBOUR REPORTS — eyewitness accounts posted to the community board on ")
            append("this phone, newest first. They are unverified. If you use one, say who ")
            append("reported it and how long ago, and never state it as confirmed fact:\n")
            board.sortedByDescending { it.ts }.take(10).forEach { r ->
                val kind = CommunityKinds.byId(r.kind).en
                val where = if (r.lat != null && r.lon != null) {
                    ", ${dist(Gis.haversineM(lat, lon, r.lat, r.lon).toInt())} from the user"
                } else {
                    ", position not recorded"
                }
                val who = if (r.mine) "the user themselves" else r.sender.ifBlank { "a neighbour" }
                append("- $kind$where, ${ageLabel(r.ts)}, reported by $who")
                // What the neighbours made of it. One claim and a claim four people have walked
                // past and confirmed are not the same evidence, and a claim more people dispute
                // than confirm is one the assistant should hedge rather than repeat.
                val up = app.communityRepository.confirmCount(r.id)
                val down = app.communityRepository.disputeCount(r.id)
                if (up > 0 || down > 0) {
                    append(" [$up other phones confirm, $down dispute")
                    if (down > up && down >= 2) append("; disputed more than confirmed")
                    append("]")
                }
                append(if (r.note.isBlank()) ".\n" else ": ${Safety.wrapAsData(r.note)}\n")
            }
        }
    }

    /** How long ago, in words the model can repeat verbatim. */
    private fun ageLabel(ts: Long): String {
        val mins = ((System.currentTimeMillis() - ts) / 60_000L).coerceAtLeast(0)
        return when {
            mins < 1 -> "just now"
            mins < 60 -> "$mins minutes ago"
            mins < 60 * 24 -> "${mins / 60} hours ago"
            else -> "${mins / (60 * 24)} days ago"
        }
    }

    private val isBn get() = app.prefs.isBangla
    private fun t(en: String, bn: String) = if (isBn) bn else en
    private fun dist(m: Int): String = if (m >= 1000) "%.1f km".format(m / 1000.0) else "$m m"

    /** Offline fallback (model not downloaded): a plain bilingual answer from the computed result. */
    private fun offlineAnswer(c: Computed, real: Boolean): String {
        val prefix = if (!real) t("Set your location for exact distances. ",
            "সঠিক দূরত্বের জন্য আপনার অবস্থান নির্ধারণ করুন। ") else ""
        return prefix + if (c.detailed) {
            val top = c.ranked.firstOrNull()
                ?: return t("No shelter found near ${c.districtEn}.", "${c.districtEn}-এর কাছে আশ্রয় নেই।")
            t("Nearest safe shelter: ${top.name}, ${dist(top.distM)}", "নিকটতম নিরাপদ আশ্রয়: ${top.name}, ${dist(top.distM)}") +
                (if (top.onHighGround) t(" (on high ground).", " (উঁচু জায়গায়)।") else ".")
        } else {
            val hits = c.nearbyPublic.take(3)
            if (hits.isEmpty()) t("No shelter data near ${c.districtEn}.", "${c.districtEn}-এর কাছে আশ্রয় ডেটা নেই।")
            else t("Nearest shelters near ${c.districtEn}:\n", "${c.districtEn}-এর কাছে নিকটতম আশ্রয়:\n") +
                hits.joinToString("\n") { "•  ${it.shelter.name} — ${dist(it.distM)}" }
        }
    }

    companion object {
        // Within this distance of a pack centre we use its detailed routing map.
        const val DETAIL_RADIUS_M = 45_000.0
    }
}
