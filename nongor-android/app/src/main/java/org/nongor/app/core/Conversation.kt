package org.nongor.app.core

/**
 * A structured cross-language conversation.
 *
 * Because every reply is a tap on a known option rather than free speech, the answers come
 * back as machine-readable codes. That is the whole point: a volunteer who speaks no Chakma
 * still ends up holding a structured record — "bleeding: yes, breathing: no, 4 people, one
 * child" — that can be triaged, summarised and sent over the mesh without a translator.
 */

/** How urgent a case is. Shared with rescue triage so both features rank the same way. */
enum class Priority(val rank: Int) {
    CRITICAL(3),
    HIGH(2),
    MODERATE(1),
    LOW(0),
    ;

    fun labelEn(): String = when (this) {
        CRITICAL -> "Critical"
        HIGH -> "High"
        MODERATE -> "Moderate"
        LOW -> "Low"
    }

    fun labelBn(): String = when (this) {
        CRITICAL -> "সংকটাপন্ন"
        HIGH -> "জরুরি"
        MODERATE -> "মাঝারি"
        LOW -> "কম"
    }
}

/** One answer, keyed by the question it answers. */
data class Reply(
    val phraseId: String,
    val kind: ReplyKind,
    /** Machine-readable: "yes", "no", "unknown", "num:4", "scale:3", "body:chest", … */
    val code: String,
    val en: String,
    val bn: String,
) {
    val isYes: Boolean get() = code == "yes"
    val isNo: Boolean get() = code == "no"

    fun intValue(): Int? = code.substringAfter(':', "").toIntOrNull()
}

/** A reason the case was ranked where it was. Always shown — never a bare score. */
data class Signal(val en: String, val bn: String, val priority: Priority)

data class Assessment(
    val priority: Priority,
    val signals: List<Signal>,
    val peopleCount: Int?,
    val hasVulnerable: Boolean,
) {
    /** Nothing was asked yet. */
    val isEmpty: Boolean get() = signals.isEmpty() && peopleCount == null
}

object ConversationTriage {

    /**
     * Rank a set of replies.
     *
     * Deliberately a transparent rule table rather than a score the user cannot audit:
     * a responder has to be able to disagree with the app, and they can only do that if
     * they can see exactly which answer caused the ranking.
     */
    fun assess(replies: Map<String, Reply>): Assessment {
        val signals = mutableListOf<Signal>()

        fun yes(id: String) = replies[id]?.isYes == true
        fun no(id: String) = replies[id]?.isNo == true

        if (no("breathe")) {
            signals += Signal(
                "Cannot breathe normally", "স্বাভাবিক শ্বাস নিতে পারছেন না", Priority.CRITICAL,
            )
        }
        if (yes("unconscious")) {
            signals += Signal("Lost consciousness", "জ্ঞান হারিয়েছিলেন", Priority.CRITICAL)
        }
        if (yes("bleeding")) {
            signals += Signal("Bleeding", "রক্তপাত হচ্ছে", Priority.CRITICAL)
        }
        if (yes("trapped")) {
            signals += Signal("Someone is trapped", "কেউ আটকে আছে", Priority.CRITICAL)
        }
        if (yes("animal_bite")) {
            signals += Signal("Snake or animal bite", "সাপ বা প্রাণীর কামড়", Priority.HIGH)
        }
        if (yes("pregnant")) {
            signals += Signal("Pregnant", "গর্ভবতী", Priority.HIGH)
        }
        if (yes("injured") && no("can_walk")) {
            signals += Signal("Injured and cannot walk", "আহত এবং হাঁটতে পারছেন না", Priority.HIGH)
        }
        replies["pain_scale"]?.intValue()?.let { level ->
            if (level >= 4) {
                signals += Signal("Severe pain reported", "তীব্র ব্যথা জানিয়েছেন", Priority.HIGH)
            }
        }
        if (no("move_limbs")) {
            signals += Signal("Cannot move arms or legs", "হাত-পা নাড়াতে পারছেন না", Priority.HIGH)
        }
        if (yes("medicine")) {
            signals += Signal("Needs daily medicine", "প্রতিদিনের ওষুধ দরকার", Priority.MODERATE)
        }
        if (yes("injured") && signals.none { it.priority.rank >= Priority.HIGH.rank }) {
            signals += Signal("Injured", "আহত", Priority.MODERATE)
        }
        if (yes("need_water")) {
            signals += Signal("Needs drinking water", "খাবার পানি দরকার", Priority.MODERATE)
        }
        if (yes("child_alone")) {
            signals += Signal("Unaccompanied child", "সঙ্গীহীন শিশু", Priority.HIGH)
        }
        if (yes("family_missing")) {
            signals += Signal("Family member missing", "পরিবারের সদস্য নিখোঁজ", Priority.HIGH)
        }

        val vulnerable = yes("vulnerable") || yes("child_alone") || yes("pregnant")
        if (yes("vulnerable")) {
            signals += Signal(
                "Children, elderly or disabled present", "শিশু, বয়স্ক বা প্রতিবন্ধী আছেন", Priority.MODERATE,
            )
        }

        val count = replies["how_many"]?.intValue()

        var priority = signals.maxByOrNull { it.priority.rank }?.priority ?: Priority.LOW
        // A large group or a vulnerable person does not by itself make a case critical, but
        // it should never be left at the bottom of a queue either.
        if (vulnerable && priority == Priority.LOW) priority = Priority.MODERATE
        if ((count ?: 0) >= 5 && priority == Priority.LOW) priority = Priority.MODERATE

        return Assessment(
            priority = priority,
            signals = signals.sortedByDescending { it.priority.rank },
            peopleCount = count,
            hasVulnerable = vulnerable,
        )
    }

    /**
     * Render the conversation as a handover note.
     *
     * Every value here is copied straight from a tapped answer — nothing is inferred,
     * summarised or reworded, so what the responder reads is exactly what the person said.
     */
    fun summarise(
        book: PhrasebookData,
        replies: Map<String, Reply>,
        bangla: Boolean,
        languageName: String?,
    ): String {
        if (replies.isEmpty()) return ""
        val sb = StringBuilder()
        if (languageName != null) {
            sb.append(
                if (bangla) "ভাষা: $languageName\n" else "Language: $languageName\n",
            )
        }
        for (phrase in book.triagePhrases()) {
            val reply = replies[phrase.id] ?: continue
            val q = if (bangla) phrase.bn else phrase.en
            val a = if (bangla) reply.bn else reply.en
            sb.append("• ").append(q).append(' ').append("→ ").append(a).append('\n')
        }
        // Anything answered outside the guided flow still belongs in the note.
        val extra = replies.keys - book.flow.toSet()
        for (id in extra) {
            val phrase = book.phrase(id) ?: continue
            val reply = replies[id] ?: continue
            sb.append("• ")
                .append(if (bangla) phrase.bn else phrase.en)
                .append(" → ")
                .append(if (bangla) reply.bn else reply.en)
                .append('\n')
        }
        return sb.toString().trimEnd()
    }
}
