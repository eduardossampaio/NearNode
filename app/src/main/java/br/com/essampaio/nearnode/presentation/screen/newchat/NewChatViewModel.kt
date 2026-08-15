package br.com.essampaio.nearnode.presentation.screen.newchat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.essampaio.nearnode.data.Node
import br.com.essampaio.nearnode.domain.service.nsdService.DiscoveryStatus
import br.com.essampaio.nearnode.domain.service.nsdService.NSDService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NewChatViewModel(
    private val nsdService: NSDService
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewChatUiState())
    val uiState: StateFlow<NewChatUiState> = _uiState.asStateFlow()

    init {
        startDiscovery()
    }

    private fun startDiscovery() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            nsdService.discoverServices().collect { status ->
                when (status) {
                    is DiscoveryStatus.Found -> {
                        val updatedList = (_uiState.value.contacts + status.node).distinctBy { it.name }
                        _uiState.value = _uiState.value.copy(contacts = updatedList)
                    }
                    DiscoveryStatus.Discovering -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                }
            }
        }
    }
}

data class NewChatUiState(
    val contacts: List<Node> = emptyList(),
    val isLoading: Boolean = false
)
