package com.cbo.core.domain.usecase.theme

import com.cbo.core.domain.preferences.ThemePreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveDarkThemeEnabledUseCase @Inject constructor(
    private val repository: ThemePreferencesRepository,
) {
    operator fun invoke(): Flow<Boolean?> = repository.darkThemeOverride
}


