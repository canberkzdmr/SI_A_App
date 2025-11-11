package com.cbo.core.domain.usecase.language

import com.cbo.core.domain.repository.UserSettingsRepository
import javax.inject.Inject

class GetAppLanguageUseCase
    @Inject
    constructor(
        private val userSettingsRepository: UserSettingsRepository,
    ) {
        suspend operator fun invoke(userId: Int): Result<String> {
            return userSettingsRepository.getPreferredLanguage(userId)
        }
    }
