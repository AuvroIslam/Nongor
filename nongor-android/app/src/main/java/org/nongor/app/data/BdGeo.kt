package org.nongor.app.data

import android.content.Context
import org.nongor.app.core.Gis
import org.nongor.app.core.Shelter
import com.google.gson.JsonParser

/** One Bangladesh district with its Bangla name and centre. */
data class District(val name: String, val bn: String, val lat: Double, val lon: Double)

/**
 * Nationwide geography so the app works anywhere in Bangladesh, not only inside a detailed
 * region pack. Districts come from the open nuhil/bangladesh-geocode dataset (gov.bd sourced):
 * all 64 districts with Bangla names + coordinates. Used to reverse-geocode the user's GPS and
 * to fall back gracefully where no detailed map is bundled.
 */
object BdGeo {

    // Bangladesh's district centroids are all well within this radius of any point inside the
    // country; a "nearest" fix farther than this is almost certainly a bad GPS reading or a
    // location outside Bangladesh, and must not be silently labelled as a specific district.
    private const val MAX_DISTRICT_DISTANCE_M = 150_000.0

    @Volatile private var cache: List<District>? = null

    fun districts(ctx: Context): List<District> = cache ?: synchronized(this) {
        cache ?: run {
            val list = runCatching {
                val txt = ctx.assets.open("bd_districts.json").bufferedReader().use { it.readText() }
                JsonParser.parseString(txt).asJsonArray.map {
                    val o = it.asJsonObject
                    District(o.get("name").asString, o.get("bn").asString,
                        o.get("lat").asDouble, o.get("lon").asDouble)
                }
            }.getOrDefault(emptyList())
            cache = list
            list
        }
    }

    fun nearestDistrict(ctx: Context, lat: Double, lon: Double): District {
        val nearest = districts(ctx).minByOrNull { Gis.haversineM(lat, lon, it.lat, it.lon) }
            ?: return District("Bangladesh", "বাংলাদেশ", lat, lon)
        val dist = Gis.haversineM(lat, lon, nearest.lat, nearest.lon)
        return if (dist <= MAX_DISTRICT_DISTANCE_M) nearest
        else District("Outside Bangladesh", "বাংলাদেশের বাইরে", lat, lon)
    }

    /** Nearest detailed region pack and the straight-line distance (metres) to its centre. */
    fun nearestPack(lat: Double, lon: Double): Pair<RegionPack, Double> =
        Regions.ALL.map { it to Gis.haversineM(lat, lon, it.centerLat, it.centerLon) }
            .minByOrNull { it.second }!!

    /** Union of shelters from every bundled pack — the nationwide nearest-shelter list. */
    fun allShelters(ctx: Context): List<Shelter> =
        Regions.ALL.flatMap {
            runCatching { RegionAssets.loadShelters(ctx, it.id) }.getOrDefault(emptyList())
        }
}
