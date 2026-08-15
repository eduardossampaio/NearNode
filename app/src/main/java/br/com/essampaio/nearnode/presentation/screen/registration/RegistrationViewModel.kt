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
import kotlinx.coroutines.launch

class RegistrationViewModel(
    private val profileRepository: ProfileRepository,
    private val deviceIdentificationService: DeviceIdentificationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    fun onUsernameChanged(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }

    fun register() {
        if (_uiState.value.username.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val profile = Profile(
                id = deviceIdentificationService.getUniqueId(),
                username = _uiState.value.username,
                ip = "", // Will be filled when becoming available
                status = AvailableStatus.ONLINE
            )
            profileRepository.saveProfile(profile)
            _uiState.value = _uiState.value.copy(isLoading = false, isRegistered = true)
        }
    }
}

data class RegistrationUiState(
    val username: String = "",
    val isLoading: Boolean = false,
    val isRegistered: Boolean = false
)
