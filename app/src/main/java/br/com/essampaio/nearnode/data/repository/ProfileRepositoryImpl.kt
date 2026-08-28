package br.com.essampaio.nearnode.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
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

    override fun getCurrentProfile(): Profile? {
        return getProfile(deviceIdentificationService.getUniqueId())
    }

    override fun getOtherProfiles(): List<Profile> {
        return queries.getOtherProfiles(deviceIdentificationService.getUniqueId())
            .executeAsList()
            .map { entity ->

                    Profile(
                        id = entity.id,
                        username = entity.username,
                        ip = entity.ip,
                        status = AvailableStatus.valueOf(entity.status)
                    )

            }
    }

    override fun getProfile(id: String): Profile?{
        return queries.getProfile(id)
            .executeAsOne()
            .run {
                    Profile(
                        id = id,
                        username = username,
                        ip = ip,
                        status = AvailableStatus.valueOf(status)
                    )

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
