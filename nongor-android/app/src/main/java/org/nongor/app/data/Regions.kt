package org.nongor.app.data

/** Offline region packs bundled with the app. Each maps to an assets/<id>/ folder of geojson. */
data class RegionPack(
    val id: String,
    val nameEn: String,
    val nameBn: String,
    val descEn: String,
    val descBn: String,
    val centerLat: Double,
    val centerLon: Double,
)

object Regions {
    val ALL = listOf(
        RegionPack(
            "chattogram", "Chattogram", "চট্টগ্রাম",
            "Coastal city on the Karnaphuli basin", "কর্ণফুলী অববাহিকার উপকূলীয় শহর",
            22.345, 91.815,
        ),
        RegionPack(
            "sylhet", "Sylhet", "সিলেট",
            "Surma river basin, monsoon flooding", "সুরমা অববাহিকা, বর্ষার বন্যা",
            24.895, 91.872,
        ),
        RegionPack(
            "sunamganj", "Sunamganj", "সুনামগঞ্জ",
            "Haor wetlands, flash floods", "হাওর অঞ্চল, আকস্মিক বন্যা",
            25.065, 91.395,
        ),
    )

    fun byId(id: String): RegionPack = ALL.firstOrNull { it.id == id } ?: ALL.first()
}
