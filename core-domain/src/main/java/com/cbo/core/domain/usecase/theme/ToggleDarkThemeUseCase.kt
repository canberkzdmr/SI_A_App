package com.cbo.core.domain.usecase.theme

import com.cbo.core.domain.preferences.ThemePreferencesRepository
import javax.inject.Inject

class ToggleDarkThemeUseCase @Inject constructor(
    private val repository: ThemePreferencesRepository,
) {
    suspend operator fun invoke(currentEffectiveDarkTheme: Boolean) {
        repository.toggleDarkTheme(currentEffectiveDarkTheme = currentEffectiveDarkTheme)
    }
}


