package org.nongor.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The model is allowed to *choose* phrases and nothing else. These tests pin that boundary,
 * because the failure they guard against — a hallucinated id becoming a blank or invented
 * question in front of someone in distress — would not show up as a crash.
 */
class PhraseFinderTest {

    private val book = PhrasebookData(
        version = "test",
        languages = listOf(LangInfo("bn", "Bangla", "বাংলা", "authored")),
        categories = listOf(Category("medical", "Medical", "চিকিৎসা", "healing")),
        phrases = listOf(
            Phrase("bleeding", "medical", "blood", "yesno", "Are you bleeding?", "রক্তপাত?",
                tags = listOf("blood", "bleeding")),
            Phrase("can_walk", "medical", "walk", "yesno", "Can you walk?", "হাঁটতে পারেন?",
                tags = listOf("walk")),
            Phrase("breathe", "medical", "air", "yesno", "Can you breathe?", "শ্বাস নিতে পারেন?",
                tags = listOf("breath", "breathing")),
        ),
        bodyParts = emptyList(),
        triageFlow = emptyList(),
    )

    @Test
    fun `parses a clean list of ids`() {
        val hits = PhraseFinder.parse("bleeding, can_walk", book)
        assertEquals(listOf("bleeding", "can_walk"), hits.map { it.id })
    }

    /** The one that matters: an id the phrasebook does not contain must vanish. */
    @Test
    fun `discards ids the model invented`() {
        val hits = PhraseFinder.parse("bleeding, ask_about_helicopter, can_walk", book)
        assertEquals(listOf("bleeding", "can_walk"), hits.map { it.id })
    }

    @Test
    fun `survives the model adding chatter around the answer`() {
        val hits = PhraseFinder.parse("  BLEEDING,  breathe.  ", book)
        assertEquals(listOf("bleeding", "breathe"), hits.map { it.id })
    }

    @Test
    fun `none means none`() {
        assertTrue(PhraseFinder.parse("none", book).isEmpty())
        assertTrue(PhraseFinder.parse("", book).isEmpty())
    }

    @Test
    fun `duplicate ids are collapsed`() {
        assertEquals(1, PhraseFinder.parse("bleeding, bleeding, bleeding", book).size)
    }

    @Test
    fun `respects the limit`() {
        assertEquals(2, PhraseFinder.parse("bleeding, can_walk, breathe", book, limit = 2).size)
    }

    @Test
    fun `uses the model when it returns something usable`() {
        val r = PhraseFinder.find(book, "he is bleeding") { _, _ -> "bleeding" }
        assertEquals(PhraseFinder.Source.MODEL, r.source)
        assertEquals(listOf("bleeding"), r.phrases.map { it.id })
    }

    /** No model installed is the normal case, not an error case. */
    @Test
    fun `falls back to keyword search with no model`() {
        val r = PhraseFinder.find(book, "bleeding", generate = null)
        assertEquals(PhraseFinder.Source.KEYWORDS, r.source)
        assertEquals(listOf("bleeding"), r.phrases.map { it.id })
    }

    @Test
    fun `falls back when the model returns nothing useful`() {
        val r = PhraseFinder.find(book, "bleeding") { _, _ -> "i'm sorry, i cannot help with that" }
        assertEquals(PhraseFinder.Source.KEYWORDS, r.source)
        assertEquals(listOf("bleeding"), r.phrases.map { it.id })
    }

    /** A model that throws must not take the screen down. */
    @Test
    fun `falls back when the model throws`() {
        val r = PhraseFinder.find(book, "bleeding") { _, _ -> error("engine died") }
        assertEquals(PhraseFinder.Source.KEYWORDS, r.source)
        assertEquals(listOf("bleeding"), r.phrases.map { it.id })
    }

    @Test
    fun `a blank query asks the model nothing`() {
        var called = false
        val r = PhraseFinder.find(book, "   ") { _, _ -> called = true; "bleeding" }
        assertTrue(r.phrases.isEmpty())
        assertEquals(PhraseFinder.Source.NONE, r.source)
        assertTrue("the model should not be woken for an empty box", !called)
    }

    @Test
    fun `the catalogue exposes ids and english only`() {
        val cat = PhraseFinder.catalogue(book.allPhrases)
        assertTrue(cat.contains("bleeding = Are you bleeding?"))
        // Bangla is not sent: the model is selecting, not translating.
        assertTrue(!cat.contains("রক্তপাত"))
    }
}
