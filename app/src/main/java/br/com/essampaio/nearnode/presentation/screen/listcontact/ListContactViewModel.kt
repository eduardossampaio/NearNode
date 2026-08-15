package br.com.essampaio.nearnode.presentation.screen.listcontact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.essampaio.nearnode.domain.model.Message
import br.com.essampaio.nearnode.domain.repository.MessageRepository
import br.com.essampaio.nearnode.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ListContactViewModel(
    private val messageRepository: MessageRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListContactUiState())
    val uiState: StateFlow<ListContactUiState> = _uiState.asStateFlow()

    init {
        loadConversations()
    }

    private fun loadConversations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            messageRepository.getLatestMessages().collect { messages ->
                val conversations = messages.map { message ->
                    // For now, using senderId/receiverId as name if profile not found
                    Conversation(
                        contactId = if (message.senderId == "me") message.receiverId else message.senderId,
                        contactName = "User ${if (message.senderId == "me") message.receiverId else message.senderId}",
                        lastMessage = message.content,
                        timestamp = message.timestamp
                    )
                }
                _uiState.value = _uiState.value.copy(conversations = conversations, isLoading = false)
            }
        }
    }
}

data class ListContactUiState(
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = false
)

data class Conversation(
    val contactId: String,
    val contactName: String,
    val lastMessage: String,
    val timestamp: Long,
    val unreadCount: Int = 0
)
