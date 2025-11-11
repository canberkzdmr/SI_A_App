package com.cbo.core.data.mapper

import com.cbo.core.database.entity.SupportedLanguageEntity
import com.cbo.core.domain.model.SupportedLanguage
import javax.inject.Inject

class SupportedLanguageEntityMapper @Inject constructor() {

    fun toDomain(entity: SupportedLanguageEntity): SupportedLanguage {
        return SupportedLanguage(
            id = entity.id,
            code = entity.code,
            displayName = entity.displayName,
            nativeName = entity.nativeName,
            isEnabled = entity.isEnabled,
            sortOrder = entity.sortOrder,
        )
    }

    fun toEntity(domain: SupportedLanguage): SupportedLanguageEntity {
        return SupportedLanguageEntity(
            id = domain.id,
            code = domain.code,
            displayName = domain.displayName,
            nativeName = domain.nativeName,
            isEnabled = domain.isEnabled,
            sortOrder = domain.sortOrder,
        )
    }
}