package com.cbo.core.data.prefs

import android.content.Context
import androidx.core.content.edit
import com.cbo.core.common.constants.PreferenceKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemePreferencesManager @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PreferenceKeys.PREFS_FILE, Context.MODE_PRIVATE)

    fun darkThemeOverrideSync(): Boolean? =
        if (prefs.contains(PreferenceKeys.DARK_THEME_ENABLED)) {
            prefs.getBoolean(PreferenceKeys.DARK_THEME_ENABLED, false)
        } else {
            null
        }

    fun setDarkThemeOverride(override: Boolean?) {
        prefs.edit {
            if (override == null) {
                remove(PreferenceKeys.DARK_THEME_ENABLED)
            } else {
                putBoolean(PreferenceKeys.DARK_THEME_ENABLED, override)
            }
        }
    }

    fun toggleDarkTheme(currentEffectiveDarkTheme: Boolean) {
        val currentOverride = darkThemeOverrideSync()
        val currentShown = currentOverride ?: currentEffectiveDarkTheme
        setDarkThemeOverride(!currentShown)
    }

    val darkThemeOverride: Flow<Boolean?> =
        callbackFlow {
            val listener =
                android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == PreferenceKeys.DARK_THEME_ENABLED) {
                        trySend(darkThemeOverrideSync())
                    }
                }

            trySend(darkThemeOverrideSync())
            prefs.registerOnSharedPreferenceChangeListener(listener)

            awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
        }.distinctUntilChanged()
}


