package com.cbo.notes.data.repository

import android.util.Log
import com.cbo.core.database.dao.UserSettingsDao
import com.cbo.core.domain.model.ViewMode
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
            Log.e("UserSettingsRepositoryImpl", "Could not get View Mode preference of the user\n\terror: ${e.message}")
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
            Log.e("UserSettingsRepositoryImpl", "An error occurred while updating viewMode: ${e.message}")
            Result.failure(e)
        }
    }
}