package br.com.essampaio.nearnode.domain.usecase

import br.com.essampaio.nearnode.data.Node
import br.com.essampaio.nearnode.domain.model.Profile
import br.com.essampaio.nearnode.domain.repository.ProfileRepository
import br.com.essampaio.nearnode.domain.service.RemoteNodeService
import br.com.essampaio.nearnode.domain.service.nsdService.DiscoveryStatus
import br.com.essampaio.nearnode.domain.service.nsdService.NSDService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


sealed class DiscoveryProfileStatus {
    data object Discovering : DiscoveryProfileStatus()
    data class Found(val profile: Profile): DiscoveryProfileStatus()
    data object Stopped: DiscoveryProfileStatus()
}


class DiscoveryNearbyUseCase(
    private val nsdService: NSDService,
    private val nodeService: RemoteNodeService,
    private val profileRepository: ProfileRepository) {

    operator fun invoke(): Flow<DiscoveryProfileStatus> {

        return nsdService.discoverServices().map {status ->
            when(status){
                DiscoveryStatus.Discovering -> DiscoveryProfileStatus.Discovering
                is DiscoveryStatus.Found -> {
                    val node = status.node
                    val profile = nodeService.getNodeInfo(node.ipAddress,node.port)
                    if(profile != null) {
                        profileRepository.saveProfile(profile)
                        DiscoveryProfileStatus.Found(profile)
                    }else
                        DiscoveryProfileStatus.Discovering

                }
                else -> DiscoveryProfileStatus.Stopped
            }
        }
    }
}
