package org.nongor.app.core

/**
 * Turns "he's bleeding from the head and can't stand up" into the phrases that ask about it.
 *
 * **What Gemma is and is not allowed to do here.** It selects from the phrasebook. It never
 * writes a line in Chakma, Marma, Kokborok, Santali, Garo or Rohingya, and it never rewrites
 * a medical question. Those languages are far outside what a 2B on-device model has seen, so
 * asking it to translate would produce fluent, confident, wrong Bangla-adjacent text — and
 * "are you bleeding?" is precisely the sentence where a plausible-looking wrong answer is
 * more dangerous than no answer at all.
 *
 * So the contract is deliberately narrow: the model returns **ids that already exist** in the
 * bundled phrasebook, and anything it invents is discarded. The worst case is that it picks
 * the wrong question, which the volunteer can see and correct — not that it puts words in
 * someone's mouth.
 *
 * When the model is not installed this falls straight through to [PhraseSearch], so the
 * feature never depends on the 2.5 GB download.
 */
object PhraseFinder {

    const val SYSTEM = """
You match a rescuer's free-text description to questions from a fixed phrasebook.

Rules:
- Reply with ONLY a comma-separated list of phrase ids from the list you are given.
- Use at most 5 ids, most important first.
- Never invent an id. Never add any other text, punctuation or explanation.
- Never translate anything. You are selecting, not writing.
- If nothing fits, reply with the single word: none
"""

    /** The catalogue handed to the model: id plus the English line, nothing else. */
    fun catalogue(phrases: List<Phrase>): String =
        phrases.joinToString("\n") { "${it.id} = ${it.en}" }

    /**
     * Parse the model's reply back into real phrases.
     *
     * Every id is checked against the book, so a hallucinated id simply disappears rather
     * than becoming a blank card in front of a frightened person.
     */
    fun parse(reply: String, book: PhrasebookData, limit: Int = 5): List<Phrase> {
        val cleaned = reply.trim().lowercase()
        if (cleaned.isBlank() || cleaned.startsWith("none")) return emptyList()
        return cleaned
            .split(',', '\n', ' ')
            .map { it.trim().trim('.', ';', '"', '\'', '`', '-') }
            .filter { it.isNotBlank() }
            .distinct()
            .mapNotNull { book.phrase(it) }
            .take(limit)
    }

    /**
     * Find phrases for [query].
     *
     * [generate] is the model call, or null when no model is installed. Either way a result
     * comes back: an empty model reply falls through to keyword search rather than leaving
     * the volunteer with nothing.
     */
    fun find(
        book: PhrasebookData,
        query: String,
        limit: Int = 5,
        // Last so it can be passed as a trailing lambda at the call sites.
        generate: ((system: String, user: String) -> String)? = null,
    ): Result {
        if (query.isBlank()) return Result(emptyList(), Source.NONE)

        if (generate != null) {
            val reply = runCatching {
                generate(
                    SYSTEM + "\n\nPhrasebook:\n" + catalogue(book.allPhrases),
                    query,
                )
            }.getOrNull()
            val picked = reply?.let { parse(it, book, limit) }.orEmpty()
            if (picked.isNotEmpty()) return Result(picked, Source.MODEL)
        }

        return Result(PhraseSearch.search(book.allPhrases, query, limit), Source.KEYWORDS)
    }

    enum class Source { MODEL, KEYWORDS, NONE }

    data class Result(val phrases: List<Phrase>, val source: Source)
}
