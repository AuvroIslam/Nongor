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

/**
 * One SOS report.
 *
 * [flags] is nullable because this type is deserialised by Gson — from bundled drill
 * scenarios, from the on-disk store, and from the mesh wire format. Gson sets fields by
 * reflection without running the constructor, so a Kotlin default is *not* applied when the
 * JSON omits the key: it leaves null in a field the type system swears is non-null. Most of
 * the bundled scenarios have no `flags` key at all. Read [riskFlags] instead.
 */
data class SosReport(
    val text: String,
    val lat: Double? = null,
    val lon: Double? = null,
    // Nullable *and* defaulted, on purpose. Kotlin callers get a fresh UUID from the default;
    // Gson skips the default and leaves null, which [id] then resolves.
    val msgId: String? = UUID.randomUUID().toString(),
    val reporterRole: String? = null,          // affected | volunteer
    val imagePath: String? = null,
    val audioPath: String? = null,
    val peopleCount: Int? = null,
    val flags: List<String>? = null,
    val status: String? = null,
    val hops: Int? = null,
) {
    /**
     * A stable identifier.
     *
     * Note this cannot be `by lazy`: the delegate is itself a field set in the constructor,
     * so Gson leaves it null and reading it throws — the same trap one level down. A plain
     * getter with a content-derived fallback avoids stored state entirely, and has a useful
     * property: re-reading the same JSON yields the same id, so reloading a file of reports
     * cannot silently duplicate them.
     */
    val id: String
        get() = msgId ?: "sos-%08x".format(contentKey())

    private fun contentKey(): Int {
        var h = text.hashCode()
        h = h * 31 + (lat?.hashCode() ?: 0)
        h = h * 31 + (lon?.hashCode() ?: 0)
        h = h * 31 + (peopleCount ?: 0)
        return h
    }

    /** Reporter-supplied risk flags, never null. */
    val riskFlags: List<String> get() = flags ?: emptyList()

    val role: String get() = reporterRole ?: "affected"
    val people: Int get() = peopleCount?.takeIf { it > 0 } ?: 1
    val state: String get() = status ?: "new"
    val hopCount: Int get() = hops ?: 0
}

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

/**
 * One passage of first-aid knowledge, loaded from a bundled JSON pack.
 *
 * Same nullability rule as [SosReport]: these packs are meant to be extended by
 * contributors, and a pack that omits `symptom_tags` must retrieve nothing rather than take
 * First Aid down. Read [tags] and [flags].
 */
data class KbChunk(
    val id: String,
    val pack: String,
    val hazard: String,
    val textMd: String,
    val source: String,
    val lang: String = "en",
    val symptomTags: List<String>? = null,
    val redFlags: List<String>? = null,
) {
    val tags: List<String> get() = symptomTags ?: emptyList()
    val flags: List<String> get() = redFlags ?: emptyList()
}

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
