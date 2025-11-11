package com.cbo.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.cbo.core.database.entity.SupportedLanguageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupportedLanguageDao: BaseDao<SupportedLanguageEntity> {
    @Query("SELECT * FROM supported_languages WHERE isEnabled = 1 ORDER BY sortOrder ASC, displayName ASC")
    fun getSupportedLanguages(): Flow<List<SupportedLanguageEntity>>

    @Query("SELECT * FROM supported_languages WHERE isEnabled = 1 ORDER BY sortOrder ASC, displayName ASC")
    suspend fun getSupportedLanguagesSync(): List<SupportedLanguageEntity>

    @Query("SELECT * FROM supported_languages ORDER BY sortOrder ASC, displayName ASC")
    fun getAllSupportedLanguages(): Flow<List<SupportedLanguageEntity>>

    @Query("SELECT * FROM supported_languages ORDER BY sortOrder ASC, displayName ASC")
    suspend fun getAllSupportedLanguagesSync(): List<SupportedLanguageEntity>

    @Query("SELECT * FROM SUPPORTED_LANGUAGES WHERE code = :code")
    suspend fun getLanguageByCode(code: String): SupportedLanguageEntity?

    @Query("UPDATE supported_languages SET isEnabled = :enabled WHERE code = :code")
    suspend fun setLanguageEnabled(code: String, enabled: Boolean)

    @Query("SELECT COUNT(*) FROM supported_languages WHERE isEnabled = 1")
    suspend fun getEnabledLanguagesCount(): Int

    @Query("SELECT COUNT(*) FROM supported_languages WHERE isEnabled = 0")
    suspend fun getDisabledLanguagesCount(): Int
}