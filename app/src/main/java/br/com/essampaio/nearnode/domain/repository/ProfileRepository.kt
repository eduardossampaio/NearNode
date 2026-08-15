package br.com.essampaio.nearnode.domain.repository

import br.com.essampaio.nearnode.domain.model.AvailableStatus
import br.com.essampaio.nearnode.domain.model.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun updateStatus(id: String, status: AvailableStatus)

    suspend fun updateCurrentStatus(status: AvailableStatus)
    fun getProfile(id: String): Flow<Profile?>
    fun getCurrentProfile(): Flow<Profile?>
    suspend fun saveProfile(profile: Profile)
}
