package org.nongor.app.core

import java.util.UUID

/**
 * Nongor data models — Kotlin port of the tested Python core (nongor/core/models.py).
 * Pure JVM (no Android deps) so the reasoning logic is unit-testable via `gradlew test`.
 */

val PRIORITIES = listOf("critical", "high", "moderate", "low")

val PRIORITY_COLOR = mapOf(
    "critical" to "🔴", "high" to "🟠", "moderate" to "🟡", "low" to "🟢",
)

/** Closed set of risk signals Gemma (and the deterministic fallback) may emit. */
val RISK_SIGNALS = listOf(
    "severe_injury", "not_breathing", "unconscious", "heavy_bleeding",
    "child", "elderly", "pregnant", "chronic_illness", "trapped",
    "rising_water", "no_food_water", "medication_needed",
)

val LIFE_THREAT_SIGNALS = listOf("not_breathing", "unconscious", "heavy_bleeding")

data class SosReport(
    val text: String,
    val lat: Double? = null,
    val lon: Double? = null,
    val msgId: String = UUID.randomUUID().toString(),
    val reporterRole: String = "affected",     // affected | volunteer
    val imagePath: String? = null,
    val audioPath: String? = null,
    val peopleCount: Int = 1,
    val flags: List<String> = emptyList(),
    val status: String = "new",
    val hops: Int = 0,
)

data class TriageResult(
    val msgId: String,
    val priority: String,
    val urgencyScore: Double,
    val riskSignals: List<String>,
    val needsHumanReview: Boolean,
    val rationale: String,
    val recommendedAction: String,
    val model: String = "gemma-4-e2b",
    val producedBy: String = "gemma",           // gemma | fallback_rules
) {
    val color: String get() = PRIORITY_COLOR[priority] ?: "⚪"
}

data class Shelter(
    val id: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val capacity: Int = 0,
    val occupancy: Int = 0,
    val hasPwdAccess: Boolean = false,
    val allowsPets: Boolean = false,
    val hasMedical: Boolean = false,
    val onHighGround: Boolean = false,
) {
    val capacityLeft: Int get() = maxOf(0, capacity - occupancy)
    val capacityPressure: Double
        get() = if (capacity <= 0) 1.0 else minOf(1.0, occupancy.toDouble() / capacity)
}

data class Facility(
    val id: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val type: String,                            // hospital | relief | clinic
)

data class KbChunk(
    val id: String,
    val pack: String,
    val hazard: String,
    val textMd: String,
    val source: String,
    val lang: String = "en",
    val symptomTags: List<String> = emptyList(),
    val redFlags: List<String> = emptyList(),
)

/** Minimal LLM abstraction so engines are testable with a mock (mirrors Python GemmaRunner). */
interface LlmEngine {
    val modelName: String
    fun generate(
        system: String,
        user: String,
        temperature: Double = 0.4,
        maxTokens: Int = 512,
    ): String

    /**
     * Multimodal generation: like [generate] but the model may also see the image at [imagePath].
     * Engines without vision ignore the image and answer from text, so only the on-device Gemma
     * engine overrides this. (imagePath has no default, so plain two-arg calls stay unambiguous.)
     */
    fun generate(
        system: String,
        user: String,
        imagePath: String?,
        temperature: Double = 0.4,
        maxTokens: Int = 512,
    ): String = generate(system, user, temperature, maxTokens)
}
