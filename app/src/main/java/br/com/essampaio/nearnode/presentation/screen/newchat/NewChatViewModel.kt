package br.com.essampaio.nearnode.presentation.screen.newchat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.essampaio.nearnode.domain.model.Profile
import br.com.essampaio.nearnode.domain.usecase.DiscoveryNearbyUseCase
import br.com.essampaio.nearnode.domain.usecase.DiscoveryProfileStatus
import br.com.essampaio.nearnode.domain.usecase.ListContactsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewChatState(
    val contacts: List<Profile> = emptyList(),
    val isDiscovering: Boolean = false
)

sealed class NewChatAction {
    data object StartDiscovery : NewChatAction()
    data object StopDiscovery : NewChatAction()
}

class NewChatViewModel(
    private val discoveryNearbyUseCase: DiscoveryNearbyUseCase,
    private val listContactsUseCase: ListContactsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(NewChatState())
    val state: StateFlow<NewChatState> = _state.asStateFlow()

    private var discoveryJob: Job? = null

    fun start() {
        retrieveSavedContacts()
        onAction(NewChatAction.StartDiscovery)
    }

    fun stop() {
        discoveryJob?.cancel()
    }

    private fun retrieveSavedContacts(){
        viewModelScope.launch {
            val contacts = listContactsUseCase.execute()
            _state.update { currentState ->
                currentState.copy(contacts = currentState.contacts + contacts)
            }
        }
    }

    fun onAction(action: NewChatAction) {
        when (action) {
            NewChatAction.StartDiscovery -> startDiscovery()
            NewChatAction.StopDiscovery -> stopDiscovery()

        }
    }

    private fun startDiscovery() {
        discoveryJob?.cancel()
        discoveryJob = viewModelScope.launch {
            _state.update { it.copy(contacts = emptyList(), isDiscovering = true) }
            discoveryNearbyUseCase.invoke().collect { status ->
                when (status) {
                    is DiscoveryProfileStatus.Found -> {
                        _state.update { currentState ->
                            val profile = status.profile
                            if (currentState.contacts.any { it.id == profile.id }) {
                                currentState
                            } else {
                                currentState.copy(contacts = currentState.contacts + profile)
                            }
                        }
                    }
                    DiscoveryProfileStatus.Discovering -> {
                        _state.update { it.copy(isDiscovering = true) }
                    }
                    else -> {
                        _state.update { it.copy(isDiscovering = false) }
                    }
                }
            }
        }
    }

    private fun stopDiscovery(){
        discoveryJob?.cancel()
        _state.update { it.copy(isDiscovering = false) }
    }
}
