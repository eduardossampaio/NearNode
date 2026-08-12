package br.com.essampaio.nearnode.domain.usecase

import br.com.essampaio.nearnode.domain.repository.DiscoveryRepository
import br.com.essampaio.nearnode.domain.service.nsdService.DiscoveryStatus
import kotlinx.coroutines.flow.Flow

class DiscoveryNearbyUseCase(private val repository: DiscoveryRepository) {
    operator fun invoke(): Flow<DiscoveryStatus> {
        return repository.discoverServices()
    }
}