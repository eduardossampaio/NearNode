package br.com.essampaio.nearnode.domain.service.nsdService
sealed class RegistrationStatus {
    data class Registered(val registrationServiceName: String): RegistrationStatus()
    data class Failed(val cause: String): RegistrationStatus()
    data object Unregistered: RegistrationStatus()
}