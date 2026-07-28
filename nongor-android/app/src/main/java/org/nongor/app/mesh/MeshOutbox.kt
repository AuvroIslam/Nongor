package org.nongor.app.mesh

import org.nongor.app.core.SignedEnvelope

/**
 * Store-and-forward queue for envelopes composed while nothing was in range.
 *
 * The whole premise of the mesh is "send an SOS with no network", so the case where no peer has
 * been found *yet* is the case that matters most — dropping the message there and telling the user
 * it was queued is the worst possible failure. Anything that cannot go out now waits here and is
 * flushed the moment a peer connects.
 *
 * Owned by [MeshHub] rather than [MeshManager] so that stopping and restarting the radio (leaving
 * the mesh screens, a screen transition) does not throw away messages the user believes are
 * pending. It is deliberately bounded: a phone that spends a long time alone in a flood should not
 * grow this without limit, and the oldest entry is the least likely to still be worth delivering.
 */
class MeshOutbox(private val max: Int = MAX_PENDING) {
    private val pending = ArrayDeque<SignedEnvelope>()

    companion object {
        private const val MAX_PENDING = 64
    }

    /** Queue an envelope for later delivery. Re-queuing an already-pending id is a no-op. */
    @Synchronized
    fun add(env: SignedEnvelope) {
        if (pending.any { it.msgId == env.msgId }) return
        while (pending.size >= max) pending.removeFirst()
        pending.addLast(env)
    }

    /** Take everything pending. Callers must re-[add] whatever fails to send. */
    @Synchronized
    fun drain(): List<SignedEnvelope> {
        val out = pending.toList()
        pending.clear()
        return out
    }

    @Synchronized
    fun size(): Int = pending.size

    @Synchronized
    fun isEmpty(): Boolean = pending.isEmpty()
}
