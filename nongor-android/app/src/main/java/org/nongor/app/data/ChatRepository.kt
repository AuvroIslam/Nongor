package org.nongor.app.data

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

data class ConversationEntity(
    val id: Long,
    val title: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

data class MessageEntity(
    val id: Long,
    val conversationId: Long,
    val role: String,
    val text: String,
    val imagePath: String? = null,
    val audioPath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

private data class ChatStore(
    val conversations: List<ConversationEntity> = emptyList(),
    val messages: List<MessageEntity> = emptyList(),
    val nextConversationId: Long = 1L,
    val nextMessageId: Long = 1L,
)

class ChatRepository(context: Context) {

    private val file = File(context.filesDir, "chat_store.json")
    private val gson = Gson()
    private val mutex = Mutex()
    private val _store = MutableStateFlow(loadSync())

    private fun loadSync(): ChatStore {
        if (!file.exists()) return ChatStore()
        return try {
            gson.fromJson(file.readText(), ChatStore::class.java) ?: ChatStore()
        } catch (_: Throwable) {
            ChatStore()
        }
    }

    private suspend fun persistLocked() {
        file.writeText(gson.toJson(_store.value))
    }

    fun observeConversations(): Flow<List<ConversationEntity>> =
        _store.map { s -> s.conversations.sortedByDescending { it.updatedAt } }

    fun observeMessages(conversationId: Long): Flow<List<MessageEntity>> =
        _store.map { s ->
            s.messages
                .filter { it.conversationId == conversationId }
                .sortedBy { it.createdAt }
        }

    suspend fun getOrCreateConversation(): Long = mutex.withLock {
        val s = _store.value
        if (s.conversations.isNotEmpty()) {
            return@withLock s.conversations.maxBy { it.updatedAt }.id
        }
        val id = s.nextConversationId
        val now = System.currentTimeMillis()
        val c = ConversationEntity(id = id, createdAt = now, updatedAt = now)
        _store.value = s.copy(
            conversations = s.conversations + c,
            nextConversationId = s.nextConversationId + 1,
        )
        persistLocked()
        id
    }

    suspend fun createConversation(): Long = mutex.withLock {
        val s = _store.value
        val id = s.nextConversationId
        val now = System.currentTimeMillis()
        val c = ConversationEntity(id = id, createdAt = now, updatedAt = now)
        _store.value = s.copy(
            conversations = s.conversations + c,
            nextConversationId = s.nextConversationId + 1,
        )
        persistLocked()
        id
    }

    suspend fun insertMessage(
        conversationId: Long,
        role: String,
        text: String,
        imagePath: String? = null,
        audioPath: String? = null,
    ): Long = mutex.withLock {
        val s = _store.value
        val id = s.nextMessageId
        val msg = MessageEntity(
            id = id,
            conversationId = conversationId,
            role = role,
            text = text,
            imagePath = imagePath,
            audioPath = audioPath,
        )
        val convs = s.conversations.map {
            if (it.id == conversationId) it.copy(updatedAt = System.currentTimeMillis()) else it
        }
        _store.value = s.copy(
            messages = s.messages + msg,
            nextMessageId = s.nextMessageId + 1,
            conversations = convs,
        )
        persistLocked()
        id
    }

    suspend fun clearAllChats() = mutex.withLock {
        val s = _store.value
        _store.value = ChatStore(
            nextConversationId = s.nextConversationId,
            nextMessageId = s.nextMessageId,
        )
        persistLocked()
    }

    suspend fun getConversation(conversationId: Long): ConversationEntity? = mutex.withLock {
        _store.value.conversations.find { it.id == conversationId }
    }

    suspend fun getAllConversations(): List<ConversationEntity> = mutex.withLock {
        _store.value.conversations.sortedByDescending { it.updatedAt }
    }

    suspend fun getMessagesOrdered(conversationId: Long): List<MessageEntity> = mutex.withLock {
        _store.value.messages
            .filter { it.conversationId == conversationId }
            .sortedBy { it.createdAt }
    }

    suspend fun updateConversationTitle(conversationId: Long, title: String) = mutex.withLock {
        val s = _store.value
        if (s.conversations.none { it.id == conversationId }) return@withLock
        val now = System.currentTimeMillis()
        val trimmed = title.trim().take(120)
        val convs = s.conversations.map {
            if (it.id == conversationId) it.copy(title = trimmed, updatedAt = now) else it
        }
        _store.value = s.copy(conversations = convs)
        persistLocked()
    }

    suspend fun updateConversationTitleIfBlank(conversationId: Long, title: String) = mutex.withLock {
        val s = _store.value
        val target = s.conversations.find { it.id == conversationId } ?: return@withLock
        if (target.title.isNotBlank()) return@withLock
        val now = System.currentTimeMillis()
        val trimmed = title.trim().take(120)
        val convs = s.conversations.map {
            if (it.id == conversationId) it.copy(title = trimmed, updatedAt = now) else it
        }
        _store.value = s.copy(conversations = convs)
        persistLocked()
    }
}
