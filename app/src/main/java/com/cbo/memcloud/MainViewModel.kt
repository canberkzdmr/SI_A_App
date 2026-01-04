package com.cbo.memcloud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cbo.core.domain.usecase.theme.ObserveDarkThemeEnabledUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    observeDarkThemeEnabledUseCase: ObserveDarkThemeEnabledUseCase,
) : ViewModel() {
    val darkThemeOverride: StateFlow<Boolean?> =
        observeDarkThemeEnabledUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}


