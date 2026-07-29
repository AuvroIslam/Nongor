package org.nongor.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Read receipts on an SOS.
 *
 * The rules that matter to someone waiting on a roof: a receipt must never be counted twice
 * because a relay hop repeated it, this phone must never acknowledge the same message twice,
 * and neither fact may be lost when the app is killed mid-incident.
 */
class SosReceiptTest {

    @Test fun recordsWhoHasSeenAMessage() {
        val repo = SosRepository()
        assertTrue(repo.recordSeen("m1", "rahim"))
        assertTrue(repo.recordSeen("m1", "karim"))
        assertEquals(setOf("rahim", "karim"), repo.seenBy("m1"))
    }

    @Test fun sameAcknowledgerNeverCountsTwice() {
        val repo = SosRepository()
        assertTrue("first receipt is new", repo.recordSeen("m1", "rahim"))
        assertFalse("a relayed duplicate is not new", repo.recordSeen("m1", "rahim"))
        assertEquals(
            "a message echoing back over a second hop must not inflate the count",
            1,
            repo.seenBy("m1").size,
        )
    }

    @Test fun receiptsAreScopedToTheirOwnMessage() {
        val repo = SosRepository()
        repo.recordSeen("m1", "rahim")
        repo.recordSeen("m2", "karim")
        assertEquals(setOf("rahim"), repo.seenBy("m1"))
        assertEquals(setOf("karim"), repo.seenBy("m2"))
        assertTrue("an unseen message reports nobody", repo.seenBy("m3").isEmpty())
    }

    @Test fun blankIdsAndSendersAreIgnored() {
        val repo = SosRepository()
        assertFalse(repo.recordSeen("", "rahim"))
        assertFalse(repo.recordSeen("m1", ""))
        assertTrue(repo.seenBy("m1").isEmpty())
    }

    @Test fun eachMessageIsAcknowledgedOnlyOnce() {
        val repo = SosRepository()
        assertTrue("first viewing sends the receipt", repo.claimAck("m1"))
        assertFalse("scrolling past it again sends nothing", repo.claimAck("m1"))
        assertFalse(repo.claimAck("m1"))
        assertTrue(repo.hasAcked("m1"))
        assertFalse(repo.hasAcked("m2"))
    }

    @Test fun blankIdIsNeverAcknowledged() {
        val repo = SosRepository()
        assertFalse(repo.claimAck(""))
    }

    @Test fun receiptsAndAcksSurviveARestart() {
        val f = File.createTempFile("sos-receipts", ".json").apply { deleteOnExit() }

        val before = SosRepository(f)
        before.recordSeen("m1", "rahim")
        before.recordSeen("m1", "karim")
        before.claimAck("incoming-1")

        // A background kill mid-incident, then a relaunch.
        val after = SosRepository(f)
        assertEquals(
            "who saw our SOS must not be forgotten on restart",
            setOf("rahim", "karim"),
            after.seenBy("m1"),
        )
        assertFalse(
            "a restart must not re-acknowledge everything still in the store",
            after.claimAck("incoming-1"),
        )
    }

    @Test fun clearingTheStoreClearsReceipts() {
        val repo = SosRepository()
        repo.recordSeen("m1", "rahim")
        repo.claimAck("m2")
        repo.clear()
        assertTrue(repo.seenBy("m1").isEmpty())
        assertFalse(repo.hasAcked("m2"))
    }

    /**
     * A store written before receipts existed has no such field. Gson populates fields by
     * reflection and never runs the Kotlin constructor, so a defaulted non-null type would be
     * left holding null and crash on first read.
     */
    @Test fun aFileWrittenBeforeReceiptsExistedStillLoads() {
        val f = File.createTempFile("sos-legacy", ".json").apply { deleteOnExit() }
        f.writeText("""{"entries":[],"quarantine":[]}""")

        val repo = SosRepository(f)
        assertTrue(repo.seenBy("anything").isEmpty())
        assertFalse(repo.hasAcked("anything"))
        assertTrue("and a receipt can still be recorded afterwards", repo.recordSeen("m1", "rahim"))
    }
}
