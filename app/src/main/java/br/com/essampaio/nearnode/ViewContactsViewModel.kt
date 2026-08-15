package br.com.essampaio.nearnode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.essampaio.nearnode.data.Node
import br.com.essampaio.nearnode.domain.model.AvailableStatus
import br.com.essampaio.nearnode.domain.service.nsdService.DiscoveryStatus
import br.com.essampaio.nearnode.domain.service.nsdService.NSDService
import br.com.essampaio.nearnode.domain.service.nsdService.RegistrationStatus
import br.com.essampaio.nearnode.domain.usecase.BecomeAvailableUseCase
import br.com.essampaio.nearnode.domain.usecase.BecomeUnavailableUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ViewContactsViewModelState(
    val status: AvailableStatus = AvailableStatus.OFFLINE,
    val nodesFound: List<Node> = emptyList()
)
sealed class ViewContactsViewModelAction {
    object StartSearch: ViewContactsViewModelAction()
    object StopSearch: ViewContactsViewModelAction()
}
class ViewContactsViewModel(
    val becomeAvailableUseCase: BecomeAvailableUseCase,
    val becomeUnavailableUseCase: BecomeUnavailableUseCase,
    val nsdServiceImpl: NSDService) : ViewModel() {


    private var currentState = ViewContactsViewModelState()
    val state = MutableStateFlow(currentState)


    fun start(){
        viewModelScope.launch {
            startService()
        }
    }

    fun close(){
        viewModelScope.launch {
            stopService()
        }
    }

    fun onAction(action: ViewContactsViewModelAction){
        when(action){
            ViewContactsViewModelAction.StartSearch -> {
                startSearch()
            }

            ViewContactsViewModelAction.StopSearch -> {
                stopSearch()
            }
        }
    }

    // Variável para guardar a busca atual e evitar duplicações
    private var discoveryJob: kotlinx.coroutines.Job? = null

    private fun startSearch(){
        // 1. Se já estiver buscando, cancela a busca anterior (chama o awaitClose no NSDHelper)
        discoveryJob?.cancel()

        discoveryJob = viewModelScope.launch {
            // Reinicia a lista vazia no estado para limpar a tela ao atualizar
            currentState = currentState.copy(nodesFound = emptyList())
            state.value = currentState

            nsdServiceImpl.discoverServices().collect { value ->
                when(value){
                    DiscoveryStatus.Discovering -> {}
                    is DiscoveryStatus.Found -> {

                        val node = value.node

                        if(currentState.nodesFound.find { it.name == node.name } != null){
                            return@collect
                        }
                        val updatedList = currentState.nodesFound + node

                        currentState = currentState.copy(nodesFound = updatedList)
                        state.value = currentState
                    }
                    DiscoveryStatus.Stopped -> { }
                    DiscoveryStatus.WaitingForRegistration -> { }
                }
            }
        }
    }
    private suspend fun startService(){
        when(becomeAvailableUseCase.execute()){
            true -> {
                currentState = currentState.copy(status = AvailableStatus.ONLINE)
                state.value = currentState
            }
            else ->{
                currentState = currentState.copy(status = AvailableStatus.OFFLINE)
                state.value = currentState

            }
        }
    }
    private suspend fun stopService(){
        becomeUnavailableUseCase.execute()
    }

    private fun stopSearch(){
        viewModelScope.launch {
            discoveryJob?.cancel()
        }
    }

}