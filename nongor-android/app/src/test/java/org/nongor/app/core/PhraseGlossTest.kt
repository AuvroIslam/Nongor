package org.nongor.app.core

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The key-word fallback for questions the corpus never covered.
 *
 * Rohingya, Kokborok and Santali have no corpus line for eight of the ten guided questions.
 * The gloss is what stands between a volunteer and a blank card there, so these tests pin
 * both that it produces something useful and that it never overstates what it is.
 */
class PhraseGlossTest {

    // Loaded through the real deserialiser, against the real asset — a gloss that works on a
    // hand-built Kotlin object but not on the shipped JSON would be worth nothing.
    private val book: PhrasebookData by lazy {
        Gson().fromJson(
            File("src/main/assets/phrasebook.json").readText(),
            PhrasebookData::class.java,
        )
    }

    private fun phrase(id: String) = book.phrase(id)!!

    @Test fun glossCoversTheQuestionsWithNoCorpusLine() {
        // "Are you injured?" has no Rohingya line, but WOUND and PAIN are both sayable.
        val g = book.keyWords(phrase("injured"), "rhg")
        assertTrue("expected at least one usable word, got $g", g.isNotEmpty())
        assertTrue(g.all { it.word.isNotBlank() && it.en.isNotBlank() })
    }

    @Test fun aRealTranslationAlwaysWins() {
        // "understand" does have a Chakma line, so the gloss must stay out of the way.
        assertTrue(
            "a phrase with a corpus line must not be glossed",
            book.keyWords(phrase("understand"), "ccp").isEmpty(),
        )
    }

    @Test fun neverInventsWordsForALanguageWithNoLexicon() {
        // Chakma has no GATITOS vocabulary at all — the honest answer is nothing.
        assertTrue(book.keyWords(phrase("injured"), "ccp").isEmpty())
    }

    @Test fun corpusSentencesAreNeverUsedAsWords() {
        // The Chakma entry attached to "water" is the sentence "Can I get a glass of water?".
        // Showing that as the *word* water would be a lie the volunteer cannot detect.
        book.allPhrases.forEach { p ->
            book.keyWords(p, "rhg").forEach { g ->
                assertTrue(
                    "gloss word '${g.word}' looks like a sentence",
                    g.word.length < 40 && !g.word.contains("?"),
                )
            }
        }
    }

    @Test fun glossIsCapped() {
        book.allPhrases.forEach { p ->
            listOf("rhg", "trp", "sat").forEach { lang ->
                assertTrue(book.keyWords(p, lang).size <= 4)
            }
        }
    }

    @Test fun unknownLanguageGlossesToNothing() {
        assertTrue(book.keyWords(phrase("injured"), "zz").isEmpty())
    }

    @Test fun guidedFlowPutsTranslatedQuestionsFirst() {
        val ordered = book.triagePhrasesFor("ccp")
        assertEquals("the flow must not lose questions", book.flow.size, ordered.size)
        val firstUntranslated = ordered.indexOfFirst { it.t?.get("ccp") == null }
        val lastTranslated = ordered.indexOfLast { it.t?.get("ccp") != null }
        assertTrue(
            "every translated question should precede every untranslated one",
            lastTranslated < firstUntranslated,
        )
    }

    @Test fun guidedFlowIsNeverEmptied() {
        // Filtering to translated-only would leave these three languages with nothing at all,
        // which would delete the feature rather than improve it — the pictogram completes the
        // exchange with no line whatsoever.
        listOf("rhg", "trp", "sat", "ccp", "mrh", "grt").forEach { lang ->
            assertEquals(book.flow.size, book.triagePhrasesFor(lang).size)
        }
        assertEquals(book.flow.size, book.triagePhrasesFor(null).size)
    }
}
