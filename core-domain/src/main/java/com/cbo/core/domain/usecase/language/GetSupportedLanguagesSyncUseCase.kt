package com.cbo.core.domain.usecase.language

import com.cbo.core.domain.model.SupportedLanguage
import com.cbo.core.domain.repository.SupportedLanguageRepository
import javax.inject.Inject

class GetSupportedLanguagesSyncUseCase @Inject constructor(
    private val repository: SupportedLanguageRepository
) {
    suspend operator fun invoke(): List<SupportedLanguage> = repository.getSupportedLanguagesSync()
}