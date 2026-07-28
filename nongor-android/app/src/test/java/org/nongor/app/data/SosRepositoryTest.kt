package org.nongor.app.data

import org.nongor.app.core.SosReport
import org.nongor.app.core.Triage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SosRepositoryTest {

    private fun entry(text: String, source: String): SosEntry {
        val sos = SosReport(text = text)
        return SosEntry(sos, Triage.fallbackTriage(sos), source = source)
    }

    @Test fun realReportPurgesSeededDrillEntries() {
        val repo = SosRepository()
        repo.add(entry("drill case A", "drill"))
        repo.add(entry("drill case B", "drill"))
        assertEquals("drill data seeds the store", 2, repo.entries.value.size)

        repo.add(entry("real triage case", "triage"))
        assertEquals("a real report clears all drill data", 1, repo.entries.value.size)
        assertEquals("triage", repo.entries.value.single().source)
    }

    @Test fun unverifiedReportGoesToQuarantineNotEntries() {
        val repo = SosRepository()
        repo.addQuarantined(entry("forged mesh report", "mesh_recv").copy(verified = false))
        assertTrue("trusted set stays empty", repo.entries.value.isEmpty())
        assertEquals("quarantine holds it", 1, repo.quarantine.value.size)
    }

    @Test fun realReportsSurviveReload_drillDoesNot() {
        val file = File.createTempFile("sos_store", ".json").apply { delete() }
        try {
            val repo = SosRepository(file)
            repo.add(entry("drill case", "drill"))
            repo.add(entry("real case", "triage"))
            repo.addQuarantined(entry("bad mesh", "mesh_recv").copy(verified = false))

            // Simulate an app restart: a fresh instance reading the same file.
            val reloaded = SosRepository(file)
            assertEquals("only the real report is restored", 1, reloaded.entries.value.size)
            assertEquals("real case", reloaded.entries.value.single().report.text)
            assertFalse("drill data was not persisted",
                reloaded.entries.value.any { it.source == "drill" })
            assertEquals("quarantine survives restart too", 1, reloaded.quarantine.value.size)
        } finally {
            file.delete()
        }
    }
}
