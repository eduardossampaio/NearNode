package br.com.essampaio.nearnode.presentation.screen.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.essampaio.nearnode.domain.model.Message
import br.com.essampaio.nearnode.domain.repository.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val contactId: String,
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            messageRepository.getMessagesWithContact(contactId).collect { messages ->
                _uiState.value = _uiState.value.copy(messages = messages)
            }
        }
    }

    fun onMessageTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(messageText = text)
    }

    fun sendMessage() {
        val text = _uiState.value.messageText
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
            _uiState.value = _uiState.value.copy(messageText = "")
        }
    }
}

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val messageText: String = "",
    val contactName: String = "Contact"
)
