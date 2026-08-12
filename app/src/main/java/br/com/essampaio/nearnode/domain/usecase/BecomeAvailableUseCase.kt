package br.com.essampaio.nearnode.domain.usecase


import br.com.essampaio.nearnode.domain.repository.DiscoveryRepository
import br.com.essampaio.nearnode.domain.service.nsdService.NSDService
import br.com.essampaio.nearnode.domain.service.nsdService.RegistrationStatus
import kotlinx.coroutines.flow.Flow

class BecomeAvailableUseCase(private val nsdService: NSDService) {
    val port = 9876
    suspend fun execute(): Boolean{
        return nsdService.registerService(port) is RegistrationStatus.Registered
    }
}