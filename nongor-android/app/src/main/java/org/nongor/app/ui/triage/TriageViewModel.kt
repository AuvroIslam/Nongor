package org.nongor.app.ui.triage

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import org.nongor.app.NongorApplication
import org.nongor.app.core.Prompts
import org.nongor.app.core.SosReport
import org.nongor.app.core.Triage
import org.nongor.app.core.TriageResult
import org.nongor.app.data.SosEntry
import org.nongor.app.data.download.HfDownloadRepository
import org.nongor.app.inference.GemmaLlmEngine
import org.nongor.app.util.decodeDownscaledToCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class TriagedItem(val text: String, val result: TriageResult)

data class TriageUiState(
    val engineLoading: Boolean = true,
    val engineReady: Boolean = false,
    val busy: Boolean = false,
    val queue: List<TriagedItem> = emptyList(),
    val imagePath: String? = null,
    val error: String? = null,
)

class TriageViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as NongorApplication
    private val engine by lazy { GemmaLlmEngine(app.engineHolder) }
    private val rank = mapOf("critical" to 0, "high" to 1, "moderate" to 2, "low" to 3)

    private val _ui = MutableStateFlow(TriageUiState())
    val ui: StateFlow<TriageUiState> = _ui

    init {
        viewModelScope.launch {
            if (!app.engineHolder.isReady()) {
                val model = HfDownloadRepository.modelFile(getApplication())
                if (model.exists()) app.engineHolder.loadModel(model)
            }
            _ui.value = _ui.value.copy(engineLoading = false, engineReady = app.engineHolder.isReady())
        }
    }

    fun setImageFromUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ctx = getApplication<Application>()
                // Downscale before it ever reaches Gemma — a raw camera photo can OOM the model.
                val file = decodeDownscaledToCache(ctx, uri, prefix = "triage")
                    ?: throw IllegalStateException("could not read image")
                _ui.value = _ui.value.copy(imagePath = file.absolutePath)
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(error = "Image: ${e.message}")
            }
        }
    }

    fun clearImage() {
        _ui.value = _ui.value.copy(imagePath = null)
    }

    fun triage(text: String) {
        val t = text.trim()
        val img = _ui.value.imagePath
        if ((t.isEmpty() && img == null) || _ui.value.busy) return
        viewModelScope.launch {
            _ui.value = _ui.value.copy(busy = true, error = null)
            try {
                val ready = app.engineHolder.isReady()
                app.engineHolder.respondInBangla = app.prefs.isBangla
                val loc = app.locationProvider.current()
                val sos = SosReport(
                    text = t.ifEmpty { "(photo of the scene)" }, imagePath = img,
                    lat = loc?.first, lon = loc?.second, reporterRole = "volunteer",
                )
                val result = withContext(Dispatchers.Default) {
                    if (ready && img != null) {
                        val raw = app.engineHolder.generateWith(
                            Prompts.TRIAGE_SYSTEM, Triage.triageUserPrompt(sos.text),
                            temperature = 0.3, imagePath = img)
                        Triage.fromRawOrFallback(sos, raw, "gemma-4-e2b")
                    } else {
                        Triage.triageSos(sos, if (ready) engine else null)
                    }
                }
                app.sosRepository.add(SosEntry(sos, result, source = "triage"))
                val label = if (img != null) "📷 ${sos.text}" else sos.text
                val queue = (_ui.value.queue + TriagedItem(label, result))
                    .sortedWith(compareBy({ rank[it.result.priority] }, { -it.result.urgencyScore }))
                _ui.value = _ui.value.copy(busy = false, queue = queue, imagePath = null)
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(busy = false, error = e.message)
            }
        }
    }

    fun clearQueue() {
        _ui.value = _ui.value.copy(queue = emptyList())
    }
}
