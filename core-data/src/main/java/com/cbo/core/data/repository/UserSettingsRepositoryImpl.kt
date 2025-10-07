package com.cbo.core.data.repository

import android.util.Log
import com.cbo.core.data.mapper.UserSettingsEntityMapper
import com.cbo.core.database.dao.UserSettingsDao
import com.cbo.core.domain.exception.UserSettingsException
import com.cbo.core.domain.repository.UserSettingsRepository
import javax.inject.Inject

class UserSettingsRepositoryImpl @Inject constructor(
    private val userSettingsDao: UserSettingsDao,
    private val userSettingsEntityMapper: UserSettingsEntityMapper,
): UserSettingsRepository {
    override suspend fun getUserSettings(userId: Int): Result<com.cbo.core.domain.model.UserSettings> {
        return try {
            val userSettingsEntity = userSettingsDao.getUserSettings(userId)
            userSettingsEntity?.let {
                Result.success(userSettingsEntityMapper.toDomain(it))
            } ?: Result.failure(UserSettingsException.UserSettingsNotFoundException())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setFirstLoginDone(userId: Int, done: Boolean): Result<Unit> {
        return try {
            userSettingsDao.updateFirstLoginDone(userId = userId, done = done)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("UserSettingsRepositoryImpl", "Error setFirstLoginDone: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun setBiometricsEnabled(userId: Int, enabled: Boolean): Result<Unit> {
        return try {
            userSettingsDao.updateBiometricsEnabled(userId = userId, enabled = enabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("UserSettingsRepositoryImpl", "Error setFirstLoginDone: ${e.message}")
            Result.failure(e)
        }
    }
}