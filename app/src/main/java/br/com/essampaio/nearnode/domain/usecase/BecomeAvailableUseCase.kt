package br.com.essampaio.nearnode.domain.usecase


import br.com.essampaio.nearnode.domain.model.AvailableStatus
import br.com.essampaio.nearnode.domain.repository.DiscoveryRepository
import br.com.essampaio.nearnode.domain.repository.ProfileRepository
import br.com.essampaio.nearnode.domain.service.CommunicationService
import br.com.essampaio.nearnode.domain.service.nsdService.NSDService
import br.com.essampaio.nearnode.domain.service.nsdService.RegistrationStatus
import kotlinx.coroutines.flow.Flow

class BecomeAvailableUseCase(
    private val nsdService: NSDService,
    private val communicationService: CommunicationService,
    private val profileRepository: ProfileRepository
) {
    val port = 9876
    suspend fun execute(): Boolean{
        profileRepository.updateCurrentStatus(AvailableStatus.ONLINE)
        communicationService.start()
        return nsdService.registerService(port) is RegistrationStatus.Registered
    }
}
