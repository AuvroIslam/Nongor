package org.nongor.app.core

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Parses the **real** bundled asset with the **real** Gson configuration.
 *
 * This exists because of a bug that shipped: `Phrase.t` was declared
 * `Map<String, Translation> = emptyMap()`, which reads as safe Kotlin and is not. Gson fills
 * fields by reflection without running the constructor, so a phrase whose JSON omits `t` —
 * which is most of them — got a null in a field typed non-null, and the translation screen
 * crashed on open.
 *
 * The unit tests all passed, because every one of them built a `Phrase` in Kotlin, where
 * defaults do apply. The lesson is that a model deserialised from an asset has to be tested
 * through the deserialiser, so that is what this does.
 */
class PhrasebookAssetTest {

    private val book: PhrasebookData by lazy {
        val file = File("src/main/assets/phrasebook.json")
        assertTrue("phrasebook.json not found at ${file.absolutePath}", file.exists())
        Gson().fromJson(file.readText(), PhrasebookData::class.java)
    }

    @Test
    fun `the bundled phrasebook parses`() {
        assertNotNull(book)
        assertTrue("expected phrases", book.allPhrases.isNotEmpty())
        assertTrue("expected languages", book.allLanguages.isNotEmpty())
        assertTrue("expected categories", book.allCategories.isNotEmpty())
        assertTrue("expected body parts", book.allBodyParts.isNotEmpty())
        assertTrue("expected a triage flow", book.flow.isNotEmpty())
    }

    /** The exact call that crashed the translation screen, over the real data. */
    @Test
    fun `coverage can be computed for every selectable language`() {
        for (lang in book.targetLanguages()) {
            val n = book.coverage(lang.code)
            assertTrue("negative coverage for ${lang.code}", n >= 0)
            assertTrue("coverage exceeds phrase count", n <= book.allPhrases.size)
        }
    }

    /** Reading a phrase with no `t` block at all must not blow up. */
    @Test
    fun `phrases without translations normalise to empty`() {
        val bare = book.allPhrases.filter { it.t == null }
        assertTrue("expected some phrases with no translations", bare.isNotEmpty())
        bare.forEach {
            assertTrue(it.translations.isEmpty())
            assertEquals(null, it.translation("ccp"))
        }
    }

    @Test
    fun `phrases without tags still search without crashing`() {
        book.allPhrases.forEach { assertNotNull(it.tagList) }
        assertNotNull(PhraseSearch.search(book.allPhrases, "water"))
    }

    @Test
    fun `every phrase has the fields the ui reads`() {
        book.allPhrases.forEach { p ->
            assertTrue("blank id", p.id.isNotBlank())
            assertTrue("${p.id}: blank en", p.en.isNotBlank())
            assertTrue("${p.id}: blank bn", p.bn.isNotBlank())
            assertTrue("${p.id}: blank icon", p.icon.isNotBlank())
            assertTrue("${p.id}: unknown category", book.allCategories.any { it.id == p.cat })
        }
    }

    @Test
    fun `phrase ids are unique`() {
        val ids = book.allPhrases.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `every guided step resolves to a real phrase`() {
        assertEquals(book.flow.size, book.triagePhrases().size)
    }

    /**
     * Nothing in the shipped seed claims to be verified. If a native speaker ever marks an
     * entry `verified`, this test is meant to fail — so the change is looked at deliberately
     * rather than sliding in with an unrelated commit.
     */
    @Test
    fun `no seeded line is marked verified yet`() {
        book.allPhrases.forEach { p ->
            p.translations.forEach { (code, line) ->
                assertFalse("${p.id}/$code claims to be verified", line.isVerified)
            }
        }
    }

    @Test
    fun `search over the real phrasebook finds the medical basics`() {
        fun firstId(q: String) = PhraseSearch.search(book.allPhrases, q).firstOrNull()?.id
        assertEquals("bleeding", firstId("bleeding"))
        assertEquals("bleeding", firstId("রক্ত"))
        assertNotNull(firstId("water"))
        assertNotNull(firstId("পানি"))
    }
}
