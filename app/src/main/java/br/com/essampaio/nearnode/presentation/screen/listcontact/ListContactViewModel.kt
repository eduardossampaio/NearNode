package br.com.essampaio.nearnode.presentation.screen.listcontact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.essampaio.nearnode.domain.repository.MessageRepository
import br.com.essampaio.nearnode.domain.repository.ProfileRepository
import br.com.essampaio.nearnode.domain.usecase.BecomeAvailableUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ListContactState(
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = false
)

sealed class ListContactAction {
    data object LoadConversations : ListContactAction()
}

class ListContactViewModel(
    private val becomeAvailableUseCase: BecomeAvailableUseCase,
    private val messageRepository: MessageRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ListContactState())
    val state: StateFlow<ListContactState> = _state.asStateFlow()

    private var loadJob: Job? = null

    fun start() {
        viewModelScope.launch {
            becomeAvailableUseCase.execute()
        }
        onAction(ListContactAction.LoadConversations)
    }

    fun stop() {
        loadJob?.cancel()
    }

    fun onAction(action: ListContactAction) {
        when (action) {
            ListContactAction.LoadConversations -> loadConversations()
        }
    }

    private fun loadConversations() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            messageRepository.getLatestMessages().collect { messages ->
                val conversations = messages.map { message ->
                    Conversation(
                        contactId = if (message.senderId == "me") message.receiverId else message.senderId,
                        contactName = "User ${if (message.senderId == "me") message.receiverId else message.senderId}",
                        lastMessage = message.content,
                        timestamp = message.timestamp
                    )
                }
                _state.update { it.copy(conversations = conversations, isLoading = false) }
            }
        }
    }
}

data class Conversation(
    val contactId: String,
    val contactName: String,
    val lastMessage: String,
    val timestamp: Long,
    val unreadCount: Int = 0
)
