package org.nongor.app.core

import com.google.gson.Gson
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Parses the rest of the bundled assets through Gson and then does the thing the app does
 * with them.
 *
 * Two crashes shipped from the same root cause: a Kotlin `= emptyList()` default on a field
 * Gson fills by reflection. Gson never runs the constructor, so when the JSON omits the key
 * the field is left null inside a non-null type and nothing complains until a screen touches
 * it. Seven of the eight bundled drill scenarios have no `flags` key, so starting the flood
 * drill — the first thing a judge is likely to tap — went straight to a null pointer.
 *
 * A test that builds these objects in Kotlin cannot catch that, because Kotlin defaults do
 * apply there. The only thing that catches it is parsing the real file.
 */
class BundledAssetTest {

    private val assets = File("src/main/assets")

    private fun gson(): Gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    private fun asset(name: String): File =
        File(assets, name).also { assertTrue("missing asset $name", it.exists()) }

    @Test
    fun `drill scenarios parse and triage without a null flags list`() {
        val type = object : TypeToken<List<SosReport>>() {}.type
        val reports: List<SosReport> = gson().fromJson(asset("chattogram_sos.json").readText(), type)
        assertTrue("expected scenarios", reports.isNotEmpty())

        // The bug: most of these have no "flags" key at all.
        assertTrue(
            "this asset is supposed to exercise the missing-key path",
            reports.any { it.flags == null },
        )

        // The drill does exactly this for every scenario when it seeds sample data.
        reports.forEach { report ->
            assertNotNull(report.riskFlags)
            val result = Triage.fallbackTriage(report)
            assertTrue("unknown priority ${result.priority}", result.priority in PRIORITIES)
            assertNotNull(result.riskSignals)
        }
    }

    @Test
    fun `first aid packs parse and retrieve`() {
        val type = object : TypeToken<List<KbChunk>>() {}.type
        val chunks: List<KbChunk> = gson().fromJson(asset("first_aid_packs.json").readText(), type)
        assertTrue("expected passages", chunks.isNotEmpty())
        chunks.forEach {
            assertTrue("${it.id}: blank text", it.textMd.isNotBlank())
            assertTrue("${it.id}: blank source", it.source.isNotBlank())
            assertNotNull("${it.id}: null tags", it.tags)
            assertNotNull("${it.id}: null flags", it.flags)
        }
    }

    /** A pack contributor who omits a key must degrade to no match, not crash retrieval. */
    @Test
    fun `a passage with no tags retrieves nothing rather than crashing`() {
        val bare = KbChunk(
            id = "bare", pack = "test", hazard = "test",
            textMd = "Some guidance.", source = "test",
            symptomTags = null, redFlags = null,
        )
        assertTrue(bare.tags.isEmpty())
        assertFalse(bare.flags.isNotEmpty())
    }

    /** An SOS arriving over the mesh with no flags field must still triage. */
    @Test
    fun `an sos with no flags still triages`() {
        val report: SosReport = Gson().fromJson(
            """{"text":"Trapped on the roof, water rising","peopleCount":3}""",
            SosReport::class.java,
        )
        assertTrue(report.riskFlags.isEmpty())
        val result = Triage.fallbackTriage(report)
        assertTrue(result.riskSignals.contains("trapped"))
    }

    @Test
    fun `district and shelter data parse`() {
        assertTrue(asset("bd_districts.json").readText().isNotBlank())
        assertTrue(asset("bd_shelters.json").readText().isNotBlank())
    }
}
