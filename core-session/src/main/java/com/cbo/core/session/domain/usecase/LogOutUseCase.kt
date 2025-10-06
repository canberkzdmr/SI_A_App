package com.cbo.core.session.domain.usecase

import com.cbo.core.domain.preferences.PreferencesRepository
import com.cbo.core.session.domain.repository.SessionRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke() {
        preferencesRepository.setBiometricEnabled(false)
        sessionRepository.clearSession()
    }
}
