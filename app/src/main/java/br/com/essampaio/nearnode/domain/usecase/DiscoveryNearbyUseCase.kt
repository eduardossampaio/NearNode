package br.com.essampaio.nearnode.domain.usecase

import br.com.essampaio.nearnode.domain.service.nsdService.DiscoveryStatus
import br.com.essampaio.nearnode.domain.service.nsdService.NSDService
import kotlinx.coroutines.flow.Flow

class DiscoveryNearbyUseCase(private val nsdService: NSDService) {
    operator fun invoke(): Flow<DiscoveryStatus> {
        return nsdService.discoverServices()
    }
}
