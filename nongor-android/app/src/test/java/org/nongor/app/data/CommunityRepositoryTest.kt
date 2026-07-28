package org.nongor.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Community Intelligence: relayed copies must not pile up, and forged reports must not be trusted. */
class CommunityRepositoryTest {

    private fun report(kind: String, district: String, id: String = "id-$kind-$district") =
        CommunityReport(id = id, kind = kind, note = "", districtEn = district, sender = "peer")

    @Test fun relayedDuplicatesAreIgnored() {
        val repo = CommunityRepository()
        val r = report("road_flooded", "Khulna", id = "same-id")
        repo.add(r)
        repo.add(r.copy(note = "arrived again via another hop"))
        assertEquals("multi-hop relay must not duplicate a report", 1, repo.entries.value.size)
    }

    @Test fun unverifiedReportGoesToQuarantineNotTheBoard() {
        val repo = CommunityRepository()
        repo.addQuarantined(report("danger", "Khulna").copy(verified = false))
        assertTrue("forged reports never reach the trusted board", repo.entries.value.isEmpty())
        assertEquals(1, repo.quarantine.value.size)
    }

    @Test fun forDistrictKeepsOneAreaSeparateFromAnother() {
        val repo = CommunityRepository()
        repo.add(report("road_flooded", "Khulna", id = "a"))
        repo.add(report("supplies", "Khulna", id = "b"))
        repo.add(report("danger", "Sylhet", id = "c"))
        assertEquals("only this district's reports", 2, repo.forDistrict("Khulna").size)
        assertEquals("district match is case-insensitive", 2, repo.forDistrict("khulna").size)
        assertEquals(1, repo.forDistrict("Sylhet").size)
    }

    @Test fun reportsSurviveRestart() {
        val file = File.createTempFile("community_store", ".json").apply { delete() }
        try {
            val repo = CommunityRepository(file)
            repo.add(report("pharmacy_open", "Khulna", id = "keep-me"))
            repo.addQuarantined(report("danger", "Khulna", id = "bad").copy(verified = false))

            val reloaded = CommunityRepository(file)
            assertEquals(1, reloaded.entries.value.size)
            assertEquals("keep-me", reloaded.entries.value.single().id)
            assertEquals("quarantine survives too", 1, reloaded.quarantine.value.size)
        } finally {
            file.delete()
        }
    }

    @Test fun newestReportsSortFirst() {
        val repo = CommunityRepository()
        repo.add(report("supplies", "Khulna", id = "old").copy(ts = 1_000))
        repo.add(report("danger", "Khulna", id = "new").copy(ts = 9_000))
        assertEquals("newest first", "new", repo.entries.value.first().id)
    }
}
