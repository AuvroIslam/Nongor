package org.nongor.app.core

import com.google.gson.annotations.SerializedName

/**
 * The emergency phrasebook.
 *
 * The design rule for this whole feature: **the conversation must still work when the
 * translation does not exist.** Bangla and English lines are authored; minority-language
 * lines are a community seed that may be absent or unverified. So every phrase also carries
 * a pictogram and a described sign-language gesture, and every question has a structured
 * reply kind (yes/no, a number, a body part…) that the other person can answer by tapping —
 * without either side reading a word of the other's language.
 */

data class LangInfo(
    val code: String,
    val name: String,
    val native: String,
    /** authored · seed · empty · gesture */
    val status: String,
    val region: String? = null,
) {
    val isAuthored: Boolean get() = status == "authored"
    val isGesture: Boolean get() = status == "gesture"
}

data class Translation(
    /** The line written in Bengali script, so a Bangla reader can sound it out. */
    val beng: String? = null,
    /** Roman transliteration. */
    val latn: String? = null,
    /** verified · corpus · community · unverified. Nullable: see the note on [Phrase]. */
    val v: String? = null,
    /** Which published source this line came from, e.g. "MELD". */
    val src: String? = null,
    /**
     * The English sentence this line was actually translated from.
     *
     * A corpus gives you a real sentence by a real speaker, but not necessarily *our*
     * sentence — the closest line to "Do you need help?" may be "Can I help you?". Recording
     * the original means the app can show the volunteer what the line literally says instead
     * of quietly implying it matches the question above it.
     */
    @SerializedName("src_en") val srcEn: String? = null,
) {
    /**
     * Anything not explicitly `verified` is treated as unverified — including a missing
     * value. The safe default has to be the pessimistic one: this flag decides whether the
     * app shows a life-critical line as trustworthy.
     */
    val isVerified: Boolean get() = v == "verified"

    /**
     * From a published, citable parallel corpus collected from native speakers.
     *
     * Weaker than [isVerified] — nobody has confirmed this exact sentence is right for this
     * exact emergency — but far stronger than a guess, and the app says which it is.
     */
    val isFromCorpus: Boolean get() = v == "corpus"

    /**
     * The line to put in front of the other person, in whichever script it exists.
     *
     * Not every language here is written in Bengali script: Rohingya, Kokborok and Santali
     * are published in Latin, and for those the Latin *is* the writing, not a pronunciation
     * hint. Treating a missing [beng] as "no translation" would have hidden 150 perfectly
     * good professional translations behind an empty card.
     */
    val display: String? get() = beng?.takeIf { it.isNotBlank() } ?: latn?.takeIf { it.isNotBlank() }

    /** True when there is any line at all to show. */
    val hasLine: Boolean get() = display != null

    /** Shown small under [display] — only when it adds something. */
    val pronunciation: String? get() = latn?.takeIf { it.isNotBlank() && it != display }
}

data class Category(val id: String, val en: String, val bn: String, val icon: String)

data class BodyPart(val id: String, val en: String, val bn: String)

/**
 * One phrase.
 *
 * **Every collection here is nullable, and that is deliberate.** Gson populates fields by
 * reflection and does not run the Kotlin constructor, so a Kotlin default value is *not*
 * applied when the JSON simply omits the key — the field is left null and the non-null type
 * is a lie the compiler cannot catch. Most phrases have no `t` block at all, so declaring it
 * `Map<String, Translation> = emptyMap()` crashed the moment the screen read it.
 *
 * The fix is to admit the nullability at the boundary and normalise it in one place, via
 * [tagList] and [translations], which the rest of the app uses instead.
 */
/**
 * One content word of a phrase, in the other person's language.
 *
 * [en] is the English concept, [word] the line the other person reads.
 */
data class GlossWord(val en: String, val word: String)

data class Phrase(
    val id: String,
    val cat: String,
    val icon: String,
    val reply: String,
    val en: String,
    val bn: String,
    val tags: List<String>? = null,
    @SerializedName("sign_bn") val signBn: String? = null,
    val t: Map<String, Translation>? = null,
) {
    val replyKind: ReplyKind get() = ReplyKind.of(reply)

    /** Search terms, never null. */
    val tagList: List<String> get() = tags ?: emptyList()

    /** Minority-language lines, never null. */
    val translations: Map<String, Translation> get() = t ?: emptyMap()

    /** The line for [langCode], or null when this language has no entry for this phrase. */
    fun translation(langCode: String?): Translation? =
        langCode?.let { translations[it] }
}

/** How the other person answers. Chosen so the answer is machine-readable, not free text. */
enum class ReplyKind {
    YES_NO,
    /** A statement — the reply is only "understood" / "say it again". */
    ACK,
    NUMBER,
    /** 1..5 pain scale. */
    SCALE,
    BODY_PART,
    /** Rough buckets of time since eating. */
    HOURS,
    /** Rough distance buckets. */
    DISTANCE,
    /** Free text, typed by the volunteer after hearing the answer. */
    TEXT,
    NONE,
    ;

    companion object {
        fun of(raw: String): ReplyKind = when (raw) {
            "yesno" -> YES_NO
            "ack" -> ACK
            "number" -> NUMBER
            "scale" -> SCALE
            "bodypart" -> BODY_PART
            "hours" -> HOURS
            "distance" -> DISTANCE
            "text" -> TEXT
            else -> NONE
        }
    }
}

/**
 * The whole phrasebook.
 *
 * Same rule as [Phrase]: every list is nullable because Gson fills these by reflection and a
 * truncated or hand-edited asset would otherwise hand the UI a null typed as non-null. A
 * phrasebook missing a section should degrade to an empty section, not take the screen down —
 * this file is meant to be edited by contributors who do not run the app.
 */
data class PhrasebookData(
    val version: String? = null,
    private val languages: List<LangInfo>? = null,
    private val categories: List<Category>? = null,
    private val phrases: List<Phrase>? = null,
    @SerializedName("body_parts") private val bodyParts: List<BodyPart>? = null,
    @SerializedName("triage_flow") private val triageFlow: List<String>? = null,
) {
    val allLanguages: List<LangInfo> get() = languages ?: emptyList()
    val allCategories: List<Category> get() = categories ?: emptyList()
    val allPhrases: List<Phrase> get() = phrases ?: emptyList()
    val allBodyParts: List<BodyPart> get() = bodyParts ?: emptyList()
    val flow: List<String> get() = triageFlow ?: emptyList()

    private val byId: Map<String, Phrase> by lazy { allPhrases.associateBy { it.id } }

    fun phrase(id: String): Phrase? = byId[id]

    fun inCategory(catId: String): List<Phrase> = allPhrases.filter { it.cat == catId }

    fun language(code: String): LangInfo? = allLanguages.firstOrNull { it.code == code }

    /** Languages a volunteer can actually pick, i.e. everything except the two authored ones. */
    fun targetLanguages(): List<LangInfo> = allLanguages.filter { !it.isAuthored }

    /** How many phrases carry a written line in [code], in any script. Shown in the picker. */
    private companion object {
        val ESSENTIAL_IDS = listOf(
            "help_need", "safe_now", "injured", "bleeding", "trapped", "need_water",
        )
    }

    fun coverage(code: String): Int =
        allPhrases.count { it.translations[code]?.hasLine == true }

    fun triagePhrases(): List<Phrase> = flow.mapNotNull { byId[it] }

    /**
     * The single-word lexicon for a language, built from the GATITOS entries only.
     *
     * Deliberately excludes corpus sentences. The Chakma line attached to the word "water" is
     * "Can I get a glass of water?" - a real sentence, but showing it as the *word* water would
     * be a lie the volunteer cannot detect.
     */
    private fun lexicon(lang: String): Map<String, String> =
        lexiconCache.getOrPut(lang) {
            val out = HashMap<String, String>()
            allPhrases.forEach { p ->
                val t = p.t?.get(lang) ?: return@forEach
                if (t.src != "GATITOS") return@forEach
                val key = (t.srcEn ?: p.en).trim().lowercase()
                val word = t.latn ?: t.beng
                if (key.isNotEmpty() && !word.isNullOrBlank()) out[key] = word
            }
            out
        }

    private val lexiconCache = HashMap<String, Map<String, String>>()

    /**
     * The content words of [phrase] that we can actually say in [lang].
     *
     * This is the honest fallback for a phrase the corpus never covered. It is **not** a
     * translation and the UI must never present it as one: a word list is not grammar, and
     * "you injured?" is not a sentence in any of these languages. But a volunteer who can point
     * at the pictogram *and* show the word for BLOOD has far more than one who can do neither,
     * and for Rohingya, Kokborok and Santali - which have no corpus line for eight of the ten
     * guided questions - this is the difference between something and nothing.
     *
     * Empty when we cannot say any of it, which is a fine answer: the pictogram still works.
     */
    fun keyWords(phrase: Phrase, lang: String): List<GlossWord> {
        if (phrase.t?.get(lang) != null) return emptyList()   // a real line exists; use that
        val lex = lexicon(lang)
        if (lex.isEmpty()) return emptyList()
        val out = LinkedHashMap<String, String>()
        concepts(phrase).forEach { c ->
            val w = lex[c]
            if (w != null && out.size < MAX_GLOSS) out[c] = w
        }
        return out.map { (en, word) -> GlossWord(en, word) }
    }

    /** English concepts a phrase is about, expanded through a small crisis synonym table. */
    private fun concepts(phrase: Phrase): List<String> {
        val words = WORD_RE.findAll(phrase.en.lowercase()).map { it.value }.toMutableList()
        phrase.tagList.forEach { tag ->
            WORD_RE.findAll(tag.lowercase()).forEach { words.add(it.value) }
        }
        val out = ArrayList<String>()
        words.forEach { w ->
            if (w in STOPWORDS) return@forEach
            (SYNONYMS[w] ?: listOf(w)).forEach { if (it !in out) out.add(it) }
        }
        return out
    }

    /**
     * The guided flow, with the questions we can actually say in [lang] first.
     *
     * Not filtered. Filtering to translated-only would leave Rohingya, Kokborok and Santali with
     * an empty flow and the rest with two questions, which would delete the feature rather than
     * improve it - the pictogram and tap-reply complete the exchange with no line at all. So the
     * lines we do have simply lead.
     */
    fun triagePhrasesFor(lang: String?): List<Phrase> {
        val all = triagePhrases()
        if (lang.isNullOrBlank()) return all
        val (translated, rest) = all.partition { it.t?.get(lang) != null }
        return translated + rest
    }



    /**
     * The handful you reach for before you have worked out what is going on.
     *
     * Not the guided flow — that is a sequence you commit to. These are the standalone
     * openers: establish that you are help, find out if anyone is hurt or stuck, offer water.
     * They sit on the screen rather than behind a search box because in the first thirty
     * seconds of meeting a stranger you do not know what to search for.
     */
    fun essentials(): List<Phrase> = ESSENTIAL_IDS.mapNotNull { byId[it] }
}

/**
 * Search over the phrasebook.
 *
 * Pure Kotlin with no Android types so it can be unit-tested on the JVM. Matching is
 * token-based over English, Bangla and the tag list at once — a volunteer under pressure
 * types "blood" or "রক্ত" and must get the same phrase either way.
 */
object PhraseSearch {

    private val SPLIT = Regex("[^\\p{L}\\p{N}]+")

    fun tokens(text: String): List<String> =
        text.lowercase().split(SPLIT).filter { it.isNotBlank() }

    fun score(phrase: Phrase, queryTokens: List<String>): Int {
        if (queryTokens.isEmpty()) return 0
        val tagTokens = phrase.tagList.flatMap { tokens(it) }.toSet()
        val textTokens = (tokens(phrase.en) + tokens(phrase.bn)).toSet()
        var total = 0
        for (q in queryTokens) {
            var best = 0
            if (q in tagTokens) best = 4
            if (best < 3 && tagTokens.any { it.startsWith(q) }) best = 3
            if (best < 3 && q in textTokens) best = 3
            if (best < 2 && textTokens.any { it.startsWith(q) }) best = 2
            if (best < 1 && (tagTokens + textTokens).any { it.contains(q) }) best = 1
            // A query token that matches nothing at all sinks the whole phrase: searching
            // "child water" should not return every phrase that merely mentions water.
            if (best == 0) return 0
            total += best
        }
        return total
    }

    fun search(phrases: List<Phrase>, query: String, limit: Int = 20): List<Phrase> {
        val q = tokens(query)
        if (q.isEmpty()) return emptyList()
        return phrases
            .map { it to score(it, q) }
            .filter { it.second > 0 }
            .sortedWith(compareByDescending<Pair<Phrase, Int>> { it.second }.thenBy { it.first.en })
            .take(limit)
            .map { it.first }
    }
}

private val WORD_RE = Regex("[a-z]+")
private const val MAX_GLOSS = 4

/** Words that carry no meaning worth glossing. */
private val STOPWORDS = setOf(
    "do", "you", "are", "is", "the", "a", "an", "of", "to", "your", "there", "any", "i", "we",
    "can", "it", "with", "or", "does", "me", "normally", "many", "how", "and", "in", "on", "at",
    "for", "be", "have", "has", "this", "that", "am", "was", "will", "if", "not", "no",
)

/**
 * Crisis concepts mapped onto words the vocabulary actually contains.
 *
 * "Are you injured?" has no entry for "injured", but WOUND and PAIN are both in the lexicon and
 * both land the meaning when paired with the pictogram. Without this table the gloss found one
 * usable word across all ten guided questions; with it, eight of the ten are covered for the
 * three languages that have no corpus line at all.
 */
private val SYNONYMS: Map<String, List<String>> = mapOf(
    "injured" to listOf("wound", "pain"),
    "injury" to listOf("wound"),
    "wounded" to listOf("wound"),
    "bleeding" to listOf("blood"),
    "bleed" to listOf("blood"),
    "hurt" to listOf("pain"),
    "hurts" to listOf("pain"),
    "drinking" to listOf("drink"),
    "children" to listOf("child"),
    "elderly" to listOf("man", "woman"),
    "disabled" to listOf("sick"),
    "ill" to listOf("sick"),
    "people" to listOf("man", "woman", "child"),
    "person" to listOf("man", "woman"),
    "trapped" to listOf("help"),
    "buried" to listOf("help"),
    "stuck" to listOf("help"),
    "rescued" to listOf("rescue"),
    "walking" to listOf("walk"),
    "thirst" to listOf("thirsty"),
    "hunger" to listOf("hungry"),
    "boats" to listOf("boat"),
    "roads" to listOf("road"),
    "houses" to listOf("house"),
    // Nothing in the vocabulary carries these, and a wrong word is worse than none.
    "understand" to emptyList(),
    "breathe" to emptyList(),
    "breathing" to emptyList(),
)
