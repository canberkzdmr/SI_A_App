package com.cbo.core.data.repository

import com.cbo.core.data.prefs.ThemePreferencesManager
import com.cbo.core.domain.preferences.ThemePreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemePreferencesRepositoryImpl @Inject constructor(
    private val themePreferencesManager: ThemePreferencesManager,
) : ThemePreferencesRepository {
    override val darkThemeOverride: Flow<Boolean?> = themePreferencesManager.darkThemeOverride

    override suspend fun toggleDarkTheme(currentEffectiveDarkTheme: Boolean) {
        themePreferencesManager.toggleDarkTheme(currentEffectiveDarkTheme = currentEffectiveDarkTheme)
    }
}


