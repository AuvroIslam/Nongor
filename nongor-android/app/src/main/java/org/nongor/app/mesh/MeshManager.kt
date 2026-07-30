package org.nongor.app.mesh

import android.content.Context
import org.nongor.app.core.SignedEnvelope
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.google.gson.Gson
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Offline mesh transport over Google Nearby Connections (Bluetooth + Wi-Fi Direct, no internet).
 * Wraps each SOS in a Ed25519-signable [SignedEnvelope] (see core/Mesh.kt) — verify-before-trust,
 * Lamport-clock dedup, and controlled-flooding multi-hop relay.
 */
class MeshManager(
    context: Context,
    val localName: String,
    private val outbox: MeshOutbox,
    private val onStatus: (String) -> Unit,
    private val onPeersChanged: (Int) -> Unit,
    private val onReceived: (env: SignedEnvelope, verified: Boolean, hops: Int) -> Unit,
) {
    private val client: ConnectionsClient = Nearby.getConnectionsClient(context)
    private val signer = MeshIdentity.loadOrCreateSigner(context, localName)
    private val gson = Gson()

    /**
     * What actually goes on the air. Two phones of the same model share a [localName] ("Nongor
     * SM-G990E"), which would make them indistinguishable as peers and leave the dial tie-break
     * below undecidable. The signing keypair is per-install random, so a few characters of the
     * public key give each phone a stable unique advertising name at no extra cost. The suffix is
     * an implementation detail — [displayNameOf] strips it before anything reaches the UI.
     */
    private val advertisedName: String = run {
        val tag = signer.publicKeyB64.filter { it.isLetterOrDigit() }.take(4).ifEmpty { "0000" }
        "${localName.take(40)}#$tag"
    }

    /** The human-facing half of an advertised name. */
    private fun displayNameOf(name: String): String = name.substringBefore('#')
    private var clock = 0
    // Bounded dedup cache: a long relay session could otherwise grow this set without limit.
    // LRU-evicting the oldest ids past the cap keeps memory flat; re-seeing an id older than the
    // cap at worst re-delivers one message, which the UI already tolerates.
    private val seen: MutableSet<String> = java.util.Collections.synchronizedSet(
        java.util.Collections.newSetFromMap(
            object : LinkedHashMap<String, Boolean>(256, 0.75f, false) {
                override fun removeEldestEntry(eldest: Map.Entry<String, Boolean>): Boolean =
                    size > MAX_SEEN
            },
        ),
    )
    // Guards both maps below: Nearby fires its callbacks on its own threads while the UI sends.
    private val peerLock = Any()
    private val connected = mutableSetOf<String>()
    // endpointId -> advertised name. One physical phone can end up behind more than one endpointId
    // (P2P_CLUSTER has both sides advertising *and* discovering, and a peer reached over a second
    // medium arrives as a fresh id), so the displayed peer count is distinct *names*, not ids.
    private val endpointNames = mutableMapOf<String, String>()

    companion object {
        const val SERVICE_ID = "org.nongor.app.NONGOR_MESH"
        private const val MAX_SEEN = 2_000

        // Wide enough that two phones rarely land in the same window, short enough that pairing
        // still feels immediate when someone is standing next to you.
        private const val DIAL_JITTER_MIN_MS = 150L
        private const val DIAL_JITTER_MAX_MS = 1_800L

        // How long a dial may sit unresolved before we assume it stalled and allow a retry.
        private const val DIAL_TIMEOUT_MS = 12_000L
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let { handleIncoming(String(it, Charsets.UTF_8)) }
        }
        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }

    private val lifecycle = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            synchronized(peerLock) { endpointNames[endpointId] = info.endpointName }
            client.acceptConnection(endpointId, payloadCallback)   // auto-accept for the demo
            onStatus("Connecting to ${displayNameOf(info.endpointName)}…")
        }
        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            synchronized(peerLock) { endpointNames[endpointId]?.let { dialing.remove(it) } }
            if (!result.status.isSuccess) {
                synchronized(peerLock) { endpointNames.remove(endpointId) }
                return
            }
            // Safety net behind the dial tie-break: if this phone is already connected on another
            // endpoint, a second channel would only double-send. Advertised names are unique, so
            // a match here is unambiguously the same device.
            val redundant = synchronized(peerLock) {
                val name = endpointNames[endpointId]
                name != null && connected.any { it != endpointId && endpointNames[it] == name }
            }
            if (redundant) {
                synchronized(peerLock) { endpointNames.remove(endpointId) }
                try {
                    client.disconnectFromEndpoint(endpointId)
                } catch (_: Exception) {
                }
                return
            }
            val n = synchronized(peerLock) { connected.add(endpointId); peerCountLocked() }
            onPeersChanged(n)
            onStatus("Connected — $n peer(s)")
            // Someone is finally in range: anything composed while we were alone goes out now.
            flushOutbox()
        }
        override fun onDisconnected(endpointId: String) {
            val n = synchronized(peerLock) {
                connected.remove(endpointId)
                endpointNames.remove(endpointId)?.let { dialing.remove(it) }
                peerCountLocked()
            }
            onPeersChanged(n)
            // Without this the banner keeps claiming "Connected — 1 peer(s)" after the last phone
            // walks away, which is exactly the wrong thing to tell someone waiting on a rescue.
            onStatus(
                if (n == 0) {
                    if (outbox.isEmpty()) "No peers in range — listening"
                    else "No peers in range — ${outbox.size()} message(s) waiting to send"
                } else "Connected — $n peer(s)",
            )
        }
    }

    /**
     * Advertised names we have an outstanding connection request to.
     *
     * Discovery fires repeatedly for the same endpoint; without this we would hammer
     * requestConnection every callback. Cleared on failure, on loss, and once resolved.
     */
    private val dialing = mutableSetOf<String>()

    /** Own scope so a jittered dial and its watchdog survive independently of any screen. */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private fun clearDial(name: String) {
        synchronized(peerLock) { dialing.remove(name) }
    }

    private val discovery = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            // Both sides dial. This used to break the symmetry so that only the lower advertised
            // name initiated — tidier, and it avoided a redundant second channel. But it made the
            // entire pairing depend on one specific phone's *discovery* working: if the lower-named
            // phone was the one whose BLE scan got throttled (routine on OEM Android), both phones
            // advertised at each other forever and neither ever dialled. Two phones on the same
            // table, both saying "listening", peers stuck at zero.
            //
            // Dialling from both ends costs at most one duplicate channel, and that is already
            // handled: onConnectionResult drops a second connection to a name we hold, and
            // incoming payloads are deduped by message id regardless. A redundant socket is a far
            // cheaper failure than no socket.
            val name = info.endpointName
            val alreadyKnown = synchronized(peerLock) {
                connected.any { endpointNames[it] == name } || name in dialing
            }
            if (alreadyKnown) return
            synchronized(peerLock) { dialing.add(name) }

            scope.launch {
                // Jitter before dialling. Two phones that discover each other in the same instant
                // will otherwise call requestConnection simultaneously, and Nearby resolves that
                // collision by dropping one or both requests — silently, with no failure callback.
                // A short random wait means one side almost always gets there first and the other
                // sees it as an incoming connection instead.
                delay(Random.nextLong(DIAL_JITTER_MIN_MS, DIAL_JITTER_MAX_MS))
                val stillWanted = synchronized(peerLock) {
                    name in dialing && connected.none { endpointNames[it] == name }
                }
                if (!stillWanted) return@launch

                runCatching {
                    client.requestConnection(advertisedName, endpointId, lifecycle)
                        .addOnFailureListener { clearDial(name) }
                }.onFailure { clearDial(name) }

                // Watchdog. requestConnection can report success and then never resolve — the
                // handshake stalls and neither onConnectionResult nor the failure listener ever
                // fires. Without this the guard stays set for the life of the process and every
                // later discovery callback returns early, so two phones sitting next to each
                // other never pair again. Releasing the guard lets the next callback retry.
                delay(DIAL_TIMEOUT_MS)
                val stalled = synchronized(peerLock) {
                    name in dialing && connected.none { endpointNames[it] == name }
                }
                if (stalled) {
                    clearDial(name)
                    onStatus("Retrying ${displayNameOf(name)}…")
                }
            }
        }

        override fun onEndpointLost(endpointId: String) {
            // Discovery losing the advertisement does not mean an established connection dropped
            // (onDisconnected owns that), so only forget endpoints we never connected to.
            synchronized(peerLock) {
                if (endpointId !in connected) {
                    endpointNames.remove(endpointId)?.let { dialing.remove(it) }
                }
            }
        }
    }

    /** Distinct physical phones behind the live endpoints. Caller must hold [peerLock]. */
    private fun peerCountLocked(): Int =
        connected.mapTo(mutableSetOf()) { endpointNames[it] ?: it }.size

    fun start() {
        // Nearby endpoints live in Google Play services, not in this process, so a crash or a
        // force-stop leaves the previous session's connections established and invisible to us —
        // they reappear as extra peers we never accepted. Clear them before advertising again.
        try {
            client.stopAllEndpoints()
        } catch (_: Exception) {
        }
        val strategy = Strategy.P2P_CLUSTER
        client.startAdvertising(
            advertisedName, SERVICE_ID, lifecycle,
            AdvertisingOptions.Builder().setStrategy(strategy).build())
            .addOnSuccessListener { onStatus("Advertising as $localName") }
            .addOnFailureListener { onStatus("Advertise failed: ${it.message}") }
        client.startDiscovery(
            SERVICE_ID, discovery,
            DiscoveryOptions.Builder().setStrategy(strategy).build())
            .addOnFailureListener { onStatus("Discovery failed: ${it.message}") }
    }

    fun stop() {
        scope.coroutineContext.cancelChildren()
        try {
            client.stopAdvertising(); client.stopDiscovery(); client.stopAllEndpoints()
        } catch (_: Exception) {
        }
        synchronized(peerLock) { connected.clear(); endpointNames.clear(); dialing.clear() }
    }

    fun peers(): Int = synchronized(peerLock) { peerCountLocked() }

    /**
     * Compose + broadcast a signed SOS. Returns the envelope so the UI can show it locally.
     *
     * [msgId] lets a held-down SOS reuse one id for every repeat of the same call for help.
     * Receivers dedupe on the id, so a phone already holding it ignores the repeat while a
     * phone that only comes into range on the fourth attempt still gets it - which is the
     * behaviour a store-and-forward mesh is for. Minting a fresh id each time instead turned
     * one person shouting into five separate reports of five separate emergencies.
     */
    fun sendSos(text: String, lat: Double?, lon: Double?, msgId: String? = null): SignedEnvelope {
        clock += 1
        val payload = mapOf(
            "text" to text,
            "lat" to (lat?.toString() ?: ""),
            "lon" to (lon?.toString() ?: ""),
        )
        val env = SignedEnvelope.create(
            signer, payload, msgId ?: UUID.randomUUID().toString(), clock, ttl = 4)
        seen.add(env.msgId)
        broadcast(env)
        val n = peers()
        if (n == 0) onStatus("No peers yet — SOS queued (${outbox.size()} waiting to send)")
        else onStatus("SOS broadcast to $n peer(s)")
        return env
    }

    /** Compose + broadcast a signed community report (type = "community"). Same trust as SOS. */
    fun sendReport(kind: String, note: String, district: String, lat: Double?, lon: Double?): SignedEnvelope {
        clock += 1
        val payload = mapOf(
            "kind" to kind,
            "note" to note,
            "district" to district,
            "lat" to (lat?.toString() ?: ""),
            "lon" to (lon?.toString() ?: ""),
        )
        val env = SignedEnvelope.create(
            signer, payload, UUID.randomUUID().toString(), clock, ttl = 4, type = "community")
        seen.add(env.msgId)
        broadcast(env)
        val n = peers()
        onStatus(if (n == 0) "No peers yet — report queued (${outbox.size()} waiting to send)"
        else "Report shared with $n peer(s)")
        return env
    }

    /**
     * Confirm or dispute somebody else's report (type = "vote").
     *
     * Signed like everything else, because the whole value of a dispute is knowing that real
     * distinct phones disagreed. Unsigned votes would let one handset manufacture a consensus
     * and bury a true report — the exact attack this is meant to defend against.
     */
    fun sendVote(forReportId: String, up: Boolean): SignedEnvelope {
        clock += 1
        val env = SignedEnvelope.create(
            signer, mapOf("for" to forReportId, "v" to if (up) "up" else "down"),
            UUID.randomUUID().toString(), clock, ttl = 4, type = "vote")
        seen.add(env.msgId)
        broadcast(env)
        return env
    }

    /**
     * Stand down a call for help (type = "safe").
     *
     * Carries only the id of the SOS being resolved. This matters more than it looks: in a flood
     * the scarce resource is boats and people, and a rescuer steering towards an SOS that was
     * resolved an hour ago is capacity taken away from someone still waiting. Cancelling has to
     * travel as far as the original call did, so it relays on the same terms.
     */
    fun sendSafe(forMsgId: String): SignedEnvelope {
        clock += 1
        val env = SignedEnvelope.create(
            signer, mapOf("for" to forMsgId), UUID.randomUUID().toString(), clock,
            ttl = 4, type = "safe")
        seen.add(env.msgId)
        broadcast(env)
        return env
    }

    /**
     * Acknowledge that a received SOS was actually put in front of a human (type = "seen").
     *
     * The payload is only the id of the message being acknowledged - no text, no location, no
     * new claim about the world. It rides the same relay as everything else, so an ack can
     * find its way back over two hops even though the two phones were never in range of each
     * other at the same moment.
     */
    fun sendSeen(forMsgId: String): SignedEnvelope {
        clock += 1
        val env = SignedEnvelope.create(
            signer, mapOf("for" to forMsgId), UUID.randomUUID().toString(), clock,
            ttl = 4, type = "seen")
        seen.add(env.msgId)
        broadcast(env)
        return env
    }

    /**
     * Broadcast a signed family "presence" beacon (type = "presence"). Only a hashed family tag and
     * an encrypted member name go on the air, so a stranger in range learns nothing.
     */
    fun sendPresence(tag: String, sealedName: String, lat: Double?, lon: Double?): SignedEnvelope {
        clock += 1
        val payload = mapOf(
            "tag" to tag,
            "who" to sealedName,
            "lat" to (lat?.toString() ?: ""),
            "lon" to (lon?.toString() ?: ""),
        )
        val env = SignedEnvelope.create(
            signer, payload, UUID.randomUUID().toString(), clock, ttl = 3, type = "presence")
        seen.add(env.msgId)
        broadcast(env)
        return env
    }

    private fun broadcast(env: SignedEnvelope) {
        val targets = synchronized(peerLock) { connected.toList() }
        if (targets.isEmpty()) {
            // Nobody in range yet. Hold it — never drop it — and let flushOutbox() deliver it as
            // soon as a phone connects. This is the ordinary case in a flood, not an error.
            outbox.add(env)
            return
        }
        sendTo(targets, env)
    }

    private fun sendTo(targets: List<String>, env: SignedEnvelope) {
        val bytes = gson.toJson(envToMap(env)).toByteArray(Charsets.UTF_8)
        // Sent per endpoint rather than as one multi-target call so a failure names the endpoint
        // that actually died and we can drop just that one.
        targets.forEach { endpointId ->
            client.sendPayload(endpointId, Payload.fromBytes(bytes))
                .addOnFailureListener {
                    // A peer whose process was killed leaves its endpoint established on our side
                    // with no onDisconnected callback, so it keeps inflating the peer count until
                    // something tries to use it. A failed send is that proof — drop it.
                    dropEndpoint(endpointId)
                    // The sender must never believe an SOS went out when the transfer actually
                    // failed (peer walked out of range, radio dropped) — keep it for the next
                    // peer (add() is id-deduped, so several failures queue it once) and say so.
                    outbox.add(env)
                    onStatus("⚠ Send failed: ${it.message ?: "connection lost"} — kept for retry")
                }
        }
    }

    /** Forget an endpoint we can no longer reach and refresh the peer count. */
    private fun dropEndpoint(endpointId: String) {
        val n = synchronized(peerLock) {
            connected.remove(endpointId); endpointNames.remove(endpointId); peerCountLocked()
        }
        onPeersChanged(n)
    }

    /** Deliver everything that was composed while no peer was in range. */
    private fun flushOutbox() {
        if (outbox.isEmpty()) return
        val targets = synchronized(peerLock) { connected.toList() }
        if (targets.isEmpty()) return
        val pending = outbox.drain()
        if (pending.isEmpty()) return
        pending.forEach { sendTo(targets, it) }
        onStatus("Sent ${pending.size} queued message(s)")
    }

    private fun envToMap(env: SignedEnvelope): Map<String, Any?> {
        val m = env.signedContent().toMutableMap()
        m["sig"] = env.sig
        m["ttl"] = env.ttl
        return m
    }

    private fun handleIncoming(json: String) {
        try {
            @Suppress("UNCHECKED_CAST")
            val d = gson.fromJson(json, Map::class.java) as Map<String, Any?>
            @Suppress("UNCHECKED_CAST")
            val payload = (d["payload"] as? Map<String, Any?>) ?: emptyMap()
            val env = SignedEnvelope(
                sender = d["sender"] as String,
                msgId = d["msg_id"] as String,
                lamport = (d["lamport"] as Double).toInt(),
                payload = payload,
                sig = d["sig"] as String,
                type = (d["type"] as? String) ?: "sos",
                version = (d["version"] as Double).toInt(),
                ttl = (d["ttl"] as Double).toInt(),
                scheme = (d["scheme"] as? String) ?: "dev-sha256",
                senderKey = (d["sender_key"] as? String) ?: "",
            )
            if (env.msgId in seen) return             // dedup
            seen.add(env.msgId)
            val ok = env.isProductionTrusted()
            clock = maxOf(clock, env.lamport) + 1     // Lamport update
            val hops = 4 - env.ttl
            onReceived(env, ok, hops)
            if (ok && env.ttl > 0) broadcast(env.copy(ttl = env.ttl - 1))   // multi-hop relay
        } catch (e: Exception) {
            onStatus("Recv error: ${e.message}")
        }
    }
}
