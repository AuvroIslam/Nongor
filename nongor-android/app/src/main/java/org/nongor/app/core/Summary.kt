package org.nongor.app.core

/** Coordinator disaster summary — Kotlin port of nongor/core/summary.py. Counts in code; Gemma phrases. */
object Summary {

    private val rank = mapOf("critical" to 0, "high" to 1, "moderate" to 2, "low" to 3)

    data class TopCase(val id: String, val loc: String, val reason: String, val priority: String)
    data class ShelterPressure(val name: String, val occupancy: Int, val capacity: Int, val pressure: Double)
    data class Stats(
        val totalSos: Int, val newSos: Int,
        val critical: Int, val high: Int, val moderate: Int, val low: Int,
        val top5: List<TopCase>, val shortages: Map<String, Int>,
        val shelterPressure: List<ShelterPressure>, val blockedRoads: List<String>,
    )

    fun computeStats(
        sos: List<SosReport>, triage: List<TriageResult>,
        shelters: List<Shelter> = emptyList(), blockedRoads: List<String> = emptyList(),
        newSince: Int = 0,
    ): Stats {
        val sosById = sos.associateBy { it.id }
        val ordered = triage.sortedWith(compareBy({ rank[it.priority] }, { -it.urgencyScore })).take(5)
        val top5 = ordered.map { t ->
            val s = sosById[t.msgId]
            val loc = if (s?.lat != null) "%.3f,%.3f".format(s.lat, s.lon) else "loc?"
            TopCase(t.msgId.take(8), loc, t.rationale, t.priority)
        }
        val shortages = HashMap<String, Int>()
        for (t in triage) for (sig in t.riskSignals)
            if (sig == "no_food_water" || sig == "medication_needed")
                shortages[sig] = (shortages[sig] ?: 0) + 1
        return Stats(
            totalSos = sos.size, newSos = newSince,
            critical = triage.count { it.priority == "critical" },
            high = triage.count { it.priority == "high" },
            moderate = triage.count { it.priority == "moderate" },
            low = triage.count { it.priority == "low" },
            top5 = top5, shortages = shortages,
            shelterPressure = shelters.map {
                ShelterPressure(it.name, it.occupancy, it.capacity,
                    Math.round(it.capacityPressure * 100) / 100.0)
            },
            blockedRoads = blockedRoads,
        )
    }

    // The per-case list (ids, coordinates) is rendered separately by the app, so it is deliberately
    // NOT part of this prose — keeping both the Gemma and offline briefings free of raw values that
    // must be exact.
    fun deterministicBriefing(st: Stats): String {
        val sb = StringBuilder()
        sb.append("1) Situation: ${st.totalSos} SOS total (${st.newSos} new).\n")
        sb.append("2) Critical: ${st.critical} | High: ${st.high} | Moderate: ${st.moderate} | Low: ${st.low}.\n")
        val sh = st.shortages.entries.joinToString(", ") { "${it.key}×${it.value}" }.ifEmpty { "none reported" }
        sb.append("3) Shortages: $sh.\n")
        val nearCap = st.shelterPressure.count { it.pressure >= 0.8 }
        sb.append("4) Shelter pressure: $nearCap of ${st.shelterPressure.size} shelters near or over capacity.\n")
        // "Crosses the flood layer" is not "blocked". The engine counts graph segments that
        // intersect an illustrative flood polygon; reporting those as blocked roads is a much
        // bigger claim than the data supports, and a responder acting on it would route around
        // roads that are perfectly passable.
        sb.append(
            "5) Roads: ${st.blockedRoads.size} road segments cross the sample flood layer " +
                "(illustrative, not live flood data).\n",
        )
        sb.append("6) Recommended focus: ${if (st.critical > 0) "critical cases first" else "high-priority cases"}.")
        return sb.toString()
    }

    data class Briefing(val briefing: String, val stats: Stats, val producedBy: String)

    fun disasterSummary(
        sos: List<SosReport>, triage: List<TriageResult>, shelters: List<Shelter> = emptyList(),
        blockedRoads: List<String> = emptyList(), newSince: Int = 0, gemma: LlmEngine? = null,
    ): Briefing {
        val st = computeStats(sos, triage, shelters, blockedRoads, newSince)
        if (gemma == null) return Briefing(deterministicBriefing(st), st, "deterministic")
        val user = "NUMBERS + CASES (use only these):\n" + statsToJson(st)
        val briefing = gemma.generate(Prompts.SUMMARY_SYSTEM, user, temperature = 0.3, maxTokens = 350)
        return Briefing(briefing, st, "gemma")
    }

    // Only aggregate COUNTS go to the model — never per-case coordinates, shelter capacities, or the
    // road-segment list. The model reliably phrases small counts but mangles long numbers, IDs and
    // lists (it once turned "500" into "50000" and a road list into runaway garbage), so every exact
    // value is rendered by the app instead. This is the same "counts in code, Gemma phrases" rule the
    // per-report counts already follow.
    /** Internal, but visible to tests so the field naming can be pinned. */
    internal fun statsToJson(st: Stats): String = buildString {
        append("{\"total\":${st.totalSos},\"new\":${st.newSos},\"critical\":${st.critical},")
        append("\"high\":${st.high},\"moderate\":${st.moderate},\"low\":${st.low},")
        append("\"shortages\":{")
        append(st.shortages.entries.joinToString(",") { "\"${it.key}\":${it.value}" })
        append("},\"sheltersTracked\":${st.shelterPressure.size},")
        append("\"sheltersNearCapacity\":${st.shelterPressure.count { it.pressure >= 0.8 }},")
        // Named for what it is. Handing the model a field called "blockedRoads" invites it to
        // write "blocked roads" back out, which is how the overclaim reached the screen.
        append("\"floodCrossingRoadSegments\":${st.blockedRoads.size}}")
    }
}
