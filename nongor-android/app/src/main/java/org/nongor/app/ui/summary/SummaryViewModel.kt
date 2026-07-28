package org.nongor.app.ui.summary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import org.nongor.app.NongorApplication
import org.nongor.app.core.Gis
import org.nongor.app.core.Summary
import org.nongor.app.data.RegionAssets
import org.nongor.app.data.download.HfDownloadRepository
import org.nongor.app.inference.GemmaLlmEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SummaryUiState(
    val engineLoading: Boolean = true,
    val engineReady: Boolean = false,
    val busy: Boolean = false,
    val reportCount: Int = 0,
    val quarantineCount: Int = 0,
    val briefing: String? = null,
    val stats: Summary.Stats? = null,
    val error: String? = null,
)

class SummaryViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as NongorApplication
    private val engine by lazy { GemmaLlmEngine(app.engineHolder) }

    private val _ui = MutableStateFlow(SummaryUiState())
    val ui: StateFlow<SummaryUiState> = _ui

    init {
        // Live counts — a coordinator watching this screen during an active incident must see
        // new (or newly quarantined) reports as they arrive, not just on next screen visit.
        viewModelScope.launch {
            app.sosRepository.entries.collect { list ->
                _ui.value = _ui.value.copy(reportCount = list.size)
            }
        }
        viewModelScope.launch {
            app.sosRepository.quarantine.collect { list ->
                _ui.value = _ui.value.copy(quarantineCount = list.size)
            }
        }
        viewModelScope.launch {
            if (!app.engineHolder.isReady()) {
                val model = HfDownloadRepository.modelFile(getApplication())
                if (model.exists()) app.engineHolder.loadModel(model)
            }
            _ui.value = _ui.value.copy(engineLoading = false, engineReady = app.engineHolder.isReady())
        }
    }

    fun generate() {
        if (_ui.value.busy) return
        viewModelScope.launch {
            val reports = app.sosRepository.reports()
            if (reports.isEmpty()) {
                _ui.value = _ui.value.copy(briefing = null, stats = null, error = null)
                return@launch
            }
            _ui.value = _ui.value.copy(busy = true, error = null)
            try {
                val ctx = getApplication<Application>()
                val region = app.prefs.activeRegion.value
                val results = app.sosRepository.triageResults()
                val shelters = RegionAssets.loadShelters(ctx, region)
                val graph = RegionAssets.loadGraph(ctx, region)
                val flood = RegionAssets.loadFloodPolys(ctx, region)
                val blocked = graph.edges.mapIndexedNotNull { i, (u, v) ->
                    if (Gis.segmentCrossesFlood(graph.nodes.getValue(u), graph.nodes.getValue(v), flood))
                        "seg_$i" else null
                }
                val newSince = app.sosRepository.newSince()
                val ready = app.engineHolder.isReady()
                app.engineHolder.respondInBangla = app.prefs.isBangla
                val out = withContext(Dispatchers.Default) {
                    Summary.disasterSummary(
                        reports, results, shelters = shelters, blockedRoads = blocked,
                        newSince = newSince, gemma = if (ready) engine else null)
                }
                app.sosRepository.markBriefed()
                _ui.value = _ui.value.copy(busy = false, briefing = out.briefing, stats = out.stats)
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(busy = false, error = e.message)
            }
        }
    }
}
