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

data class TranslateState(
    val query: String = "",
    val category: String? = null,
    val targetLang: String? = null,
    val results: List<Phrase> = emptyList(),
    val replies: Map<String, Reply> = emptyMap(),
    /** Index into the guided triage flow, or null when browsing freely. */
    val guidedStep: Int? = null,
)

class TranslateViewModel(app: Application) : AndroidViewModel(app) {

    val book: PhrasebookData = PhrasebookRepository.get(app)
    val speaker = Speaker(app)

    private val _state = MutableStateFlow(TranslateState())
    val state: StateFlow<TranslateState> = _state.asStateFlow()

    fun setQuery(q: String) {
        _state.value = _state.value.copy(
            query = q,
            results = if (q.isBlank()) emptyList() else PhraseSearch.search(book.phrases, q),
        )
    }

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
            guidedStep = if (next >= book.triageFlow.size) null else next,
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
