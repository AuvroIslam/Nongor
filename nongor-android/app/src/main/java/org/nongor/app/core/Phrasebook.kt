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
    fun coverage(code: String): Int =
        allPhrases.count { it.translations[code]?.hasLine == true }

    fun triagePhrases(): List<Phrase> = flow.mapNotNull { byId[it] }
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
