package br.com.essampaio.nearnode.domain.repository

import br.com.essampaio.nearnode.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun getLatestMessages(): Flow<List<Message>>
    fun getMessagesWithContact(contactId: String): Flow<List<Message>>
    suspend fun sendMessage(message: Message)
}
