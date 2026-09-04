package com.cbo.memcloud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cbo.core.domain.usecase.theme.ObserveDarkThemeEnabledUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.cbo.memcloud.data.remote.RemoteConfigManager
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    observeDarkThemeEnabledUseCase: ObserveDarkThemeEnabledUseCase,
    private val remoteConfigManager: RemoteConfigManager
) : ViewModel() {
    val darkThemeOverride: StateFlow<Boolean?> =
        observeDarkThemeEnabledUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _isForceUpdateRequired = MutableStateFlow(false)
    val isForceUpdateRequired = _isForceUpdateRequired.asStateFlow()

    init {
        checkForceUpdate()
    }

    private fun checkForceUpdate() {
        viewModelScope.launch {
            val isSuccess = remoteConfigManager.fetchAndActivate()
            if (isSuccess) {
                // Update logger with latest remote config parameters
                com.cbo.core.logger.AppLogger.updateConfig(remoteConfigManager.getLoggerConfig())
                com.cbo.core.logger.AppLogger.d("Remote config fetched and AppLogger config updated")

                val minRequired = remoteConfigManager.getMinAppVersion()
                val currentVersion = BuildConfig.VERSION_CODE.toLong()
                if (currentVersion < minRequired) {
                    _isForceUpdateRequired.value = true
                }
            }
        }
    }
}


