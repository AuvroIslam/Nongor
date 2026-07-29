package org.nongor.app.ui.translate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.nongor.app.core.Assessment
import org.nongor.app.core.ConversationTriage
import org.nongor.app.core.LangInfo
import org.nongor.app.core.Phrase
import org.nongor.app.core.PhraseSearch
import org.nongor.app.core.PhrasebookData
import org.nongor.app.core.Reply
import org.nongor.app.data.PhrasebookRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import androidx.lifecycle.viewModelScope
import org.nongor.app.core.PhraseFinder

data class TranslateState(
    val query: String = "",
    val category: String? = null,
    val targetLang: String? = null,
    val results: List<Phrase> = emptyList(),
    val replies: Map<String, Reply> = emptyMap(),
    /** Index into the guided triage flow, or null when browsing freely. */
    val guidedStep: Int? = null,
    /** The on-device model is still refining the keyword results. */
    val searching: Boolean = false,
    /** The shown results came from the model rather than keyword matching. */
    val usedModel: Boolean = false,
)

class TranslateViewModel(app: Application) : AndroidViewModel(app) {

    private val app = app as org.nongor.app.NongorApplication
    val book: PhrasebookData = PhrasebookRepository.get(app)
    private var findJob: Job? = null
    val speaker = Speaker(app)

    private val _state = MutableStateFlow(TranslateState())
    val state: StateFlow<TranslateState> = _state.asStateFlow()

    /**
     * Find the phrases that match what the volunteer just described.
     *
     * Runs the on-device model when it is installed and falls back to keyword search when it
     * is not — the feature is not allowed to depend on the optional download. The model only
     * ever picks ids from the bundled phrasebook; it is never asked to translate.
     */
    fun setQuery(q: String) {
        _state.value = _state.value.copy(query = q)
        findJob?.cancel()
        if (q.isBlank()) {
            _state.value = _state.value.copy(results = emptyList(), searching = false, usedModel = false)
            return
        }
        findJob = viewModelScope.launch {
            // Keyword results appear instantly; the model refines them a moment later.
            val quick = PhraseSearch.search(book.allPhrases, q)
            _state.value = _state.value.copy(results = quick, searching = engineReady())

            if (!engineReady()) return@launch
            val found = withContext(Dispatchers.Default) {
                PhraseFinder.find(book, q) { system, user ->
                    runBlocking { app.engineHolder.generateWith(system, user, temperature = 0.1) }
                }
            }
            if (_state.value.query != q) return@launch      // the user kept typing
            _state.value = _state.value.copy(
                results = found.phrases.ifEmpty { quick },
                searching = false,
                usedModel = found.source == PhraseFinder.Source.MODEL,
            )
        }
    }

    private fun engineReady(): Boolean = app.engineHolder.isReady()

    fun setCategory(id: String?) {
        _state.value = _state.value.copy(category = id)
    }

    fun setTargetLanguage(code: String?) {
        _state.value = _state.value.copy(targetLang = code)
    }

    fun targetLanguage(): LangInfo? = _state.value.targetLang?.let { book.language(it) }

    fun record(reply: Reply) {
        _state.value = _state.value.copy(replies = _state.value.replies + (reply.phraseId to reply))
    }

    fun clearReply(phraseId: String) {
        _state.value = _state.value.copy(replies = _state.value.replies - phraseId)
    }

    fun clearConversation() {
        _state.value = _state.value.copy(replies = emptyMap(), guidedStep = null)
    }

    fun startGuided() {
        _state.value = _state.value.copy(guidedStep = 0, replies = emptyMap())
    }

    fun stopGuided() {
        _state.value = _state.value.copy(guidedStep = null)
    }

    fun advanceGuided() {
        val step = _state.value.guidedStep ?: return
        val next = step + 1
        _state.value = _state.value.copy(
            guidedStep = if (next >= book.flow.size) null else next,
        )
    }

    fun backGuided() {
        val step = _state.value.guidedStep ?: return
        if (step > 0) _state.value = _state.value.copy(guidedStep = step - 1)
    }

    fun guidedPhrase(): Phrase? =
        _state.value.guidedStep?.let { book.triagePhrases().getOrNull(it) }

    fun assessment(): Assessment = ConversationTriage.assess(_state.value.replies)

    fun handoverNote(bangla: Boolean): String = ConversationTriage.summarise(
        book = book,
        replies = _state.value.replies,
        bangla = bangla,
        languageName = targetLanguage()?.let { if (bangla) it.native else it.name },
    )

    override fun onCleared() {
        speaker.release()
        super.onCleared()
    }
}
