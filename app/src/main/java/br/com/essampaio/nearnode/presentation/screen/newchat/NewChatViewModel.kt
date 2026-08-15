package br.com.essampaio.nearnode.presentation.screen.newchat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.essampaio.nearnode.data.Node
import br.com.essampaio.nearnode.domain.service.nsdService.DiscoveryStatus
import br.com.essampaio.nearnode.domain.service.nsdService.NSDService
import br.com.essampaio.nearnode.domain.usecase.BecomeAvailableUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.collections.plus

class NewChatViewModel(
    private val nsdService: NSDService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewChatUiState())
    val uiState: StateFlow<NewChatUiState> = _uiState.asStateFlow()

    private var discoveryJob: kotlinx.coroutines.Job? = null

    fun start(){
        startDiscovery()
    }
    private fun startDiscovery() {

        // 1. Se já estiver buscando, cancela a busca anterior (chama o awaitClose no NSDHelper)
        discoveryJob?.cancel()

        discoveryJob = viewModelScope.launch {
            var currentState = _uiState.value
            currentState = currentState.copy(contacts = emptyList())
            _uiState.value = currentState

            nsdService.discoverServices().collect { value ->
                when(value){
                    DiscoveryStatus.Discovering -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                    is DiscoveryStatus.Found -> {

                        val node = value.node

                        if(currentState.contacts.find { it.name == node.name } != null){
                            return@collect
                        }
                        val updatedList = currentState.contacts + node

                        _uiState.value = _uiState.value.copy(contacts = updatedList)
                    }
                    DiscoveryStatus.Stopped -> {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                    DiscoveryStatus.WaitingForRegistration -> { }
                }
            }
        }
    }

    fun stopDiscovery(){
        discoveryJob?.cancel()
    }
}

data class NewChatUiState(
    val contacts: List<Node> = emptyList(),
    val isLoading: Boolean = false
)
