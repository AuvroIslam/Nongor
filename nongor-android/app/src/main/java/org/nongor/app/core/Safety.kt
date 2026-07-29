package org.nongor.app.core

/**
 * Safety sidecar — Kotlin port of nongor/core/safety.py.
 * Deterministic guards around every Gemma call (injection defense, red-flag, disclaimer).
 */
object Safety {

    const val DISCLAIMER = "This is first-aid guidance, not a substitute for professional medical care."

    private val redFlagTerms = listOf(
        "not breathing", "no breath", "stopped breathing", "unconscious", "not responding",
        "heavy bleeding", "bleeding heavily", "spurting", "choking", "no pulse", "drowning",
        "শ্বাস নিচ্ছে না", "অজ্ঞান", "রক্তক্ষরণ", "ডুবে",
    )

    private val injection = Regex(
        "(ignore\\s+[\\w\\s]{0,25}?instructions|" +
            "disregard\\s+[\\w\\s]{0,25}?(system|instructions|prompt|rules)|" +
            "you are now|forget\\s+[\\w\\s]{0,20}?(rules|instructions|prompt)|" +
            "reveal your (system )?prompt|new instructions:|act as (if|a|an|though))",
        RegexOption.IGNORE_CASE,
    )

    fun wrapAsData(untrusted: String) = "<<<DATA_START>>>\n$untrusted\n<<<DATA_END>>>"

    fun detectInjection(untrusted: String?): Boolean = injection.containsMatchIn(untrusted ?: "")

    fun isRedFlag(text: String?): Boolean {
        val t = (text ?: "").lowercase()
        return redFlagTerms.any { it in t }
    }

    fun redFlagBanner(): String =
        "জীবন-সংকটজনক অবস্থা — এখনই সাহায্য নিন / LIFE-THREATENING: seek emergency help NOW."

    fun ensureDisclaimer(answer: String, medical: Boolean = true): String =
        if (medical && DISCLAIMER !in answer) answer.trimEnd() + "\n" + DISCLAIMER else answer

    data class Guard(
        val answer: String, val redFlag: Boolean, val banner: String?,
        val injectionDetected: Boolean,
    )

    fun guardMedicalAnswer(query: String, answer: String): Guard {
        val flag = isRedFlag(query)
        return Guard(
            answer = ensureDisclaimer(answer, medical = true),
            redFlag = flag,
            banner = if (flag) redFlagBanner() else null,
            injectionDetected = detectInjection(query),
        )
    }
}
