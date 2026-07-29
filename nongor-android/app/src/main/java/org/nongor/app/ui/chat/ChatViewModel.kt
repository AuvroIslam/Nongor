package org.nongor.app.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import org.nongor.app.NongorApplication
import org.nongor.app.actions.AssistantAction
import org.nongor.app.actions.AssistantActionParser
import org.nongor.app.data.ConversationEntity
import org.nongor.app.data.MessageEntity
import org.nongor.app.data.download.HfDownloadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class ChatUiState(
    val engineLoading: Boolean = true,
    val engineReady: Boolean = false,
    val engineError: String? = null,
    val sending: Boolean = false,
    val sendError: String? = null,
    val pendingAction: AssistantAction? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as NongorApplication

    private val titleMutex = Mutex()

    private val _conversationId = MutableStateFlow<Long?>(null)
    val conversationId: StateFlow<Long?> = _conversationId

    private val _ui = MutableStateFlow(ChatUiState())
    val ui: StateFlow<ChatUiState> = _ui

    val messages: StateFlow<List<MessageEntity>> = _conversationId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else app.chatRepository.observeMessages(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val conversations: StateFlow<List<ConversationEntity>> =
        app.chatRepository.observeConversations()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            val latest = app.chatRepository.getAllConversations().firstOrNull()
            val id = if (latest != null && app.chatRepository.getMessagesOrdered(latest.id).isEmpty()) {
                latest.id
            } else {
                app.chatRepository.createConversation()
            }
            _conversationId.value = id
            loadEngine()
        }
    }

    private suspend fun loadEngine() {
        _ui.value = _ui.value.copy(engineLoading = true, engineError = null)
        val model = HfDownloadRepository.modelFile(getApplication())
        if (!model.exists()) {
            _ui.value = ChatUiState(
                engineLoading = false,
                engineReady = false,
                engineError = "Model not found. Complete onboarding to download Gemma 4 E2B.",
            )
            return
        }
        val result = app.engineHolder.loadModel(model)
        _ui.value = if (result.isSuccess) {
            backfillConversationTitles()
            ChatUiState(engineLoading = false, engineReady = true)
        } else {
            ChatUiState(
                engineLoading = false,
                engineReady = false,
                engineError = result.exceptionOrNull()?.message ?: "Failed to load model",
            )
        }
    }

    private suspend fun backfillConversationTitles() {
        val conversations = app.chatRepository.getAllConversations()
        conversations.forEach { conversation ->
            maybeGenerateConversationTitleNow(conversation.id)
        }
    }

    fun retryLoadEngine() {
        viewModelScope.launch { loadEngine() }
    }

    fun clearSendError() {
        _ui.value = _ui.value.copy(sendError = null)
    }

    fun clearPendingAction() {
        _ui.value = _ui.value.copy(pendingAction = null)
    }

    fun newChat() {
        viewModelScope.launch {
            app.engineHolder.resetConversation()
            val newId = app.chatRepository.createConversation()
            _conversationId.value = newId
        }
    }

    fun openConversation(id: Long) {
        viewModelScope.launch {
            app.engineHolder.resetConversation()
            _conversationId.value = id
        }
    }

    /**
     * When the conversation still has no title, ask the model (via a separate temp conversation)
     * for a short title from the first user turn + first assistant reply, then persist it.
     */
    private fun maybeGenerateConversationTitle(conversationId: Long) {
        viewModelScope.launch {
            maybeGenerateConversationTitleNow(conversationId)
        }
    }

    private suspend fun maybeGenerateConversationTitleNow(conversationId: Long) {
        titleMutex.withLock {
            val conv = app.chatRepository.getConversation(conversationId) ?: return@withLock
            if (conv.title.isNotBlank()) return@withLock
            val messages = app.chatRepository.getMessagesOrdered(conversationId)
            val firstUser = messages.firstOrNull { it.role == "user" }?.text?.trim().orEmpty()
            val firstAssistant = messages.firstOrNull { it.role == "assistant" }?.text?.trim().orEmpty()

            if (firstUser.isNotBlank()) {
                app.chatRepository.updateConversationTitleIfBlank(
                    conversationId,
                    shortTitleFallback(firstUser),
                )
            }

            if (firstAssistant.isEmpty()) return@withLock

            val userForTitle = firstUser.ifEmpty { "(attachment)" }
            val generated = runCatching {
                app.engineHolder.generateConversationTitle(userForTitle, firstAssistant)
            }.getOrNull()?.trim().orEmpty()
            val finalTitle = generated
                .ifBlank { shortTitleFallback(userForTitle) }
                .ifBlank { "Chat" }
                .take(120)
            app.chatRepository.updateConversationTitle(conversationId, finalTitle)
        }
    }

    private fun shortTitleFallback(text: String): String =
        text
            .replace(Regex("\\s+"), " ")
            .trim()
            .take(48)
            .trimEnd()

    fun sendMessage(
        text: String,
        imagePath: String?,
        audioPath: String?,
        thinking: Boolean,
        concise: Boolean,
        onToken: (String) -> Unit,
        onComplete: () -> Unit,
    ) {
        val cid = _conversationId.value ?: return
        if (!_ui.value.engineReady) return
        val trimmed = text.trim()
        if (trimmed.isEmpty() && imagePath == null && audioPath == null) return

        viewModelScope.launch {
            _ui.value = _ui.value.copy(sending = true, sendError = null)
            try {
                val userLabel = trimmed.ifEmpty { "(attachment)" }
                app.chatRepository.insertMessage(
                    conversationId = cid,
                    role = "user",
                    text = userLabel,
                    imagePath = imagePath,
                    audioPath = audioPath,
                )
                app.chatRepository.updateConversationTitleIfBlank(
                    conversationId = cid,
                    title = shortTitleFallback(userLabel),
                )
                val replyBuilder = StringBuilder()
                app.engineHolder.streamReply(
                    userText = trimmed,
                    imagePath = imagePath,
                    audioPath = audioPath,
                    thinking = thinking,
                    concise = concise,
                ).collect { chunk ->
                    replyBuilder.append(chunk)
                    onToken(chunk)
                }
                val parsed = AssistantActionParser.parse(replyBuilder.toString())
                if (parsed.text.isNotEmpty()) {
                    app.chatRepository.insertMessage(
                        conversationId = cid,
                        role = "assistant",
                        text = parsed.text,
                    )
                    maybeGenerateConversationTitle(cid)
                }
                _ui.value = _ui.value.copy(pendingAction = parsed.action)
                onComplete()
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(sendError = e.message)
            } finally {
                _ui.value = _ui.value.copy(sending = false)
            }
        }
    }
}
