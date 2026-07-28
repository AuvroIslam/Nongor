package org.nongor.app.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/** The encounter log must always show the most recent sighting of each relative, and only one row each. */
class FamilyRepositoryTest {

    private fun seen(name: String, ts: Long, lat: Double? = null, lon: Double? = null) =
        FamilyMember(name = name, lastSeenTs = ts, lat = lat, lon = lon)

    @Test fun aNewerSightingReplacesTheOlderOne() {
        val repo = FamilyRepository()
        repo.record(seen("Ma", 1_000, 22.80, 89.55))
        repo.record(seen("Ma", 5_000, 22.81, 89.56))
        assertEquals("one row per family member", 1, repo.members.value.size)
        assertEquals("keeps the latest encounter", 5_000L, repo.members.value.single().lastSeenTs)
    }

    @Test fun aStaleRelayedSightingDoesNotOverwriteAFresherOne() {
        val repo = FamilyRepository()
        repo.record(seen("Ma", 5_000))
        repo.record(seen("Ma", 1_000))     // arrives late via a slow multi-hop relay
        assertEquals("the fresher sighting wins", 5_000L, repo.members.value.single().lastSeenTs)
    }

    @Test fun differentRelativesAreTrackedSeparately_newestFirst() {
        val repo = FamilyRepository()
        repo.record(seen("Ma", 1_000))
        repo.record(seen("Abba", 9_000))
        assertEquals(2, repo.members.value.size)
        assertEquals("most recently seen listed first", "Abba", repo.members.value.first().name)
    }

    @Test fun namesAreMatchedCaseInsensitively() {
        val repo = FamilyRepository()
        repo.record(seen("Ma", 1_000))
        repo.record(seen("ma", 2_000))
        assertEquals("same person, not two rows", 1, repo.members.value.size)
    }

    @Test fun encountersSurviveRestart() {
        val file = File.createTempFile("family_store", ".json").apply { delete() }
        try {
            FamilyRepository(file).record(seen("Ma", 7_000, 22.80, 89.55))
            val reloaded = FamilyRepository(file)
            assertEquals(1, reloaded.members.value.size)
            assertEquals("Ma", reloaded.members.value.single().name)
        } finally {
            file.delete()
        }
    }
}
