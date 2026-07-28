package org.nongor.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The phrasebook engine is pure Kotlin so the parts a rescuer depends on can be proved on
 * the JVM: that searching finds the right phrase, and that a set of tapped answers always
 * ranks the same way.
 */
class PhraseSearchTest {

    private val phrases = listOf(
        Phrase(
            id = "bleeding", cat = "medical", icon = "blood", reply = "yesno",
            en = "Are you bleeding?", bn = "আপনার কি রক্তপাত হচ্ছে?",
            tags = listOf("blood", "bleeding", "রক্ত"),
        ),
        Phrase(
            id = "need_water", cat = "water_food", icon = "water", reply = "yesno",
            en = "Do you need drinking water?", bn = "আপনার কি খাবার পানি দরকার?",
            tags = listOf("water", "thirsty", "পানি"),
        ),
        Phrase(
            id = "child_alone", cat = "family", icon = "child", reply = "yesno",
            en = "Is this child alone?", bn = "এই শিশুটি কি একা?",
            tags = listOf("child", "alone", "শিশু", "একা"),
        ),
    )

    @Test
    fun `finds a phrase by its english tag`() {
        val hits = PhraseSearch.search(phrases, "bleeding")
        assertEquals("bleeding", hits.first().id)
    }

    @Test
    fun `finds the same phrase by its bangla tag`() {
        val hits = PhraseSearch.search(phrases, "রক্ত")
        assertEquals("bleeding", hits.first().id)
    }

    @Test
    fun `matches on a prefix so half-typed words still work`() {
        // A volunteer under pressure rarely finishes the word.
        val hits = PhraseSearch.search(phrases, "wat")
        assertEquals("need_water", hits.first().id)
    }

    /**
     * A multi-word query must mean "all of these", not "any of these". Otherwise typing
     * "child water" returns every phrase mentioning water, which buries the one that matters.
     */
    @Test
    fun `a query token that matches nothing rejects the phrase`() {
        val hits = PhraseSearch.search(phrases, "child water")
        assertTrue(hits.isEmpty())
    }

    @Test
    fun `blank query returns nothing rather than everything`() {
        assertTrue(PhraseSearch.search(phrases, "   ").isEmpty())
    }
}

class ConversationTriageTest {

    private fun yes(id: String) = id to Reply(id, ReplyKind.YES_NO, "yes", "Yes", "হ্যাঁ")
    private fun no(id: String) = id to Reply(id, ReplyKind.YES_NO, "no", "No", "না")

    @Test
    fun `no answers means nothing is claimed`() {
        val a = ConversationTriage.assess(emptyMap())
        assertEquals(Priority.LOW, a.priority)
        assertTrue(a.isEmpty)
    }

    @Test
    fun `not breathing is critical`() {
        val a = ConversationTriage.assess(mapOf(no("breathe")))
        assertEquals(Priority.CRITICAL, a.priority)
        assertTrue(a.signals.any { it.en.contains("breathe", ignoreCase = true) })
    }

    @Test
    fun `bleeding is critical`() {
        assertEquals(Priority.CRITICAL, ConversationTriage.assess(mapOf(yes("bleeding"))).priority)
    }

    @Test
    fun `injured but walking is only moderate`() {
        val a = ConversationTriage.assess(mapOf(yes("injured"), yes("can_walk")))
        assertEquals(Priority.MODERATE, a.priority)
    }

    @Test
    fun `injured and unable to walk escalates to high`() {
        val a = ConversationTriage.assess(mapOf(yes("injured"), no("can_walk")))
        assertEquals(Priority.HIGH, a.priority)
    }

    /** Every ranking must come with the answer that caused it — never a bare score. */
    @Test
    fun `every assessment explains itself`() {
        val a = ConversationTriage.assess(mapOf(yes("bleeding"), yes("trapped")))
        assertTrue(a.signals.isNotEmpty())
        assertTrue(a.signals.all { it.en.isNotBlank() && it.bn.isNotBlank() })
    }

    @Test
    fun `signals are ordered worst first`() {
        val a = ConversationTriage.assess(mapOf(yes("bleeding"), yes("need_water")))
        assertEquals(Priority.CRITICAL, a.signals.first().priority)
    }

    @Test
    fun `a vulnerable group is never left at the bottom of the queue`() {
        val a = ConversationTriage.assess(mapOf(yes("vulnerable")))
        assertEquals(Priority.MODERATE, a.priority)
        assertTrue(a.hasVulnerable)
    }

    @Test
    fun `people count is read back from the tapped number`() {
        val replies = mapOf(
            "how_many" to Reply("how_many", ReplyKind.NUMBER, "num:7", "7", "৭"),
        )
        assertEquals(7, ConversationTriage.assess(replies).peopleCount)
    }

    @Test
    fun `severe pain raises priority but mild pain does not`() {
        fun pain(level: Int) = mapOf(
            "pain_scale" to Reply("pain_scale", ReplyKind.SCALE, "scale:$level", "$level", "$level"),
        )
        assertEquals(Priority.LOW, ConversationTriage.assess(pain(2)).priority)
        assertEquals(Priority.HIGH, ConversationTriage.assess(pain(4)).priority)
    }
}

class ReplyTest {

    @Test
    fun `structured codes parse back to values`() {
        assertEquals(4, Reply("x", ReplyKind.NUMBER, "num:4", "4", "৪").intValue())
        assertEquals(3, Reply("x", ReplyKind.SCALE, "scale:3", "3", "৩").intValue())
        assertNull(Reply("x", ReplyKind.YES_NO, "yes", "Yes", "হ্যাঁ").intValue())
    }

    @Test
    fun `reply kinds map from their asset spelling`() {
        assertEquals(ReplyKind.YES_NO, ReplyKind.of("yesno"))
        assertEquals(ReplyKind.BODY_PART, ReplyKind.of("bodypart"))
        assertEquals(ReplyKind.NONE, ReplyKind.of("something-we-never-shipped"))
    }
}

class PhrasebookDataTest {

    private val book = PhrasebookData(
        version = "test",
        languages = listOf(
            LangInfo("bn", "Bangla", "বাংলা", "authored"),
            LangInfo("en", "English", "English", "authored"),
            LangInfo("ccp", "Chakma", "চাঙমা", "seed"),
            LangInfo("mrh", "Marma", "মারমা", "empty"),
        ),
        categories = listOf(Category("words", "Single words", "একক শব্দ", "translate")),
        phrases = listOf(
            Phrase(
                id = "w_water", cat = "words", icon = "water", reply = "none",
                en = "Water", bn = "পানি",
                t = mapOf("ccp" to Translation(beng = "পানি", latn = "paani", v = "unverified")),
            ),
            Phrase(
                id = "w_food", cat = "words", icon = "food", reply = "none",
                en = "Food", bn = "খাবার",
            ),
        ),
        bodyParts = emptyList(),
        triageFlow = listOf("w_water", "does_not_exist"),
    )

    @Test
    fun `target languages exclude the two we author`() {
        val codes = book.targetLanguages().map { it.code }
        assertEquals(listOf("ccp", "mrh"), codes)
    }

    /** Coverage is shown to the volunteer, so it must count real lines and not aspirations. */
    @Test
    fun `coverage counts only phrases with a written line`() {
        assertEquals(1, book.coverage("ccp"))
        assertEquals(0, book.coverage("mrh"))
    }

    @Test
    fun `an unverified line is never reported as verified`() {
        val line = book.phrase("w_water")!!.t["ccp"]!!
        assertTrue(line.beng!!.isNotBlank())
        assertEquals(false, line.isVerified)
    }

    @Test
    fun `a phrase with no translation still resolves`() {
        val phrase = book.phrase("w_food")
        assertNotNull(phrase)
        assertNull(phrase!!.t["ccp"])
    }

    /** A typo in the flow list must drop that step, not crash the guided questions. */
    @Test
    fun `triage flow skips ids that are not in the book`() {
        assertEquals(listOf("w_water"), book.triagePhrases().map { it.id })
    }
}
