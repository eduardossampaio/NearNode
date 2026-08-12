package br.com.essampaio.nearnode.domain.repository

import br.com.essampaio.nearnode.domain.service.nsdService.DiscoveryStatus
import br.com.essampaio.nearnode.domain.service.nsdService.RegistrationStatus

import kotlinx.coroutines.flow.Flow

interface DiscoveryRepository {
    fun registerService(port: Int): Flow<RegistrationStatus>
    fun stopService()
    fun discoverServices(): Flow<DiscoveryStatus>
}