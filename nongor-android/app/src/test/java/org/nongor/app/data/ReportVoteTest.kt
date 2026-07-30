package org.nongor.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Confirming and disputing a neighbour's report.
 *
 * The whole point is that a count means "this many distinct phones", so the rules that matter
 * are the ones that stop a single handset manufacturing a consensus: a relayed duplicate must
 * not count twice, and changing your mind must move your vote rather than adding a second one
 * on the opposite side.
 */
class ReportVoteTest {

    @Test fun distinctVotersAccumulate() {
        val repo = CommunityRepository()
        assertTrue(repo.recordVote("r1", "rahim", up = true))
        assertTrue(repo.recordVote("r1", "karim", up = true))
        assertEquals(2, repo.confirmCount("r1"))
        assertEquals(0, repo.disputeCount("r1"))
    }

    @Test fun aRelayedDuplicateDoesNotCountTwice() {
        val repo = CommunityRepository()
        assertTrue(repo.recordVote("r1", "rahim", up = true))
        assertFalse("the same vote arriving over a second hop is not new", repo.recordVote("r1", "rahim", up = true))
        assertEquals(
            "one phone must not be able to inflate a report by bouncing its vote around",
            1,
            repo.confirmCount("r1"),
        )
    }

    @Test fun changingYourMindMovesTheVoteRatherThanAddingOne() {
        val repo = CommunityRepository()
        repo.recordVote("r1", "rahim", up = true)
        assertEquals(1, repo.confirmCount("r1"))

        repo.recordVote("r1", "rahim", up = false)
        assertEquals("the old confirm must be withdrawn", 0, repo.confirmCount("r1"))
        assertEquals(1, repo.disputeCount("r1"))
    }

    @Test fun votesAreScopedToTheirOwnReport() {
        val repo = CommunityRepository()
        repo.recordVote("r1", "rahim", up = true)
        repo.recordVote("r2", "rahim", up = false)
        assertEquals(1, repo.confirmCount("r1"))
        assertEquals(0, repo.disputeCount("r1"))
        assertEquals(0, repo.confirmCount("r2"))
        assertEquals(1, repo.disputeCount("r2"))
    }

    @Test fun blankIdsAndVotersAreIgnored() {
        val repo = CommunityRepository()
        assertFalse(repo.recordVote("", "rahim", up = true))
        assertFalse(repo.recordVote("r1", "", up = true))
        assertEquals(0, repo.confirmCount("r1"))
    }

    @Test fun anUnvotedReportReportsZero() {
        val repo = CommunityRepository()
        assertEquals(0, repo.confirmCount("never-seen"))
        assertEquals(0, repo.disputeCount("never-seen"))
    }

    @Test fun votesSurviveARestart() {
        val f = File.createTempFile("community-votes", ".json").apply { deleteOnExit() }

        val before = CommunityRepository(f)
        before.recordVote("r1", "rahim", up = true)
        before.recordVote("r1", "karim", up = false)
        before.setMyVote("r1", up = true)

        val after = CommunityRepository(f)
        assertEquals(1, after.confirmCount("r1"))
        assertEquals(1, after.disputeCount("r1"))
        assertEquals("up", after.myVotes.value["r1"])
    }

    /**
     * A store written before voting existed has no such fields. Gson populates by reflection and
     * skips Kotlin defaults, so a non-null type would arrive holding null and crash on first read.
     */
    @Test fun aStoreWrittenBeforeVotingExistedStillLoads() {
        val f = File.createTempFile("community-legacy", ".json").apply { deleteOnExit() }
        f.writeText("""{"entries":[],"quarantine":[]}""")

        val repo = CommunityRepository(f)
        assertEquals(0, repo.confirmCount("r1"))
        assertTrue("and a vote can still be cast afterwards", repo.recordVote("r1", "rahim", up = true))
    }

    @Test fun clearingTheBoardClearsVotes() {
        val repo = CommunityRepository()
        repo.recordVote("r1", "rahim", up = true)
        repo.clear()
        assertEquals(0, repo.confirmCount("r1"))
    }
}
