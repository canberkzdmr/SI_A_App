package com.cbo.core.data.prefs

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext context: Context,
){
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit { putBoolean("biometric_enabled", enabled) }
    }

    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean("biometrics_enabled", false)
    }
}