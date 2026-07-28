package org.nongor.app.data

import android.content.Context
import org.nongor.app.core.Facility
import org.nongor.app.core.Gis
import org.nongor.app.core.KbChunk
import org.nongor.app.core.Shelter
import org.nongor.app.core.SosReport
import com.google.gson.JsonParser

/** Loads the bundled offline region pack + first-aid knowledge from app assets. */
object RegionAssets {

    private fun read(ctx: Context, name: String): String =
        ctx.assets.open(name).bufferedReader().use { it.readText() }

    fun loadFirstAid(ctx: Context): List<KbChunk> = runCatching {
        val arr = JsonParser.parseString(read(ctx, "first_aid_packs.json")).asJsonArray
        arr.map { el ->
            val o = el.asJsonObject
            KbChunk(
                id = o.get("id").asString,
                pack = o.get("pack").asString,
                hazard = o.get("hazard")?.asString ?: o.get("pack").asString,
                textMd = o.get("text_md").asString,
                source = o.get("source")?.asString ?: "",
                lang = o.get("lang")?.asString ?: "en",
                symptomTags = o.getAsJsonArray("symptom_tags")?.map { it.asString } ?: emptyList(),
                redFlags = o.getAsJsonArray("red_flags")?.map { it.asString } ?: emptyList(),
            )
        }
    }.getOrDefault(emptyList())

    private fun features(ctx: Context, name: String) =
        JsonParser.parseString(read(ctx, name)).asJsonObject.getAsJsonArray("features")

    fun loadShelters(ctx: Context, region: String = "chattogram"): List<Shelter> = runCatching {
        features(ctx, "$region/shelters.geojson").map {
            val ft = it.asJsonObject
            val p = ft.getAsJsonObject("properties")
            val c = ft.getAsJsonObject("geometry").getAsJsonArray("coordinates")
            Shelter(
                id = p.get("id").asString, name = p.get("name").asString,
                lat = c[1].asDouble, lon = c[0].asDouble,
                capacity = p.get("capacity")?.asInt ?: 0, occupancy = p.get("occupancy")?.asInt ?: 0,
                hasPwdAccess = p.get("has_pwd_access")?.asBoolean ?: false,
                allowsPets = p.get("allows_pets")?.asBoolean ?: false,
                hasMedical = p.get("has_medical")?.asBoolean ?: false,
                onHighGround = p.get("on_high_ground")?.asBoolean ?: false,
            )
        }
    }.getOrDefault(emptyList())

    fun loadFacilities(ctx: Context, region: String = "chattogram"): List<Facility> = runCatching {
        features(ctx, "$region/facilities.geojson").map {
            val ft = it.asJsonObject
            val p = ft.getAsJsonObject("properties")
            val c = ft.getAsJsonObject("geometry").getAsJsonArray("coordinates")
            Facility(
                id = p.get("id").asString, name = p.get("name").asString,
                lat = c[1].asDouble, lon = c[0].asDouble, type = p.get("type").asString,
            )
        }
    }.getOrDefault(emptyList())

    /** Flood exterior rings, each as list of [lon, lat]. */
    fun loadFloodPolys(ctx: Context, region: String = "chattogram"): List<List<DoubleArray>> = runCatching {
        features(ctx, "$region/flood_zones.geojson").mapNotNull {
            val geom = it.asJsonObject.getAsJsonObject("geometry")
            if (geom.get("type").asString != "Polygon") return@mapNotNull null
            geom.getAsJsonArray("coordinates")[0].asJsonArray.map { pt ->
                val a = pt.asJsonArray; doubleArrayOf(a[0].asDouble, a[1].asDouble)
            }
        }
    }.getOrDefault(emptyList())

    fun loadScenarios(ctx: Context): List<SosReport> = runCatching {
        val arr = JsonParser.parseString(read(ctx, "chattogram_sos.json")).asJsonArray
        arr.map { el ->
            val o = el.asJsonObject
            SosReport(
                text = o.get("text").asString,
                lat = o.get("lat")?.asDouble, lon = o.get("lon")?.asDouble,
                reporterRole = o.get("reporter_role")?.asString ?: "affected",
                peopleCount = o.get("people_count")?.asInt ?: 1,
                flags = o.getAsJsonArray("flags")?.map { it.asString } ?: emptyList(),
            )
        }
    }.getOrDefault(emptyList())

    fun loadGraph(ctx: Context, region: String = "chattogram"): Gis.PedGraph = runCatching {
        val g = JsonParser.parseString(read(ctx, "$region/ped_graph.json")).asJsonObject
        val nodes = HashMap<String, DoubleArray>()
        g.getAsJsonObject("nodes").entrySet().forEach { (k, v) ->
            val a = v.asJsonArray; nodes[k] = doubleArrayOf(a[0].asDouble, a[1].asDouble)
        }
        val edges = g.getAsJsonArray("edges").map {
            val e = it.asJsonArray; e[0].asString to e[1].asString
        }
        Gis.PedGraph(nodes, edges)
    }.getOrDefault(Gis.PedGraph(emptyMap(), emptyList()))
}
