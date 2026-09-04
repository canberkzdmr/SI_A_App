package com.cbo.notes.data.repository

import com.cbo.core.database.dao.UserSettingsDao
import com.cbo.core.domain.model.ViewMode
import com.cbo.core.logger.AppLogger
import com.cbo.notes.domain.repository.UserSettingsRepository
import javax.inject.Inject

class UserSettingsRepositoryImpl @Inject constructor(
    private val userSettingsDao: UserSettingsDao
): UserSettingsRepository{
    override suspend fun getNotesViewMode(userId: Int): Result<ViewMode> {
        return try {
            val viewMode = userSettingsDao.getNotesViewMode(userId)
            Result.success(viewMode)
        } catch (e: Exception) {
            AppLogger.e("Could not get View Mode preference of the user\n\terror: ${e.message}", e)
            Result.failure(Throwable("Could not get View Mode preference of the user"))
        }
    }

    override suspend fun setNotesViewMode(
        userId: Int,
        viewMode: ViewMode
    ): Result<Unit> {
        return try {
            userSettingsDao.setNotesViewMode(userId = userId, viewMode = viewMode)
            Result.success(Unit)
        } catch (e: Exception) {
            AppLogger.e("An error occurred while updating viewMode: ${e.message}", e)
            Result.failure(e)
        }
    }
}