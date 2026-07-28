package org.nongor.app.core

import com.google.gson.JsonObject
import com.google.gson.JsonParser

/**
 * Triage engine — Kotlin port of nongor/core/triage.py.
 * Gemma -> JSON -> validate -> (deterministic fallback if invalid). The queue never breaks.
 */
object Triage {

    private val priorityRank = mapOf("critical" to 0, "high" to 1, "moderate" to 2, "low" to 3)

    private val keywords: Map<String, List<String>> = mapOf(
        "not_breathing" to listOf("not breathing", "no breath", "stopped breathing", "শ্বাস নিচ্ছে না"),
        "unconscious" to listOf("unconscious", "passed out", "not responding", "অজ্ঞান"),
        "heavy_bleeding" to listOf("bleeding heavily", "heavy bleeding", "lot of blood", "spurting",
            "রক্তক্ষরণ", "প্রচুর রক্ত"),
        "severe_injury" to listOf("broken", "fracture", "deep cut", "burn", "injured badly", "আহত",
            "snake", "snakebite", "bitten", "sting", "amputat"),
        "trapped" to listOf("trapped", "stuck", "can't get out", "roof", "rooftop", "আটকে", "ছাদে"),
        "rising_water" to listOf("water rising", "rising water", "water is rising", "flood rising",
            "still rising", "water level rising", "water rose", "পানি বাড়", "বন্যা বাড়"),
        "child" to listOf("child", "baby", "infant", "kid", "শিশু", "বাচ্চা"),
        "elderly" to listOf("elderly", "old man", "old woman", "grandmother", "grandfather", "বয়স্ক"),
        "pregnant" to listOf("pregnant", "pregnancy", "গর্ভবতী"),
        "chronic_illness" to listOf("diabetic", "heart", "asthma", "dialysis", "chronic", "অসুস্থ"),
        "no_food_water" to listOf("no food", "no water", "hungry", "thirsty", "no drinking",
            "খাবার নেই", "পানি নেই"),
        "medication_needed" to listOf("medicine", "insulin", "medication", "inhaler", "ঔষধ", "ওষুধ"),
    )

    fun detectSignals(text: String?, givenFlags: List<String> = emptyList()): List<String> {
        val t = (text ?: "").lowercase()
        val found = givenFlags.toMutableSet()
        for ((sig, kws) in keywords) if (kws.any { it in t }) found.add(sig)
        return RISK_SIGNALS.filter { it in found }        // stable ordering
    }

    fun priorityFromSignals(signals: List<String>): Pair<String, Double> {
        val s = signals.toSet()
        if (s.any { it in LIFE_THREAT_SIGNALS } || (s.containsAll(listOf("trapped", "rising_water"))))
            return "critical" to 0.95
        val vuln = s.intersect(setOf("child", "elderly", "pregnant", "chronic_illness"))
        if (s.any { it in setOf("severe_injury", "trapped", "medication_needed") } ||
            (vuln.isNotEmpty() && "rising_water" in s)
        ) return "high" to 0.75
        if (s.any { it in setOf("rising_water", "no_food_water") } || vuln.isNotEmpty())
            return "moderate" to 0.45
        return "low" to 0.2
    }

    private fun defaultAction(priority: String): String = when (priority) {
        "critical" -> "Dispatch rescue immediately; flag for human review."
        "high" -> "Prioritise on next rescue run; verify details."
        "moderate" -> "Queue for relief/support; monitor."
        else -> "Log; follow up when capacity allows."
    }

    fun fallbackTriage(sos: SosReport): TriageResult {
        val signals = detectSignals(sos.text, sos.riskFlags)
        val (priority, score) = priorityFromSignals(signals)
        return TriageResult(
            msgId = sos.id, priority = priority, urgencyScore = score,
            riskSignals = signals, needsHumanReview = priority == "critical" || priority == "high",
            rationale = if (signals.isNotEmpty()) "Signals: " + signals.joinToString(", ")
            else "No strong signals detected.",
            recommendedAction = defaultAction(priority), producedBy = "fallback_rules",
        )
    }

    fun extractJson(raw: String?): JsonObject? {
        if (raw.isNullOrEmpty()) return null
        val fence = Regex("```(?:json)?\\s*(\\{[\\s\\S]*?\\})\\s*```").find(raw)
        val candidate = fence?.groupValues?.get(1) ?: run {
            val start = raw.indexOf('{'); val end = raw.lastIndexOf('}')
            if (start != -1 && end > start) raw.substring(start, end + 1) else null
        } ?: return null
        return try {
            JsonParser.parseString(candidate).asJsonObject
        } catch (_: Exception) {
            null
        }
    }

    fun validateTriage(obj: JsonObject?): Pair<Boolean, List<String>> {
        if (obj == null) return false to listOf("null")
        val errs = mutableListOf<String>()
        val pri = obj.get("priority")?.takeIf { it.isJsonPrimitive }?.asString
        if (pri !in PRIORITIES) errs.add("priority invalid")
        val score = try { obj.get("urgency_score")?.asDouble } catch (_: Exception) { null }
        if (score == null || score < 0.0 || score > 1.0) errs.add("urgency_score out of range")
        val sig = try { obj.getAsJsonArray("risk_signals")?.map { it.asString } } catch (_: Exception) { null }
        if (sig == null || sig.any { it !in RISK_SIGNALS }) errs.add("risk_signals invalid")
        if (obj.get("needs_human_review")?.isJsonPrimitive != true) errs.add("needs_human_review")
        for (k in listOf("rationale", "recommended_action")) {
            val v = obj.get(k)?.takeIf { it.isJsonPrimitive }?.asString
            if (v.isNullOrEmpty()) errs.add("$k missing")
        }
        return (errs.isEmpty()) to errs
    }

    // The SOS text is untrusted (it can arrive over the mesh from another device), so it is fenced
    // as data — an embedded "ignore your instructions" can't be read as a command to the model.
    fun triageUserPrompt(text: String): String = "SOS:\n${Safety.wrapAsData(text)}\nJSON:"

    /** Parse a raw model response into a TriageResult, or fall back to rules if invalid. */
    fun fromRawOrFallback(sos: SosReport, raw: String, modelName: String): TriageResult {
        val obj = extractJson(raw)
        val (ok, _) = validateTriage(obj)
        if (!ok || obj == null) return fallbackTriage(sos)
        val priority = obj.get("priority").asString
        return TriageResult(
            msgId = sos.id,
            priority = priority,
            urgencyScore = obj.get("urgency_score").asDouble,
            riskSignals = obj.getAsJsonArray("risk_signals").map { it.asString },
            // A critical/high case must always be flagged for human review — never trust the
            // model to opt out of that safety net, even if its own JSON says otherwise.
            needsHumanReview = obj.get("needs_human_review").asBoolean ||
                priority == "critical" || priority == "high",
            rationale = obj.get("rationale").asString,
            recommendedAction = obj.get("recommended_action").asString,
            model = modelName, producedBy = "gemma",
        )
    }

    /** Triage one SOS. Uses [gemma] if it yields valid JSON, else the deterministic fallback. */
    fun triageSos(sos: SosReport, gemma: LlmEngine? = null): TriageResult {
        val result = if (gemma == null) fallbackTriage(sos) else {
            val raw = gemma.generate(Prompts.TRIAGE_SYSTEM, triageUserPrompt(sos.text),
                temperature = 0.3, maxTokens = 256)
            fromRawOrFallback(sos, raw, gemma.modelName)
        }
        // A real SOS never contains prompt-injection phrasing; if it does, the report is suspect —
        // flag it for a human rather than trusting its automated priority.
        return if (Safety.detectInjection(sos.text)) result.copy(needsHumanReview = true) else result
    }

    fun sortQueue(results: List<TriageResult>): List<TriageResult> =
        results.sortedWith(compareBy({ priorityRank[it.priority] }, { -it.urgencyScore }))
}
