package com.cbo.core.data.repository

import com.cbo.core.data.mapper.SupportedLanguageEntityMapper
import com.cbo.core.database.dao.SupportedLanguageDao
import com.cbo.core.domain.model.SupportedLanguage
import com.cbo.core.domain.repository.SupportedLanguageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupportedLanguageRepositoryImpl @Inject constructor(
    private val dao: SupportedLanguageDao,
    private val mapper: SupportedLanguageEntityMapper
): SupportedLanguageRepository{
    override fun getSupportedLanguages(): Flow<List<SupportedLanguage>> {
        return dao.getSupportedLanguages().map { entities ->
            entities.map { mapper.toDomain(it) }
        }
    }

    override suspend fun getSupportedLanguagesSync(): List<SupportedLanguage> {
        return dao.getSupportedLanguagesSync().map { mapper.toDomain(it) }
    }

    override fun getAllSupportedLanguages(): Flow<List<SupportedLanguage>> {
        return dao.getAllSupportedLanguages().map { entities ->
            entities.map { mapper.toDomain(it) }
        }
    }

    override suspend fun getAllSupportedLanguagesSync(): List<SupportedLanguage> {
        return dao.getAllSupportedLanguagesSync().map { mapper.toDomain(it) }
    }

    override suspend fun getLanguageByCode(code: String): SupportedLanguage? {
        return dao.getLanguageByCode(code)?.let { mapper.toDomain(it) }
    }

    override suspend fun setLanguageEnabled(code: String, enabled: Boolean) {
        dao.setLanguageEnabled(code, enabled)
    }
}