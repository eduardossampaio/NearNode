package br.com.essampaio.nearnode.presentation.screen.newchat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.essampaio.nearnode.data.Node
import br.com.essampaio.nearnode.domain.model.Profile
import br.com.essampaio.nearnode.domain.service.nsdService.DiscoveryStatus
import br.com.essampaio.nearnode.domain.service.nsdService.NSDService
import br.com.essampaio.nearnode.domain.usecase.DiscoveryNearbyUseCase
import br.com.essampaio.nearnode.domain.usecase.DiscoveryProfileStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewChatState(
    val contacts: List<Profile> = emptyList(),
    val isLoading: Boolean = false
)

sealed class NewChatAction {
    data object StartDiscovery : NewChatAction()
}

class NewChatViewModel(
    private val discoveryNearbyUseCase: DiscoveryNearbyUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(NewChatState())
    val state: StateFlow<NewChatState> = _state.asStateFlow()

    private var discoveryJob: Job? = null

    fun start() {
        onAction(NewChatAction.StartDiscovery)
    }

    fun stop() {
        discoveryJob?.cancel()
    }

    fun onAction(action: NewChatAction) {
        when (action) {
            NewChatAction.StartDiscovery -> startDiscovery()
        }
    }

    private fun startDiscovery() {
        discoveryJob?.cancel()
        discoveryJob = viewModelScope.launch {
            _state.update { it.copy(contacts = emptyList(), isLoading = true) }
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
                        _state.update { it.copy(isLoading = true) }
                    }
                    else -> {
                        _state.update { it.copy(isLoading = false) }
                    }
                }
            }
        }
    }
}
