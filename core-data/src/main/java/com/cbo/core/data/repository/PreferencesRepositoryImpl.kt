package com.cbo.core.data.repository

import android.util.Log
import com.cbo.core.data.prefs.PreferencesManager
import com.cbo.core.domain.preferences.PreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val preferencesManager: PreferencesManager
): PreferencesRepository {
    override fun setBiometricEnabled(enabled: Boolean) {
        Log.d("PreferencesRepositoryImpl", "Biometric Login Set to -> $enabled")
        preferencesManager.setBiometricEnabled(enabled)
    }

    override fun isBiometricEnabled(): Boolean {
        return preferencesManager.isBiometricEnabled()
    }

    override fun setFirstLoginDone(enabled: Boolean) {
        Log.d("PreferencesRepositoryImpl", "First Login Done Set to -> $enabled")
        preferencesManager.setFirstLoginDone(enabled)
    }

    override fun isFirstLoginDone(): Boolean {
        return preferencesManager.isFirstLoginDone()
    }
}