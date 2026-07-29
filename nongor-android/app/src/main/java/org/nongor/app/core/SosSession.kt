package org.nongor.app.core

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * An SOS in progress, owned by the application rather than by a screen.
 *
 * This started as state inside the mesh screen's view model, which was wrong in a way that
 * only shows up in the situation the feature exists for: press SOS, put the phone in your
 * pocket, and the screen leaves composition — taking the broadcast and the siren with it.
 * Someone climbing onto a roof one-handed is exactly the person who will not keep an app
 * screen in the foreground.
 *
 * So the session lives for as long as the process does. The two obligations that come with
 * that are: it must keep re-broadcasting until stopped, and it must stay stoppable from
 * anywhere in the app — which is why the home screen grows a banner while [active] is true.
 */
class SosSession(
    context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {

    private val siren = Siren(context)

    private val _active = MutableStateFlow(false)
    val active: StateFlow<Boolean> = _active.asStateFlow()

    private val _sirenOn = MutableStateFlow(false)
    val sirenOn: StateFlow<Boolean> = _sirenOn.asStateFlow()

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    private var repeat: Job? = null

    /**
     * Start broadcasting and sounding the alarm.
     *
     * [send] is re-invoked on a timer rather than once, because the phone that will relay
     * this may not be in range yet — the neighbour who walks past in four minutes is the
     * whole point of a store-and-forward mesh.
     */
    fun start(text: String, send: suspend (String) -> Unit) {
        if (_active.value) return
        val body = text.ifBlank { DEFAULT_MESSAGE }
        _message.value = body
        _active.value = true
        siren.start()
        _sirenOn.value = siren.isPlaying
        repeat = scope.launch {
            while (isActive) {
                runCatching { send(body) }
                delay(RESEND_INTERVAL_MS)
            }
        }
    }

    fun stop() {
        repeat?.cancel()
        repeat = null
        siren.stop()
        _sirenOn.value = false
        _active.value = false
    }

    /**
     * Silence the alarm without giving up the broadcast.
     *
     * There are moments in a rescue when the noise has to stop — a responder shouting a
     * question, a team listening for someone else buried nearby — and losing the radio
     * broadcast at the same time would be a bad trade.
     */
    fun toggleSiren() {
        siren.toggle()
        _sirenOn.value = siren.isPlaying
    }

    companion object {
        const val RESEND_INTERVAL_MS = 30_000L
        const val DEFAULT_MESSAGE = "SOS — need help at this location."
    }
}
