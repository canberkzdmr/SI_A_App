package com.cbo.core.domain.preferences

import kotlinx.coroutines.flow.Flow

interface ThemePreferencesRepository {
    /**
     * If null, the app follows the system theme.
     * If non-null, the app uses the stored override (true = dark, false = light).
     */
    val darkThemeOverride: Flow<Boolean?>

    /**
     * Toggles between light/dark based on the theme currently shown to the user.
     * If we were following system (override is null), this will create an override.
     */
    suspend fun toggleDarkTheme(currentEffectiveDarkTheme: Boolean)
}


