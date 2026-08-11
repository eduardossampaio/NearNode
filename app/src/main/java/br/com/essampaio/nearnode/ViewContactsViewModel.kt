package br.com.essampaio.nearnode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

enum class AvailableStatus{
    ONLINE,
    OFFLINE
}

data class Contact(
    val name: String,
    val ip: String
)
data class ViewContactsViewModelState(
    val status: AvailableStatus = AvailableStatus.OFFLINE,
    val availableContacts: List<Contact> = emptyList()
)
sealed class ViewContactsViewModelAction {
    object StartSearch: ViewContactsViewModelAction()
    object StopSearch: ViewContactsViewModelAction()
}
class ViewContactsViewModel(val nsdHelper: NSDHelper) : ViewModel() {
    //inject

    private var currentState = ViewContactsViewModelState()
    val state = MutableStateFlow(currentState)


    fun start(){
        viewModelScope.launch {
            nsdHelper.registerService(9876).collect { value ->
                when(value){
                    is RegistrationStatus.Registered -> {
                        currentState = currentState.copy(status = AvailableStatus.ONLINE)
                        state.value = currentState
                    }
                    else ->{
                        currentState = currentState.copy(status = AvailableStatus.OFFLINE)
                        state.value = currentState

                    }
                }
            }
        }
    }

    fun close(){
        nsdHelper.stopService()
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
            currentState = currentState.copy(availableContacts = emptyList())
            state.value = currentState

            nsdHelper.discoverServices().collect { value ->
                when(value){
                    DiscoveryStatus.Discovering -> {}
                    is DiscoveryStatus.Found -> {
                        val service = value.service.serviceName
                        val resolvedInfo = nsdHelper.resolveService(value.service)

                        val ipString = resolvedInfo?.host?.hostAddress ?: "IP Desconhecido"
                        val newContact = Contact(service, ipString)

                        if(currentState.availableContacts.find { it.name == newContact.name } != null){
                            return@collect
                        }
                        val updatedList = currentState.availableContacts + newContact

                        currentState = currentState.copy(availableContacts = updatedList)
                        state.value = currentState
                    }
                    DiscoveryStatus.Stopped -> { }
                    DiscoveryStatus.WaitingForRegistration -> { }
                }
            }
        }
    }

    private fun stopSearch(){
        viewModelScope.launch {
            nsdHelper.stopService()
        }
    }
}