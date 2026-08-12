package br.com.essampaio.nearnode.domain.usecase

import br.com.essampaio.nearnode.domain.repository.DiscoveryRepository
import br.com.essampaio.nearnode.domain.service.nsdService.NSDService
import br.com.essampaio.nearnode.domain.service.nsdService.RegistrationStatus

class BecomeUnavailableUseCase(private val nsdService: NSDService) {
   suspend fun execute(): Boolean{
       return nsdService.unregisterService() == RegistrationStatus.Unregistered
    }
}