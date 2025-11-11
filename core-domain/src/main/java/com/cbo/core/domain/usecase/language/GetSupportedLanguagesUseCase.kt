package com.cbo.core.domain.usecase.language

import com.cbo.core.domain.model.SupportedLanguage
import com.cbo.core.domain.repository.SupportedLanguageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSupportedLanguagesUseCase @Inject constructor(
    private val repository: SupportedLanguageRepository
) {
    operator fun invoke(): Flow<List<SupportedLanguage>> = repository.getSupportedLanguages()
}