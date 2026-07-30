package org.nongor.app.mesh

import android.app.Application
import android.os.Build
import org.nongor.app.core.FamilyCrypto
import org.nongor.app.core.SosReport
import org.nongor.app.core.Triage
import org.nongor.app.core.SignedEnvelope
import org.nongor.app.data.AppPrefs
import org.nongor.app.data.BdGeo
import org.nongor.app.data.CommunityReport
import org.nongor.app.data.CommunityRepository
import org.nongor.app.data.FamilyMember
import org.nongor.app.data.FamilyRepository
import org.nongor.app.data.Regions
import org.nongor.app.data.SosEntry
import org.nongor.app.data.SosRepository
import org.nongor.app.location.LocationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** A single received/sent SOS line for the Mesh SOS screen. */
data class MeshMsg(
    val text: String,
    val sender: String,
    val verified: Boolean,
    val hops: Int,
    val priority: String,
    val color: String,
    val mine: Boolean,
    /** The envelope id, so acknowledgements coming back can be matched to this line. */
    val msgId: String = "",
    /** True once the sender has said they are safe. */
    val resolved: Boolean = false,
)

/**
 * App-scoped owner of the one mesh radio. Both the Mesh SOS screen and the Community Intelligence
 * screen share it, so there is a single Nearby-Connections session and a single signed-envelope
 * pipeline. Incoming envelopes are routed by [SignedEnvelope.type] — "community" reports build the
 * shared area board, everything else is treated as an SOS — and unverified (forged) envelopes are
 * quarantined in both paths, never merged into the trusted data.
 */
class MeshHub(
    private val app: Application,
    private val sosRepo: SosRepository,
    private val communityRepo: CommunityRepository,
    private val familyRepo: FamilyRepository,
    private val location: LocationProvider,
    private val prefs: AppPrefs,
) {
    val localName = "Nongor ${Build.MODEL}".take(48)

    private var mgr: MeshManager? = null
    private var refs = 0
    private var stopJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /**
     * Messages composed with nobody in range. Lives on the hub, not on [MeshManager], so stopping
     * and restarting the radio never discards something the user was told is pending.
     */
    private val outbox = MeshOutbox()

    private companion object {
        /**
         * Navigating between two mesh screens drops the ref count to 0 for an instant before the
         * incoming screen acquires. Tearing the radio down on that would drop every live
         * connection and rebuild it under fresh endpoint ids, so wait out the transition first.
         */
        const val STOP_GRACE_MS = 3_000L
    }

    private val _status = MutableStateFlow("Idle")
    val status: StateFlow<String> = _status
    private val _peers = MutableStateFlow(0)
    val peers: StateFlow<Int> = _peers
    private val _started = MutableStateFlow(false)
    val started: StateFlow<Boolean> = _started
    private val _sosMessages = MutableStateFlow<List<MeshMsg>>(emptyList())
    val sosMessages: StateFlow<List<MeshMsg>> = _sosMessages

    val communityReports: StateFlow<List<CommunityReport>> get() = communityRepo.entries
    val familyMembers: StateFlow<List<FamilyMember>> get() = familyRepo.members

    /** Screens call acquire() on enter and release() on leave; the radio runs while anyone needs it. */
    @Synchronized
    fun acquire() {
        refs++
        // A screen came back within the grace period — cancel the pending shutdown and keep the
        // existing connections rather than rebuilding them.
        stopJob?.cancel(); stopJob = null
        if (mgr == null) {
            mgr = MeshManager(
                app, localName, outbox,
                onStatus = { _status.value = it },
                onPeersChanged = { _peers.value = it },
                onReceived = ::onReceived,
            ).also { it.start() }
            _started.value = true
        }
    }

    @Synchronized
    fun release() {
        refs = (refs - 1).coerceAtLeast(0)
        if (refs > 0) return
        stopJob?.cancel()
        stopJob = scope.launch {
            delay(STOP_GRACE_MS)
            synchronized(this@MeshHub) {
                if (refs > 0) return@synchronized      // reacquired while we waited
                mgr?.stop(); mgr = null
                _started.value = false; _peers.value = 0
                stopJob = null
            }
        }
    }

    /** Messages composed with no peer in range, still waiting for one. */
    fun pendingCount(): Int = outbox.size()

    suspend fun sendSos(text: String, msgId: String? = null) {
        val t = text.trim(); if (t.isEmpty()) return
        val m = mgr ?: return
        val loc = location.current()
        val region = Regions.byId(prefs.activeRegion.value)
        val lat = loc?.first ?: region.centerLat
        val lon = loc?.second ?: region.centerLon
        val env = m.sendSos(t, lat, lon, msgId)
        // A repeat of a call already on the log is the same call, not a new one. Without this
        // a two-minute hold on the SOS button left five identical cases in the store, and the
        // coordinator briefing counted all five.
        if (_sosMessages.value.any { it.msgId == env.msgId }) return
        // The envelope id is carried into the stored report so an acknowledgement arriving two
        // hops later can be matched back to the exact message it is answering.
        val sos = SosReport(
            msgId = env.msgId, text = t, lat = lat, lon = lon, reporterRole = "volunteer")
        val tr = Triage.fallbackTriage(sos)
        sosRepo.add(SosEntry(sos, tr, source = "mesh_sent"))
        _sosMessages.value = _sosMessages.value +
            MeshMsg(t, localName, true, 0, tr.priority, tr.color, mine = true, msgId = env.msgId)
    }

    suspend fun sendReport(kind: String, note: String) {
        val m = mgr ?: return
        val loc = location.current()
        val district = if (loc != null) BdGeo.nearestDistrict(app, loc.first, loc.second).name
        else Regions.byId(prefs.activeRegion.value).nameEn
        m.sendReport(kind, note, district, loc?.first, loc?.second)
        communityRepo.add(
            CommunityReport(kind = kind, note = note, districtEn = district,
                lat = loc?.first, lon = loc?.second, sender = localName, verified = true, mine = true))
    }

    /**
     * Announce this phone to its family. Only a hashed family tag and an AES-sealed name go out, so
     * anyone outside the family who hears it learns nothing.
     */
    suspend fun sendPresence() {
        val m = mgr ?: return
        val code = prefs.familyCode.value
        val name = prefs.memberName.value
        if (code.isBlank() || name.isBlank()) return
        val loc = location.current()
        m.sendPresence(FamilyCrypto.tag(code), FamilyCrypto.seal(code, name), loc?.first, loc?.second)
    }

    /**
     * Tell the sender their SOS reached a human.
     *
     * Called when a received SOS is actually on screen, not when it arrives - the whole value
     * of the receipt is that it means a person looked, so acknowledging on delivery would make
     * it a lie. Sent at most once per message per device, and never for our own messages or
     * for ones that failed their signature check.
     */
    fun markSeen(msgId: String) {
        val m = mgr ?: return
        if (msgId.isBlank()) return
        if (!sosRepo.claimAck(msgId)) return
        runCatching { m.sendSeen(msgId) }
    }

    /**
     * Say whether you can see what somebody else reported.
     *
     * Recorded locally under this phone's own name first, so the count moves the instant you
     * tap even with nobody in range, then broadcast for everyone else.
     */
    fun voteOnReport(reportId: String, up: Boolean) {
        if (reportId.isBlank()) return
        communityRepo.recordVote(reportId, localName, up)
        communityRepo.setMyVote(reportId, up)
        mgr?.let { m -> runCatching { m.sendVote(reportId, up) } }
    }

    /**
     * Tell everyone the call for help is over.
     *
     * Sent when the person presses "I am safe now". It relays as far as the original SOS did,
     * because the phones that need to hear it are exactly the ones that heard the call.
     */
    fun markSafe(msgId: String) {
        val m = mgr ?: return
        if (msgId.isBlank()) return
        sosRepo.markResolved(msgId)
        _sosMessages.value = _sosMessages.value.map {
            if (it.msgId == msgId) it.copy(resolved = true) else it
        }
        runCatching { m.sendSafe(msgId) }
    }

    /** Our own current position, for working out how far away a family member was last seen. */
    suspend fun myLocation(): Pair<Double, Double>? = location.current()

    /** The district we consider "here" — GPS if available, else the active region. */
    suspend fun currentDistrict(): String {
        val loc = location.current()
        return if (loc != null) BdGeo.nearestDistrict(app, loc.first, loc.second).name
        else Regions.byId(prefs.activeRegion.value).nameEn
    }

    /** Bumped whenever a new acknowledgement lands, so collectors recompose. */
    private val _seenTick = MutableStateFlow(0)
    val seenTick: StateFlow<Int> = _seenTick

    private fun onReceived(env: SignedEnvelope, ok: Boolean, hops: Int) {
        when (env.type) {
            "presence" -> {
                // Family Reunion: only record it if this phone is in that family — the tag must
                // match and the sealed name must actually decrypt with our shared code.
                if (!ok) return
                val code = prefs.familyCode.value
                if (code.isBlank()) return
                if (env.payload["tag"] as? String != FamilyCrypto.tag(code)) return
                val name = FamilyCrypto.open(code, env.payload["who"] as? String ?: "") ?: return
                if (name.equals(prefs.memberName.value, ignoreCase = true)) return   // our own echo
                familyRepo.record(
                    FamilyMember(
                        name = name,
                        lastSeenTs = System.currentTimeMillis(),
                        lat = (env.payload["lat"] as? String)?.toDoubleOrNull(),
                        lon = (env.payload["lon"] as? String)?.toDoubleOrNull(),
                        hops = hops,
                    ),
                )
            }
            "vote" -> {
                // A vote only means something if we can tell distinct phones apart, so an
                // unverified one is discarded rather than counted.
                if (!ok) return
                val forId = env.payload["for"] as? String ?: return
                val up = (env.payload["v"] as? String) == "up"
                communityRepo.recordVote(forId, env.sender, up)
            }
            "safe" -> {
                // The sender says they are out of danger. Unverified envelopes are ignored: a
                // forged stand-down would call off a rescue that is still needed, which is the
                // most dangerous message this app could carry.
                if (!ok) return
                val forId = env.payload["for"] as? String ?: return
                if (sosRepo.markResolved(forId)) {
                    _sosMessages.value = _sosMessages.value.map {
                        if (it.msgId == forId) it.copy(resolved = true) else it
                    }
                }
            }
            "seen" -> {
                // Someone had our SOS on their screen. Record it against the original message.
                // Unverified envelopes are dropped: a forged receipt would tell a person in
                // danger that help has their message when nobody does, which is worse than
                // showing nothing at all.
                if (!ok) return
                val forId = env.payload["for"] as? String ?: return
                if (sosRepo.recordSeen(forId, env.sender)) {
                    _seenTick.value = _seenTick.value + 1
                }
            }
            "community" -> {
                val r = CommunityReport(
                    id = env.msgId,
                    kind = env.payload["kind"] as? String ?: "danger",
                    note = env.payload["note"] as? String ?: "",
                    districtEn = env.payload["district"] as? String ?: "",
                    lat = (env.payload["lat"] as? String)?.toDoubleOrNull(),
                    lon = (env.payload["lon"] as? String)?.toDoubleOrNull(),
                    sender = env.sender, verified = ok, mine = false,
                )
                if (ok) communityRepo.add(r) else communityRepo.addQuarantined(r)
            }
            else -> {
                val text = env.payload["text"] as? String ?: ""
                val lat = (env.payload["lat"] as? String)?.toDoubleOrNull()
                val lon = (env.payload["lon"] as? String)?.toDoubleOrNull()
                val sos = SosReport(msgId = env.msgId, text = text, lat = lat, lon = lon, hops = hops)
                val tr = Triage.fallbackTriage(sos)
                val entry = SosEntry(sos, tr, source = "mesh_recv", verified = ok, hops = hops)
                if (ok) sosRepo.add(entry) else sosRepo.addQuarantined(entry)
                _sosMessages.value = _sosMessages.value +
                    MeshMsg(
                        text, env.sender, ok, hops, tr.priority, tr.color,
                        mine = false, msgId = env.msgId,
                    )
            }
        }
    }
}
