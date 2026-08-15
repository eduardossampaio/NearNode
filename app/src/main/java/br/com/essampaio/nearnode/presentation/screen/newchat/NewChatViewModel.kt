package br.com.essampaio.nearnode.presentation.screen.newchat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.essampaio.nearnode.data.Node
import br.com.essampaio.nearnode.domain.service.nsdService.DiscoveryStatus
import br.com.essampaio.nearnode.domain.service.nsdService.NSDService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewChatState(
    val contacts: List<Node> = emptyList(),
    val isLoading: Boolean = false
)

sealed class NewChatAction {
    data object StartDiscovery : NewChatAction()
}

class NewChatViewModel(
    private val nsdService: NSDService
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
            nsdService.discoverServices().collect { status ->
                when (status) {
                    is DiscoveryStatus.Found -> {
                        _state.update { currentState ->
                            if (currentState.contacts.any { it.name == status.node.name }) {
                                currentState
                            } else {
                                currentState.copy(contacts = currentState.contacts + status.node)
                            }
                        }
                    }
                    DiscoveryStatus.Discovering -> {
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
