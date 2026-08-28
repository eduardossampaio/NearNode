package br.com.essampaio.nearnode.domain.usecase

import br.com.essampaio.nearnode.domain.model.AvailableStatus
import br.com.essampaio.nearnode.domain.model.Profile
import br.com.essampaio.nearnode.domain.repository.ProfileRepository
import br.com.essampaio.nearnode.domain.service.RemoteNodeService

class ListContactsUseCase(
    private val profileRepository: ProfileRepository,
    private val nodeService: RemoteNodeService,
) {

    suspend fun execute(): List<Profile> {
        return profileRepository.getOtherProfiles().map {profile ->
            val retrievedProfile = nodeService.getNodeInfo(profile.ip, 9876)
            if(retrievedProfile!=null) {
                profileRepository.saveProfile(retrievedProfile)
               retrievedProfile
            }else{
                profile.copy(status = AvailableStatus.OFFLINE)
            }
        }
    }
}
