package org.nongor.app.ui.mesh

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import org.nongor.app.NongorApplication
import org.nongor.app.core.SmsBridge
import org.nongor.app.core.SosReport
import org.nongor.app.core.Triage
import org.nongor.app.mesh.MeshMsg
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MeshUiState(
    val started: Boolean = false,
    val status: String = "Idle",
    val peers: Int = 0,
    val messages: List<MeshMsg> = emptyList(),
    /** A one-press SOS is being re-broadcast on a timer. */
    val sosActive: Boolean = false,
    val sirenOn: Boolean = false,
    /** Message id -> the phones that have confirmed seeing it. */
    val seenBy: Map<String, Set<String>> = emptyMap(),
)

/** Thin wrapper over the app-scoped [org.nongor.app.mesh.MeshHub] shared with Community. */
class MeshViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as NongorApplication
    private val hub = app.meshHub
    val localName = hub.localName

    /** The name the user set for family reunion, reused so an SMS SOS says who is asking. */
    val reporterName: String? get() = app.prefs.memberName.value.ifBlank { null }

    private var acquired = false

    private val _ui = MutableStateFlow(MeshUiState())
    val ui: StateFlow<MeshUiState> = _ui

    /**
     * Tell the sender that their SOS is on our screen.
     *
     * Driven by the row appearing, not by the message arriving, because the receipt only
     * means anything if a person actually looked. Idempotent - the hub drops repeats.
     */
    fun markSeen(msgId: String) = hub.markSeen(msgId)

    init {
        // Mirror the shared hub's state into this screen's UI. Atomic update{} — these collectors
        // run concurrently, so a plain read-modify-write would clobber each other.
        viewModelScope.launch { hub.started.collect { v -> _ui.update { it.copy(started = v) } } }
        viewModelScope.launch { hub.status.collect { v -> _ui.update { it.copy(status = v) } } }
        viewModelScope.launch { hub.peers.collect { v -> _ui.update { it.copy(peers = v) } } }
        viewModelScope.launch { hub.sosMessages.collect { v -> _ui.update { it.copy(messages = v) } } }
        viewModelScope.launch {
            app.sosRepository.seenBy.collect { v -> _ui.update { it.copy(seenBy = v) } }
        }
        viewModelScope.launch { app.sosSession.active.collect { v -> _ui.update { it.copy(sosActive = v) } } }
        viewModelScope.launch { app.sosSession.sirenOn.collect { v -> _ui.update { it.copy(sirenOn = v) } } }
    }

    fun start() {
        if (acquired) return
        acquired = true
        hub.acquire()
    }

    fun send(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch { hub.sendSos(text) }
    }

    /**
     * Turn the composed SOS into a one-line code that can be sent as an ordinary SMS.
     *
     * The same deterministic triage that ranks a mesh SOS runs here, so the priority letter
     * in the SMS means exactly what the coloured priority in the app means. Location comes
     * from the same GPS fix the mesh would have attached; if there is no fix the code simply
     * carries no coordinates rather than inventing one.
     */
    fun buildSmsCode(text: String, reporterName: String?, onReady: (String) -> Unit) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val loc = runCatching { hub.myLocation() }.getOrNull()
            val report = SosReport(text = text, lat = loc?.first, lon = loc?.second)
            val triage = Triage.fallbackTriage(report)
            onReady(
                SmsBridge.encode(
                    priority = triage.priority,
                    lat = report.lat,
                    lon = report.lon,
                    peopleCount = report.people,
                    signals = triage.riskSignals,
                    name = reporterName,
                ),
            )
        }
    }

    // ---- one-press SOS -----------------------------------------------------------------
    // Delegated to the app-scoped session so an SOS survives this screen being closed.

    private val sos = app.sosSession

    /** The id of the call currently going out, so "I am safe now" can name it. */
    private var activeSosId: String? = null

    fun pressSos(text: String) {
        // One id for the whole session, so every repeat is understood as the same call for
        // help rather than a new emergency each time the timer fires.
        val id = java.util.UUID.randomUUID().toString()
        activeSosId = id
        sos.start(text) { hub.sendSos(it, id) }
    }

    /**
     * Stop broadcasting, and say why.
     *
     * Stopping the siren on this phone is the easy half. The half that matters is telling the
     * phones that heard the call that it is over, so nobody sets out for someone who is already
     * safe.
     */
    fun stopSos() {
        sos.stop()
        activeSosId?.let { hub.markSafe(it) }
        activeSosId = null
    }

    fun toggleSiren() = sos.toggleSiren()

    fun stop() {
        if (!acquired) return
        acquired = false
        hub.release()
    }

    override fun onCleared() {
        // Deliberately does NOT stop the SOS: leaving this screen is not a decision to stop
        // asking for help. The home screen carries a banner to stop it from anywhere.
        stop()
    }
}
