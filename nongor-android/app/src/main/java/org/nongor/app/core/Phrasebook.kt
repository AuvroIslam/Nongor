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
    /** verified · community · unverified */
    val v: String = "unverified",
) {
    val isVerified: Boolean get() = v == "verified"
}

data class Category(val id: String, val en: String, val bn: String, val icon: String)

data class BodyPart(val id: String, val en: String, val bn: String)

data class Phrase(
    val id: String,
    val cat: String,
    val icon: String,
    val reply: String,
    val en: String,
    val bn: String,
    val tags: List<String> = emptyList(),
    @SerializedName("sign_bn") val signBn: String? = null,
    val t: Map<String, Translation> = emptyMap(),
) {
    val replyKind: ReplyKind get() = ReplyKind.of(reply)
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

data class PhrasebookData(
    val version: String,
    val languages: List<LangInfo>,
    val categories: List<Category>,
    val phrases: List<Phrase>,
    @SerializedName("body_parts") val bodyParts: List<BodyPart>,
    @SerializedName("triage_flow") val triageFlow: List<String>,
) {
    private val byId: Map<String, Phrase> by lazy { phrases.associateBy { it.id } }

    fun phrase(id: String): Phrase? = byId[id]

    fun inCategory(catId: String): List<Phrase> = phrases.filter { it.cat == catId }

    fun language(code: String): LangInfo? = languages.firstOrNull { it.code == code }

    /** Languages a volunteer can actually pick, i.e. everything except the two authored ones. */
    fun targetLanguages(): List<LangInfo> = languages.filter { !it.isAuthored }

    /** How many phrases carry a line in [code]. Shown honestly in the picker. */
    fun coverage(code: String): Int = phrases.count { it.t[code]?.beng?.isNotBlank() == true }

    fun triagePhrases(): List<Phrase> = triageFlow.mapNotNull { byId[it] }
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
        val tagTokens = phrase.tags.flatMap { tokens(it) }.toSet()
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
