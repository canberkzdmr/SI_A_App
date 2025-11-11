package com.cbo.core.domain.repository

import com.cbo.core.domain.model.SupportedLanguage
import kotlinx.coroutines.flow.Flow

interface SupportedLanguageRepository {
    fun getSupportedLanguages(): Flow<List<SupportedLanguage>>
    suspend fun getSupportedLanguagesSync(): List<SupportedLanguage>
    fun getAllSupportedLanguages(): Flow<List<SupportedLanguage>>
    suspend fun getAllSupportedLanguagesSync(): List<SupportedLanguage>
    suspend fun getLanguageByCode(code: String): SupportedLanguage?
    suspend fun setLanguageEnabled(code: String, enabled: Boolean)
}