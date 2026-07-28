package org.nongor.app.data

import android.content.Context
import org.nongor.app.core.Gis
import com.google.gson.JsonParser

/** One public building (school / college / university) that can serve as a flood shelter. */
data class PublicShelter(
    val name: String,
    val type: String,          // "s" school | "c" college | "u" university
    val district: String,
    val upazila: String,
    val lat: Double,
    val lon: Double,
)

data class PublicShelterHit(val shelter: PublicShelter, val distM: Int)

/**
 * Nationwide shelter-proxy layer: ~9,500 schools, colleges and universities across all 64
 * districts, from HOT-OSM (hotosm_bgd_education_facilities, ODbL). In Bangladesh these public
 * buildings are what the government designates as flood/cyclone shelters, so the nearest one is
 * a realistic place to head for anywhere in the country — real data, not hand-authored samples.
 */
object PublicShelters {

    @Volatile private var cache: List<PublicShelter>? = null

    fun all(ctx: Context): List<PublicShelter> = cache ?: synchronized(this) {
        cache ?: run {
            val list = runCatching {
                val txt = ctx.assets.open("bd_shelters.json").bufferedReader().use { it.readText() }
                JsonParser.parseString(txt).asJsonArray.map {
                    val o = it.asJsonObject
                    PublicShelter(
                        name = o.get("n").asString, type = o.get("t").asString,
                        district = o.get("d").asString, upazila = o.get("u").asString,
                        lat = o.get("la").asDouble, lon = o.get("lo").asDouble,
                    )
                }
            }.getOrDefault(emptyList())
            cache = list
            list
        }
    }

    fun nearest(ctx: Context, lat: Double, lon: Double, n: Int = 6): List<PublicShelterHit> =
        all(ctx).map { PublicShelterHit(it, Gis.haversineM(lat, lon, it.lat, it.lon).toInt()) }
            .sortedBy { it.distM }
            .distinctBy { it.shelter.name }        // OSM maps some places twice — keep the nearest
            .take(n)
}
