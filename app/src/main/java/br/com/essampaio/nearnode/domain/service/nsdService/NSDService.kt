package br.com.essampaio.nearnode.domain.service.nsdService


import kotlinx.coroutines.flow.Flow


interface NSDService {

    suspend fun registerService(port: Int): RegistrationStatus
    suspend fun unregisterService(): RegistrationStatus
    fun discoverServices(): Flow<DiscoveryStatus>
}