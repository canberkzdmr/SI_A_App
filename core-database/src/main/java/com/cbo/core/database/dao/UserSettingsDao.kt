package com.cbo.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cbo.core.database.entity.UserSettingsEntity
import com.cbo.core.domain.model.ViewMode
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: UserSettingsEntity)

    @Query("SELECT * FROM user_settings WHERE userId = :userId LIMIT 1")
    suspend fun getUserSettings(userId: Int): UserSettingsEntity?

    @Query("SELECT * FROM user_settings WHERE userId = :userId LIMIT 1")
    fun getUserSettingsFlow(userId: Int): Flow<UserSettingsEntity?>

    // isFirstLoginDone
    @Query("SELECT isFirstLoginDone FROM user_settings WHERE userId = :userId LIMIT 1")
    suspend fun isFirstLoginDone(userId: Int): Boolean?

    @Query("UPDATE user_settings SET isFirstLoginDone = :done WHERE userId = :userId")
    suspend fun updateFirstLoginDone(userId: Int, done: Boolean)

    // isBiometricsEnabled
    @Query("SELECT isBiometricsEnabled FROM user_settings WHERE userId = :userId LIMIT 1")
    suspend fun isBiometricsEnabled(userId: Int): Boolean?

    @Query("UPDATE user_settings SET isBiometricsEnabled = :enabled WHERE userId = :userId")
    suspend fun updateBiometricsEnabled(userId: Int, enabled: Boolean)

    // notesViewMode
    @Query("SELECT notesViewMode from user_settings WHERE userId = :userId LIMIT 1")
    suspend fun getNotesViewMode(userId: Int): ViewMode

    @Query("UPDATE user_settings SET notesViewMode = :viewMode WHERE userId = :userId")
    suspend fun setNotesViewMode(userId: Int, viewMode: ViewMode)

    // preferredLanguage
    @Query("SELECT preferredLanguage FROM user_settings WHERE userId = :userId LIMIT 1")
    suspend fun getPreferredLanguage(userId: Int): String

    @Query("UPDATE user_settings SET preferredLanguage = :languageCode WHERE userId = :userId")
    suspend fun setPreferredLanguage(userId: Int, languageCode: String)
}