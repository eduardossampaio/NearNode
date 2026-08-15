package br.com.essampaio.nearnode.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import br.com.essampaio.nearnode.domain.model.AvailableStatus
import br.com.essampaio.nearnode.database.NearNodeDatabase
import br.com.essampaio.nearnode.database.ProfileEntity
import br.com.essampaio.nearnode.domain.model.Profile
import br.com.essampaio.nearnode.domain.repository.ProfileRepository
import br.com.essampaio.nearnode.domain.service.DeviceIdentificationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProfileRepositoryImpl(
    database: NearNodeDatabase,
    private val deviceIdentificationService: DeviceIdentificationService
) : ProfileRepository {

    private val queries = database.profileQueries

    override suspend fun updateStatus(id: String, status: AvailableStatus) {
        queries.updateStatus(status.name, id)
    }

    override suspend fun updateCurrentStatus(status: AvailableStatus) {
        updateStatus(deviceIdentificationService.getUniqueId(),status)
    }

    override fun getCurrentProfile(): Flow<Profile?> {
        return getProfile(deviceIdentificationService.getUniqueId())
    }

    override fun getProfile(id: String): Flow<Profile?> {
        return queries.getProfile(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { entity ->
                entity?.let {
                    Profile(
                        id = it.id,
                        username = it.username,
                        ip = it.ip,
                        status = AvailableStatus.valueOf(it.status)
                    )
                }
            }
    }

    override suspend fun saveProfile(profile: Profile) {
        queries.insertOrUpdate(
            ProfileEntity(
                id = profile.id,
                username = profile.username,
                ip = profile.ip,
                status = profile.status.name
            )
        )
    }
}
