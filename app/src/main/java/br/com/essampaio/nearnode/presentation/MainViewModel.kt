package br.com.essampaio.nearnode.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.essampaio.nearnode.domain.repository.ProfileRepository
import br.com.essampaio.nearnode.presentation.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _startDestination = MutableStateFlow<Route?>(null)
    val startDestination: StateFlow<Route?> = _startDestination.asStateFlow()

    init {
        checkRegistration()
    }

    private fun checkRegistration() {
        viewModelScope.launch {
            val profile = profileRepository.getCurrentProfile().first()
            if (profile != null) {
                _startDestination.value = Route.ListContact
            } else {
                _startDestination.value = Route.Registration
            }
        }
    }
}
