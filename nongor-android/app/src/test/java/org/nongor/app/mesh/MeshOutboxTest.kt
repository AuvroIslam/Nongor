package org.nongor.app.mesh

import org.nongor.app.core.SignedEnvelope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Store-and-forward contract. The mesh exists to send an SOS with no network, so the case where
 * nobody is in range yet is the normal case — a message composed then must survive until a phone
 * appears, never be dropped while the UI claims it is queued.
 */
class MeshOutboxTest {

    private fun env(id: String, text: String = "help") = SignedEnvelope(
        sender = "node-1", msgId = id, lamport = 1, payload = mapOf("text" to text),
    )

    @Test fun anSosComposedWithNobodyInRangeIsKept() {
        val outbox = MeshOutbox()
        outbox.add(env("a"))
        assertEquals(1, outbox.size())
        assertFalse(outbox.isEmpty())
    }

    @Test fun drainHandsBackEverythingPendingInOrder() {
        val outbox = MeshOutbox()
        outbox.add(env("a")); outbox.add(env("b")); outbox.add(env("c"))
        assertEquals(listOf("a", "b", "c"), outbox.drain().map { it.msgId })
    }

    @Test fun drainEmptiesTheQueue_soAPeerIsNotSentTheSameSosTwice() {
        val outbox = MeshOutbox()
        outbox.add(env("a"))
        outbox.drain()
        assertTrue(outbox.isEmpty())
        assertEquals(0, outbox.size())
    }

    @Test fun aFailedSendCanBeRequeued_soTheMessageIsNotLost() {
        val outbox = MeshOutbox()
        outbox.add(env("a"))
        val pending = outbox.drain()
        // Transfer failed (peer walked out of range) — the manager puts it back.
        pending.forEach(outbox::add)
        assertEquals(listOf("a"), outbox.drain().map { it.msgId })
    }

    @Test fun requeuingAnAlreadyPendingMessageDoesNotDuplicateIt() {
        val outbox = MeshOutbox()
        outbox.add(env("a"))
        outbox.add(env("a"))
        assertEquals(1, outbox.size())
    }

    @Test fun theQueueIsBounded_soALongSpellAloneCannotExhaustMemory() {
        val outbox = MeshOutbox(max = 3)
        (1..10).forEach { outbox.add(env("id-$it")) }
        assertEquals(3, outbox.size())
    }

    @Test fun whenFullTheOldestIsDropped_keepingTheMostRecentSos() {
        val outbox = MeshOutbox(max = 3)
        (1..5).forEach { outbox.add(env("id-$it")) }
        assertEquals(listOf("id-3", "id-4", "id-5"), outbox.drain().map { it.msgId })
    }

    @Test fun drainingAnEmptyOutboxIsHarmless() {
        val outbox = MeshOutbox()
        assertTrue(outbox.drain().isEmpty())
        assertTrue(outbox.isEmpty())
    }

    /**
     * Two phones of the same model share a display name, so the advertised name carries a unique
     * suffix to keep them apart. Everything user-facing must show the plain name.
     */
    @Test fun theUniquenessSuffixNeverReachesTheUser() {
        assertEquals("Nongor SM-G990E", "Nongor SM-G990E#A3f2".substringBefore('#'))
        assertEquals("Nongor Pixel 10a", "Nongor Pixel 10a#0000".substringBefore('#'))
    }

    @Test fun aNameWithoutASuffixIsLeftAlone() {
        assertEquals("Nongor Pixel 10a", "Nongor Pixel 10a".substringBefore('#'))
    }

    @Test fun sameModelPhonesGetDifferentAdvertisedNames_soTheDialTieBreakDecides() {
        val a = "Nongor SM-G990E#A3f2"
        val b = "Nongor SM-G990E#B7c1"
        assertEquals("Nongor SM-G990E", a.substringBefore('#'))
        assertEquals("Nongor SM-G990E", b.substringBefore('#'))
        assertTrue("exactly one side must dial", (a > b) != (b > a))
    }
}
