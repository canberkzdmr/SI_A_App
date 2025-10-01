package com.cbo.core.data.repository

import com.cbo.core.data.prefs.PreferencesManager
import com.cbo.core.domain.preferences.PreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val preferencesManager: PreferencesManager
): PreferencesRepository {
    override fun setBiometricEnabled(enabled: Boolean) {
        preferencesManager.setBiometricEnabled(enabled)
    }

    override fun isBiometricEnabled(): Boolean {
        return preferencesManager.isBiometricEnabled()
    }
}