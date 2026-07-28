package org.nongor.app.core

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import java.util.PriorityQueue

/**
 * GIS engine — Kotlin port of nongor/core/gis.py.
 * The LLM picks a tool; this computes the geometry deterministically. Pure JVM.
 * A ring is a list of [lon, lat] pairs (GeoJSON order). A point is (lat, lon).
 */
object Gis {

    private const val EARTH_R_M = 6_371_000.0

    fun haversineM(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val p1 = Math.toRadians(lat1); val p2 = Math.toRadians(lat2)
        val dphi = Math.toRadians(lat2 - lat1); val dlmb = Math.toRadians(lon2 - lon1)
        val a = sin(dphi / 2) * sin(dphi / 2) +
            cos(p1) * cos(p2) * sin(dlmb / 2) * sin(dlmb / 2)
        return 2 * EARTH_R_M * asin(sqrt(a))
    }

    /** Ray-casting point-in-polygon. ring = [[lon,lat],...]. */
    fun pointInRing(lat: Double, lon: Double, ring: List<DoubleArray>): Boolean {
        var inside = false
        val n = ring.size
        var j = n - 1
        for (i in 0 until n) {
            val xi = ring[i][0]; val yi = ring[i][1]
            val xj = ring[j][0]; val yj = ring[j][1]
            if ((yi > lat) != (yj > lat) &&
                lon < (xj - xi) * (lat - yi) / ((yj - yi).let { if (it == 0.0) 1e-12 else it }) + xi
            ) inside = !inside
            j = i
        }
        return inside
    }

    fun pointInFlood(lat: Double, lon: Double, floodPolys: List<List<DoubleArray>>): Boolean =
        floodPolys.any { pointInRing(lat, lon, it) }

    private fun ccw(p: DoubleArray, q: DoubleArray, r: DoubleArray): Boolean =
        (r[1] - p[1]) * (q[0] - p[0]) > (q[1] - p[1]) * (r[0] - p[0])

    /** a, b are (lat, lon). */
    fun segIntersectsRing(a: DoubleArray, b: DoubleArray, ring: List<DoubleArray>): Boolean {
        if (pointInRing(a[0], a[1], ring) || pointInRing(b[0], b[1], ring)) return true
        val n = ring.size
        for (i in 0 until n) {
            val c = doubleArrayOf(ring[i][1], ring[i][0])                 // -> (lat,lon)
            val d = doubleArrayOf(ring[(i + 1) % n][1], ring[(i + 1) % n][0])
            if (ccw(a, c, d) != ccw(b, c, d) && ccw(a, b, c) != ccw(a, b, d)) return true
        }
        return false
    }

    fun segmentCrossesFlood(a: DoubleArray, b: DoubleArray, floodPolys: List<List<DoubleArray>>) =
        floodPolys.any { segIntersectsRing(a, b, it) }

    // ---- Pedestrian graph + Dijkstra -------------------------------------- #
    class PedGraph(val nodes: Map<String, DoubleArray>, val edges: List<Pair<String, String>>) {
        /** Nearest node id + snap distance in metres, or null if the graph has no nodes at all. */
        fun nearestNode(lat: Double, lon: Double): Pair<String, Double>? =
            nodes.minByOrNull { haversineM(lat, lon, it.value[0], it.value[1]) }
                ?.let { it.key to haversineM(lat, lon, it.value[0], it.value[1]) }

        fun adjacency(floodPolys: List<List<DoubleArray>>?): Map<String, MutableList<Pair<String, Double>>> {
            val adj = nodes.keys.associateWith { mutableListOf<Pair<String, Double>>() }
            for ((u, v) in edges) {
                val a = nodes.getValue(u); val b = nodes.getValue(v)
                if (floodPolys != null && segmentCrossesFlood(a, b, floodPolys)) continue
                val w = haversineM(a[0], a[1], b[0], b[1])
                adj.getValue(u).add(v to w); adj.getValue(v).add(u to w)
            }
            return adj
        }
    }

    fun dijkstra(adj: Map<String, List<Pair<String, Double>>>, src: String, dst: String): Pair<List<String>?, Double> {
        val dist = HashMap<String, Double>().apply { put(src, 0.0) }
        val prev = HashMap<String, String>()
        val pq = PriorityQueue<Pair<Double, String>>(compareBy { it.first })
        pq.add(0.0 to src)
        val seen = HashSet<String>()
        while (pq.isNotEmpty()) {
            val top = pq.poll() ?: break
            val d = top.first; val u = top.second
            if (u in seen) continue
            seen.add(u)
            if (u == dst) break
            for ((v, w) in adj[u] ?: emptyList()) {
                val nd = d + w
                if (nd < (dist[v] ?: Double.MAX_VALUE)) {
                    dist[v] = nd; prev[v] = u; pq.add(nd to v)
                }
            }
        }
        if (dst !in dist) return null to Double.MAX_VALUE
        val path = ArrayList<String>().apply { add(dst) }
        while (path.last() != src) path.add(prev.getValue(path.last()))
        path.reverse()
        return path to dist.getValue(dst)
    }

    // ---- Tools ------------------------------------------------------------ #
    private const val W_DIST = 0.4; private const val W_PWD = 0.3
    private const val W_PET = 0.2;  private const val W_CAP = 0.1
    private const val HIGH_GROUND_BONUS = 0.15

    data class RankedShelter(
        val shelterId: String, val name: String, val distM: Int, val score: Double,
        val capacityLeft: Int, val hasPwdAccess: Boolean, val onHighGround: Boolean,
        val lat: Double, val lon: Double,
    )

    fun findNearestShelter(
        userLat: Double, userLon: Double, shelters: List<Shelter>,
        profile: List<String> = emptyList(), topN: Int = 3,
    ): List<RankedShelter> {
        if (shelters.isEmpty()) return emptyList()
        val wantPwd = "pwd" in profile || "elderly" in profile
        val wantPet = "pet" in profile
        val dists = shelters.associate { it.id to haversineM(userLat, userLon, it.lat, it.lon) }
        val dmax = dists.values.maxOrNull()?.takeIf { it > 0 } ?: 1.0
        return shelters.map { s ->
            val normD = dists.getValue(s.id) / dmax
            val pwdPenalty = if (!wantPwd || s.hasPwdAccess) 0.0 else 1.0
            val petPenalty = if (!wantPet || s.allowsPets) 0.0 else 1.0
            var score = W_DIST * normD + W_PWD * pwdPenalty + W_PET * petPenalty +
                W_CAP * s.capacityPressure
            if (s.onHighGround) score -= HIGH_GROUND_BONUS
            if (s.capacityLeft <= 0) score += 0.5
            Triple(score, s, dists.getValue(s.id))
        }.sortedBy { it.first }.take(topN).map { (score, s, dist) ->
            RankedShelter(s.id, s.name, dist.toInt(), (Math.round(score * 1000) / 1000.0),
                s.capacityLeft, s.hasPwdAccess, s.onHighGround, s.lat, s.lon)
        }
    }

    data class Route(val polyline: List<DoubleArray>, val distM: Int,
                     val crossesFlood: Boolean, val routable: Boolean)

    // Beyond this snap distance, the request point is outside the detailed pack's road network
    // entirely (wrong pack, GPS drift, or a bordering district) — a graph route through whatever
    // node happens to be "nearest" would be a confidently-wrong path, not a real one.
    private const val MAX_SNAP_DISTANCE_M = 5_000.0

    private fun naiveRoute(
        fromLat: Double, fromLon: Double, toLat: Double, toLon: Double,
        floodPolys: List<List<DoubleArray>>,
    ): Route {
        val crosses = segmentCrossesFlood(
            doubleArrayOf(fromLat, fromLon), doubleArrayOf(toLat, toLon), floodPolys)
        return Route(
            listOf(doubleArrayOf(fromLat, fromLon), doubleArrayOf(toLat, toLon)),
            haversineM(fromLat, fromLon, toLat, toLon).toInt(), crosses, false)
    }

    fun safeRoute(
        fromLat: Double, fromLon: Double, toLat: Double, toLon: Double,
        graph: PedGraph, floodPolys: List<List<DoubleArray>>,
    ): Route {
        val (srcNode, srcDist) = graph.nearestNode(fromLat, fromLon)
            ?: return naiveRoute(fromLat, fromLon, toLat, toLon, floodPolys)
        val (dstNode, dstDist) = graph.nearestNode(toLat, toLon)
            ?: return naiveRoute(fromLat, fromLon, toLat, toLon, floodPolys)
        if (srcDist > MAX_SNAP_DISTANCE_M || dstDist > MAX_SNAP_DISTANCE_M) {
            return naiveRoute(fromLat, fromLon, toLat, toLon, floodPolys)
        }
        val adj = graph.adjacency(floodPolys)
        val (path, dist) = dijkstra(adj, srcNode, dstNode)
        if (path == null) return naiveRoute(fromLat, fromLon, toLat, toLon, floodPolys)
        val poly = path.map { graph.nodes.getValue(it) }
        return Route(poly, dist.toInt(), false, true)
    }

    fun nearbyFacilities(lat: Double, lon: Double, type: String,
                         facilities: List<Facility>, topN: Int = 3): List<Facility> =
        facilities.filter { it.type == type }
            .sortedBy { haversineM(lat, lon, it.lat, it.lon) }.take(topN)

    /** Deterministic tool router when Gemma's tool call is invalid/absent. */
    fun keywordToolFallback(text: String): Pair<String, Map<String, Any>> {
        val t = text.lowercase()
        if (listOf("shelter", "safe place", "evacuat", "আশ্রয়").any { it in t }) {
            val prof = mutableListOf<String>()
            if (listOf("elder", "old", "বয়স্ক").any { it in t }) prof.add("elderly")
            if (listOf("wheelchair", "disab", "pwd", "প্রতিবন্ধ").any { it in t }) prof.add("pwd")
            if (listOf("pet", "dog", "cat").any { it in t }) prof.add("pet")
            return "find_nearest_shelter" to mapOf("profile" to prof)
        }
        if (listOf("hospital", "clinic", "doctor", "হাসপাতাল").any { it in t })
            return "nearby_facilities" to mapOf("type" to "hospital")
        if (listOf("relief", "food", "water", "ত্রাণ").any { it in t })
            return "nearby_facilities" to mapOf("type" to "relief")
        if (listOf("flood road", "blocked", "which road", "রাস্তা").any { it in t })
            return "flooded_roads_near" to mapOf("radius_m" to 2000)
        if (listOf("route", "way to", "how do i get", "path").any { it in t })
            return "safe_route" to emptyMap()
        return "none" to emptyMap()
    }
}
