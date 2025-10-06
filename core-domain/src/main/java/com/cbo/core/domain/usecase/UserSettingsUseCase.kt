package com.cbo.core.domain.usecase

import com.cbo.core.domain.model.UserSettings
import com.cbo.core.domain.repository.UserSettingsRepository
import javax.inject.Inject

class GetUserSettingsUseCase @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
) {
    suspend operator fun invoke(userId: Int): Result<UserSettings> {
        return userSettingsRepository.getUserSettings(userId = userId)
    }
}

class SetFirstLoginDoneUseCase @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
) {
    suspend operator fun invoke(userId: Int, done: Boolean): Result<Unit> {
        return userSettingsRepository.setFirstLoginDone(userId = userId, done = done)
    }
}

class SetBiometricEnabledUseCase @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
) {
    suspend operator fun invoke(userId: Int, enabled: Boolean): Result<Unit> {
        return userSettingsRepository.setBiometricsEnabled(userId = userId, enabled = enabled)
    }
}