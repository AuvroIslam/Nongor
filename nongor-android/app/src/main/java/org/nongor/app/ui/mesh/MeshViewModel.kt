package org.nongor.app.ui.mesh

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import org.nongor.app.NongorApplication
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
)

/** Thin wrapper over the app-scoped [org.nongor.app.mesh.MeshHub] shared with Community. */
class MeshViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as NongorApplication
    private val hub = app.meshHub
    val localName = hub.localName
    private var acquired = false

    private val _ui = MutableStateFlow(MeshUiState())
    val ui: StateFlow<MeshUiState> = _ui

    init {
        // Mirror the shared hub's state into this screen's UI. Atomic update{} — these collectors
        // run concurrently, so a plain read-modify-write would clobber each other.
        viewModelScope.launch { hub.started.collect { v -> _ui.update { it.copy(started = v) } } }
        viewModelScope.launch { hub.status.collect { v -> _ui.update { it.copy(status = v) } } }
        viewModelScope.launch { hub.peers.collect { v -> _ui.update { it.copy(peers = v) } } }
        viewModelScope.launch { hub.sosMessages.collect { v -> _ui.update { it.copy(messages = v) } } }
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

    fun stop() {
        if (!acquired) return
        acquired = false
        hub.release()
    }

    override fun onCleared() {
        stop()
    }
}
