package br.com.essampaio.nearnode.domain.usecase

import br.com.essampaio.nearnode.domain.model.AvailableStatus
import br.com.essampaio.nearnode.domain.repository.DiscoveryRepository
import br.com.essampaio.nearnode.domain.repository.ProfileRepository
import br.com.essampaio.nearnode.domain.service.nsdService.NSDService
import br.com.essampaio.nearnode.domain.service.nsdService.RegistrationStatus

class BecomeUnavailableUseCase(
    private val nsdService: NSDService,
    private val profileRepository: ProfileRepository) {
   suspend fun execute(): Boolean{
       profileRepository.updateCurrentStatus(AvailableStatus.OFFLINE)
       return nsdService.unregisterService() == RegistrationStatus.Unregistered
    }
}