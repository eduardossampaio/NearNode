package br.com.essampaio.nearnode.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import br.com.essampaio.nearnode.database.NearNodeDatabase
import br.com.essampaio.nearnode.database.MessageEntity
import br.com.essampaio.nearnode.domain.model.Message
import br.com.essampaio.nearnode.domain.repository.MessageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class MessageRepositoryImpl(
    database: NearNodeDatabase
) : MessageRepository {

    private val queries = database.messageQueries

    override fun getLatestMessages(): Flow<List<Message>> {
        return queries.getLatestMessages()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    override fun getMessagesWithContact(contactId: String): Flow<List<Message>> {
        return queries.getMessagesWithContact(myId = "me", contactId = contactId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    override suspend fun sendMessage(message: Message) {
        queries.insertMessage(
            MessageEntity(
                id = message.id.ifBlank { UUID.randomUUID().toString() },
                senderId = message.senderId,
                receiverId = message.receiverId,
                content = message.content,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    private fun MessageEntity.toDomain(): Message {
        return Message(
            id = id,
            senderId = senderId,
            receiverId = receiverId,
            content = content,
            timestamp = timestamp
        )
    }
}
