package br.com.essampaio.nearnode.presentation.screen.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.essampaio.nearnode.domain.model.AvailableStatus
import br.com.essampaio.nearnode.domain.model.Profile
import br.com.essampaio.nearnode.domain.repository.ProfileRepository
import br.com.essampaio.nearnode.domain.service.DeviceIdentificationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegistrationState(
    val username: String = "",
    val isLoading: Boolean = false,
    val isRegistered: Boolean = false
)

sealed class RegistrationAction {
    data class OnUsernameChanged(val username: String) : RegistrationAction()
    data object OnRegisterClick : RegistrationAction()
}

class RegistrationViewModel(
    private val profileRepository: ProfileRepository,
    private val deviceIdentificationService: DeviceIdentificationService
) : ViewModel() {

    private val _state = MutableStateFlow(RegistrationState())
    val state: StateFlow<RegistrationState> = _state.asStateFlow()

    fun start() {
        // Initialization logic if needed
    }

    fun stop() {
        // Cleanup logic if needed
    }

    fun onAction(action: RegistrationAction) {
        when (action) {
            is RegistrationAction.OnUsernameChanged -> {
                _state.update { it.copy(username = action.username) }
            }
            RegistrationAction.OnRegisterClick -> {
                register()
            }
        }
    }

    private fun register() {
        if (_state.value.username.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val profile = Profile(
                id = deviceIdentificationService.getUniqueId(),
                username = _state.value.username,
                ip = "",
                status = AvailableStatus.ONLINE
            )
            profileRepository.saveProfile(profile)
            _state.update { it.copy(isLoading = false, isRegistered = true) }
        }
    }
}
