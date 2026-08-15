package br.com.essampaio.nearnode.presentation.screen.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.essampaio.nearnode.domain.model.Message
import br.com.essampaio.nearnode.domain.repository.MessageRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatState(
    val messages: List<Message> = emptyList(),
    val messageText: String = "",
    val contactName: String = "Contact"
)

sealed class ChatAction {
    data class OnMessageTextChanged(val text: String) : ChatAction()
    data object SendMessage : ChatAction()
    data object LoadMessages : ChatAction()
}

class ChatViewModel(
    private val contactId: String,
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private var loadMessagesJob: Job? = null

    fun start() {
        onAction(ChatAction.LoadMessages)
    }

    fun stop() {
        loadMessagesJob?.cancel()
    }

    fun onAction(action: ChatAction) {
        when (action) {
            is ChatAction.OnMessageTextChanged -> {
                _state.update { it.copy(messageText = action.text) }
            }
            ChatAction.SendMessage -> sendMessage()
            ChatAction.LoadMessages -> loadMessages()
        }
    }

    private fun loadMessages() {
        loadMessagesJob?.cancel()
        loadMessagesJob = viewModelScope.launch {
            messageRepository.getMessagesWithContact(contactId).collect { messages ->
                _state.update { it.copy(messages = messages) }
            }
        }
    }

    private fun sendMessage() {
        val text = _state.value.messageText
        if (text.isBlank()) return

        viewModelScope.launch {
            val message = Message(
                id = "",
                senderId = "me",
                receiverId = contactId,
                content = text,
                timestamp = System.currentTimeMillis()
            )
            messageRepository.sendMessage(message)
            _state.update { it.copy(messageText = "") }
        }
    }
}
