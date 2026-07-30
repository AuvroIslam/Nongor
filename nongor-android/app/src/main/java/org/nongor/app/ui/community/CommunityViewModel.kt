package org.nongor.app.ui.community

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import org.nongor.app.NongorApplication
import org.nongor.app.core.Prompts
import org.nongor.app.core.Safety
import org.nongor.app.data.CommunityKinds
import org.nongor.app.data.CommunityReport
import org.nongor.app.data.download.HfDownloadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CommunityUiState(
    val district: String = "",
    val started: Boolean = false,
    val peers: Int = 0,
    val status: String = "",
    val reports: List<CommunityReport> = emptyList(),
    val otherAreas: List<CommunityReport> = emptyList(),   // shown separately, never hidden
    val counts: Map<String, Int> = emptyMap(),
    val quarantined: Int = 0,
    val summaryBusy: Boolean = false,
    val summary: String? = null,
    /** report id -> how many distinct phones say they can see it too. */
    val confirms: Map<String, Int> = emptyMap(),
    /** report id -> how many say it is not what they see. */
    val disputes: Map<String, Int> = emptyMap(),
    /** This phone's own vote, so the buttons show what you already said. */
    val myVotes: Map<String, String> = emptyMap(),
)

/**
 * Community Intelligence Network: neighbours tag what they see, it spreads over the shared mesh,
 * and Gemma turns the collected reports into one situation briefing — all offline, and scoped to
 * this district so one area's reports never flood another's board.
 */
class CommunityViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as NongorApplication
    private val hub = app.meshHub
    private var acquired = false

    private val _ui = MutableStateFlow(CommunityUiState())
    val ui: StateFlow<CommunityUiState> = _ui

    init {
        // Warm the model so the situation briefing can run.
        viewModelScope.launch {
            if (!app.engineHolder.isReady()) {
                val m = HfDownloadRepository.modelFile(getApplication())
                if (m.exists()) app.engineHolder.loadModel(m)
            }
        }
        // NOTE: every mutation below uses atomic update{} — several collectors write concurrently,
        // and a read-modify-write across the suspending GPS lookup would clobber the others.
        viewModelScope.launch {
            val d = hub.currentDistrict()
            _ui.update { it.copy(district = d) }
            refresh()
        }
        viewModelScope.launch { hub.communityReports.collect { refresh() } }
        viewModelScope.launch { app.communityRepository.quarantine.collect { q ->
            _ui.update { it.copy(quarantined = q.size) } } }
        viewModelScope.launch { hub.started.collect { v -> _ui.update { it.copy(started = v) } } }
        viewModelScope.launch { hub.peers.collect { v -> _ui.update { it.copy(peers = v) } } }
        viewModelScope.launch { hub.status.collect { v -> _ui.update { it.copy(status = v) } } }
        viewModelScope.launch {
            app.communityRepository.confirms.collect { m ->
                _ui.update { it.copy(confirms = m.mapValues { (_, v) -> v.size }) }
            }
        }
        viewModelScope.launch {
            app.communityRepository.disputes.collect { m ->
                _ui.update { it.copy(disputes = m.mapValues { (_, v) -> v.size }) }
            }
        }
        viewModelScope.launch {
            app.communityRepository.myVotes.collect { m -> _ui.update { it.copy(myVotes = m) } }
        }
    }

    fun enter() { if (!acquired) { acquired = true; hub.acquire() } }
    fun leave() { if (acquired) { acquired = false; hub.release() } }

    /** "I can see this too" / "that is not what I see", shared over the mesh. */
    fun vote(reportId: String, up: Boolean) = hub.voteOnReport(reportId, up)

    fun post(kind: String, note: String) {
        viewModelScope.launch { hub.sendReport(kind, note.trim()) }
    }

    /**
     * Split the board by area: this district's reports lead (so one area's news doesn't flood
     * another's), but reports from elsewhere are still listed separately — never silently dropped.
     */
    private fun refresh() {
        val d = _ui.value.district
        val all = hub.communityReports.value
        val mine = all.filter {
            d.isBlank() || it.districtEn.equals(d, ignoreCase = true) || it.districtEn.isBlank()
        }
        val others = all - mine.toSet()
        _ui.update {
            it.copy(reports = mine, otherAreas = others,
                counts = mine.groupingBy { r -> r.kind }.eachCount())
        }
    }

    fun summarize() {
        if (_ui.value.summaryBusy) return
        val reports = _ui.value.reports
        if (reports.isEmpty()) return
        viewModelScope.launch {
            _ui.update { it.copy(summaryBusy = true, summary = null) }
            try {
                val facts = buildFacts(reports, _ui.value.counts, _ui.value.district)
                val answer = if (app.engineHolder.isReady()) {
                    app.engineHolder.respondInBangla = app.prefs.isBangla
                    app.engineHolder.generateWith(
                        Prompts.COMMUNITY_SUMMARY_SYSTEM, Safety.wrapAsData(facts), 0.3).trim()
                } else facts
                _ui.update { it.copy(summaryBusy = false, summary = answer) }
            } catch (e: Exception) {
                _ui.update { it.copy(summaryBusy = false,
                    summary = if (app.prefs.isBangla) "সারাংশ তৈরি করা গেল না।" else "Could not build the summary.") }
            }
        }
    }

    /** Code computes the counts; Gemma only writes the prose. */
    private fun buildFacts(reports: List<CommunityReport>, counts: Map<String, Int>, district: String): String =
        buildString {
            append("District: $district. ${reports.size} community reports from nearby phones.\n")
            append("Counts by type:\n")
            counts.forEach { (kind, n) -> append("- ${CommunityKinds.byId(kind).en}: $n\n") }
            append("Reports:\n")
            reports.take(20).forEach { r ->
                val label = CommunityKinds.byId(r.kind).en
                append("- [$label]${if (r.note.isNotBlank()) " ${r.note}" else ""}\n")
            }
        }

    override fun onCleared() { leave() }
}
