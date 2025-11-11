package com.cbo.core.domain.repository

import com.cbo.core.domain.model.UserSettings

interface UserSettingsRepository {
    suspend fun getUserSettings(userId: Int): Result<UserSettings>
    suspend fun setFirstLoginDone(userId: Int, done: Boolean): Result<Unit>
    suspend fun setBiometricsEnabled(userId: Int, enabled: Boolean): Result<Unit>
    suspend fun getPreferredLanguage(userId: Int): Result<String>
    suspend fun setPreferredLanguage(userId: Int, languageCode: String?): Result<Unit>
}