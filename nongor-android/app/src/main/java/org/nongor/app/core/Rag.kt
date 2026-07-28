package org.nongor.app.core

/**
 * First-aid RAG with citations — Kotlin port of nongor/core/rag.py.
 * KeywordRetriever (FTS-style) here; an embedding retriever can be added later.
 */
object Rag {

    private val word = Regex("[a-z0-9\\u0980-\\u09FF]+")   // includes Bangla block
    private val stopwords = setOf(
        "the", "a", "an", "i", "you", "we", "my", "our", "is", "are", "am", "be", "do", "does",
        "did", "to", "of", "and", "or", "in", "on", "at", "for", "how", "what", "can", "should",
        "it", "this", "that", "with", "from", "help", "please", "need", "have", "has", "get",
        "got", "was", "were", "if", "so", "but", "not",
    )

    private fun contentTokens(text: String): List<String> =
        word.findAll(text.lowercase()).map { it.value }.filter { it !in stopwords }.toList()

    class KeywordRetriever(private val chunks: List<KbChunk>) {
        fun search(query: String, k: Int = 3, hazard: String? = null): List<KbChunk> {
            val q = contentTokens(query).toSet()
            if (q.isEmpty()) return emptyList()
            return chunks.filter { hazard == null || it.hazard == hazard }
                .map { c ->
                    val tagTokens = c.tags.flatMap { contentTokens(it) }
                    val hay = contentTokens(c.textMd) + tagTokens + tagTokens + tagTokens  // weight tags
                    hay.count { it in q } to c
                }
                .filter { it.first > 0 }
                .sortedByDescending { it.first }
                .take(k).map { it.second }
        }
    }

    private val lifeThreat = listOf("not breathing", "unconscious", "heavy bleeding",
        "bleeding heavily", "spurting", "choking", "শ্বাস", "অজ্ঞান", "রক্তক্ষরণ")

    fun redFlag(query: String, chunks: List<KbChunk>): Boolean {
        val t = query.lowercase()
        if (lifeThreat.any { it in t }) return true
        return chunks.any { c -> c.flags.any { it.lowercase() in t } }
    }

    private fun buildContext(chunks: List<KbChunk>): String =
        chunks.mapIndexed { i, c -> "[${i + 1}] (${c.source}) ${c.textMd}" }.joinToString("\n")

    data class Citation(val n: Int, val pack: String, val source: String, val chunkId: String)
    data class Answer(
        val answer: String, val citations: List<Citation>, val redFlag: Boolean,
        val usedChunks: List<String>,
    )

    fun firstAidAnswer(
        query: String, retriever: KeywordRetriever, gemma: LlmEngine? = null,
        k: Int = 3, hazard: String? = null, searchQuery: String = query,
        imagePath: String? = null,
    ): Answer {
        // Retrieval matches on searchQuery (may be an English translation); the answer is written
        // from the original query, so Gemma still sees the user's real wording and language.
        val chunks = retriever.search(searchQuery, k, hazard)
        val citations = chunks.mapIndexed { i, c -> Citation(i + 1, c.pack, c.source, c.id) }
        val flag = redFlag(query, chunks)
        if (chunks.isEmpty()) {
            return Answer(
                "I don't have specific guidance for that. Please seek professional help. " +
                    Safety.DISCLAIMER, emptyList(), flag, emptyList())
        }
        val rawAnswer = if (gemma == null) {
            chunks.mapIndexed { i, c -> "${c.textMd.trim()} [${i + 1}]" }.joinToString(" ") +
                "\n" + Safety.DISCLAIMER
        } else {
            val ctx = buildContext(chunks)
            // Passages are trusted (bundled packs); the user query is not, so fence it as data.
            val user = "[PASSAGES]\n$ctx\n[USER]\n${Safety.wrapAsData(query)}"
            // An optional photo lets the model see the situation; the steps still come only from the
            // passages. imagePath == null falls back to text, so this one call covers both cases.
            Safety.ensureDisclaimer(
                gemma.generate(Prompts.FIRST_AID_SYSTEM, user, imagePath, temperature = 0.4, maxTokens = 400))
        }
        // Only [1]..[N] are real passages. The model sometimes emits a marker past that range
        // (e.g. "[4]" when 3 passages were retrieved); leaving it in the text would show the user
        // a citation that points to nothing, so strip out-of-range markers before display.
        val answer = stripOutOfRangeCitations(rawAnswer, chunks.size)
        // Retrieval casts a wide net, so some retrieved passages are irrelevant and the answer
        // never cites them. Only list the ones it actually cited — otherwise we imply the advice
        // came from, say, a snakebite guideline. A refusal cites nothing, so it lists nothing.
        val cited = citedNumbers(answer)
        val shown = citations.filter { it.n in cited }
        return Answer(answer, shown, flag, chunks.filterIndexed { i, _ -> (i + 1) in cited }.map { it.id })
    }

    /** Passage numbers the answer actually references, e.g. "[2]" -> 2. */
    private fun citedNumbers(answer: String): Set<Int> =
        Regex("\\[(\\d+)\\]").findAll(answer).mapNotNull { it.groupValues[1].toIntOrNull() }.toSet()

    /** Remove citation markers that point outside the real passage range [1..maxN]. */
    internal fun stripOutOfRangeCitations(answer: String, maxN: Int): String =
        Regex("\\s*\\[(\\d+)\\]").replace(answer) { m ->
            val n = m.groupValues[1].toIntOrNull()
            if (n != null && n in 1..maxN) m.value else ""
        }
}
