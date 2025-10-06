package com.cbo.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cbo.core.database.entity.UserSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: UserSettings)

    @Query("SELECT * FROM user_settings WHERE userId = :userId LIMIT 1")
    suspend fun getUserSettings(userId: Int): UserSettings?

    @Query("SELECT * FROM user_settings WHERE userId = :userId LIMIT 1")
    fun getUserSettingsFlow(userId: Int): Flow<UserSettings?>

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
}