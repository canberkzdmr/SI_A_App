package com.cbo.core.data.prefs

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit
import com.cbo.core.common.constants.PreferenceKeys

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext context: Context,
){
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PreferenceKeys.PREFS_FILE, Context.MODE_PRIVATE)

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(PreferenceKeys.BIOMETRIC_ENABLED, enabled) }
    }

    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean(PreferenceKeys.BIOMETRIC_ENABLED, false)
    }

    fun setFirstLoginDone(enabled: Boolean) {
        prefs.edit { putBoolean(PreferenceKeys.FIRST_LOGIN_DONE, enabled) }
    }

    fun isFirstLoginDone(): Boolean {
        return prefs.getBoolean(PreferenceKeys.FIRST_LOGIN_DONE, false)
    }
}