package com.cbo.core.domain.usecase.language

import com.cbo.core.domain.repository.UserSettingsRepository
import javax.inject.Inject

/**
 * Use case to set the application language for a specific user.
 * This updates the user's language preference in the database.
 */
class SetAppLanguageUseCase @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
) {
    /**
     * Sets the preferred language for the specified user.
     *
     * @param userId The ID of the user
     * @param languageCode ISO 639-1 language code (e.g., "en", "tr") or null for system default
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(userId: Int, languageCode: String?): Result<Unit> {
        return userSettingsRepository.setPreferredLanguage(userId, languageCode)
    }
}