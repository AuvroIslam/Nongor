package org.nongor.app.ui.translate

import org.nongor.app.core.Phrase
import org.nongor.app.core.PhrasebookData
import org.nongor.app.core.ReplyKind

/**
 * One tappable answer.
 *
 * The buttons are big and few on purpose: they are pressed by someone who is cold, wet,
 * frightened and possibly holding a child, on a phone held out by a stranger.
 */
data class ReplyOption(
    val code: String,
    val en: String,
    val bn: String,
    val icon: String? = null,
)

/** The answers offered for a phrase, derived from its reply kind. */
fun replyOptions(book: PhrasebookData, phrase: Phrase): List<ReplyOption> =
    when (phrase.replyKind) {
        ReplyKind.YES_NO -> listOf(
            ReplyOption("yes", "Yes", "হ্যাঁ", "yes"),
            ReplyOption("no", "No", "না", "no"),
            ReplyOption("unknown", "Don't know", "জানি না"),
        )

        ReplyKind.ACK -> listOf(
            ReplyOption("ack", "Understood", "বুঝেছি", "yes"),
            ReplyOption("repeat", "Show again", "আবার দেখান"),
            ReplyOption("no", "No", "না", "no"),
        )

        ReplyKind.NUMBER -> (1..9).map {
            ReplyOption("num:$it", it.toString(), it.toString())
        } + ReplyOption("num:10", "10 or more", "১০ বা বেশি")

        ReplyKind.SCALE -> listOf(
            ReplyOption("scale:1", "A little", "একটু"),
            ReplyOption("scale:2", "Some", "কিছুটা"),
            ReplyOption("scale:3", "Bad", "বেশ খারাপ"),
            ReplyOption("scale:4", "Very bad", "খুব খারাপ"),
            ReplyOption("scale:5", "Unbearable", "অসহ্য"),
        )

        ReplyKind.BODY_PART -> book.bodyParts.map {
            ReplyOption("body:${it.id}", it.en, it.bn, "person_pin")
        }

        ReplyKind.HOURS -> listOf(
            ReplyOption("hours:6", "Within 6 hours", "৬ ঘণ্টার মধ্যে"),
            ReplyOption("hours:24", "Within a day", "একদিনের মধ্যে"),
            ReplyOption("hours:48", "More than a day ago", "একদিনের বেশি আগে"),
        )

        ReplyKind.DISTANCE -> listOf(
            ReplyOption("dist:1", "Very close", "খুব কাছে"),
            ReplyOption("dist:5", "A short walk", "একটু হাঁটা পথ"),
            ReplyOption("dist:20", "Far", "অনেক দূর"),
        )

        // Free text is typed by the volunteer after hearing or seeing the answer, and
        // the pictogram card still shows so the other person knows what was asked.
        ReplyKind.TEXT, ReplyKind.NONE -> emptyList()
    }
