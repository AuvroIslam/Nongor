package org.nongor.app.ui.firstaid

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import org.nongor.app.NongorApplication
import org.nongor.app.core.Rag
import org.nongor.app.data.RegionAssets
import org.nongor.app.data.download.HfDownloadRepository
import org.nongor.app.inference.GemmaLlmEngine
import org.nongor.app.util.decodeDownscaledToCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class FirstAidUiState(
    val engineLoading: Boolean = true,
    val engineReady: Boolean = false,
    val busy: Boolean = false,
    val answer: String? = null,
    val citations: List<Rag.Citation> = emptyList(),
    val redFlag: Boolean = false,
    val imagePath: String? = null,
    val error: String? = null,
)

class FirstAidViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as NongorApplication
    private val engine by lazy { GemmaLlmEngine(app.engineHolder) }
    private val retriever by lazy { Rag.KeywordRetriever(RegionAssets.loadFirstAid(getApplication())) }

    private val _ui = MutableStateFlow(FirstAidUiState())
    val ui: StateFlow<FirstAidUiState> = _ui

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
                val file = decodeDownscaledToCache(ctx, uri, prefix = "firstaid")
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

    fun ask(query: String) {
        val q = query.trim()
        val img = _ui.value.imagePath
        if ((q.isEmpty() && img == null) || _ui.value.busy) return
        viewModelScope.launch {
            _ui.value = _ui.value.copy(busy = true, error = null, answer = null, citations = emptyList())
            try {
                val useGemma = app.engineHolder.isReady()
                app.engineHolder.respondInBangla = app.prefs.isBangla
                // Hybrid retrieval: the knowledge base has English tags only (one source of truth).
                // English queries match directly. A Bangla query that matches nothing is translated
                // to English once, then retried — so we only pay the extra call when tags miss.
                var searchQ = q
                if (useGemma && hasNonLatin(q)) {
                    val direct = withContext(Dispatchers.Default) { retriever.search(q, 1) }
                    if (direct.isEmpty()) {
                        val english = app.engineHolder.translateToEnglish(q)
                        if (english.isNotBlank()) searchQ = english
                    }
                }
                val ans = withContext(Dispatchers.Default) {
                    // The photo (if any) only reaches Gemma; it augments understanding, but the
                    // written steps still come from the retrieved passages.
                    Rag.firstAidAnswer(
                        q, retriever, if (useGemma) engine else null, k = 4, searchQuery = searchQ,
                        imagePath = if (useGemma) img else null)
                }
                _ui.value = _ui.value.copy(
                    busy = false, answer = ans.answer, citations = ans.citations, redFlag = ans.redFlag,
                    imagePath = null)
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(busy = false, error = e.message)
            }
        }
    }

    /** True if the text contains Bangla characters (U+0980–U+09FF). */
    private fun hasNonLatin(s: String): Boolean = s.any { it.code in 0x0980..0x09FF }
}
