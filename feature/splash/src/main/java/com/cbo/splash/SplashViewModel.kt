package com.cbo.splash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cbo.core.domain.model.User
import com.cbo.core.domain.preferences.PreferencesRepository
import com.cbo.core.domain.usecase.GetUserSettingsUseCase
import com.cbo.core.session.domain.usecase.GetActiveUserUseCase
import com.cbo.ui.snackbar.SnackbarManager
import com.cbo.ui.snackbar.SnackbarMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel
    @Inject
    constructor(
        private val userSession: com.cbo.core.session.UserSession,
        private val getActiveUserUseCase: GetActiveUserUseCase,
        private val getUserSettingsUseCase: GetUserSettingsUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SplashUiState())
        val uiState: StateFlow<SplashUiState> = _uiState

        init {
            viewModelScope.launch {
                // Give splash screen a short delay for better UX (e.g., logo animation)
                delay(2000)
                observeActiveUser()
            }
        }

        private fun observeActiveUser() {
            viewModelScope.launch {
                getActiveUserUseCase()
                    .onStart { _uiState.update { it.copy(isLoading = true) } }
                    .catch { _uiState.update { it.copy(isLoading = false, isLoggedIn = false) } }
                    .collect { user: User? ->
                        if (user != null) {
                            userSession.setUser(user) // hydrate session
                            val userSettingsResult = getUserSettingsUseCase.invoke(user.id)
                            when {
                                userSettingsResult.isSuccess -> {
                                    userSettingsResult.getOrNull()?.let { settings ->
                                        _uiState.update { it.copy(isLoading = false, isLoggedIn = true, isBiometricEnabled = settings.isBiometricsEnabled) }
                                    } ?: run {
                                        _uiState.update { it.copy(isLoading = false, isLoggedIn = true, isBiometricEnabled = false) }
                                    }
                                }
                                userSettingsResult.isFailure -> {
                                    Log.d("SplashViewModel", "Could not get user settings: ${userSettingsResult.exceptionOrNull()}")
                                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true, isBiometricEnabled = false) }
                                }
                            }
//                            _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                        } else {
                            userSession.clearUser()
                            _uiState.update { it.copy(isLoading = false, isLoggedIn = false, isBiometricEnabled = false) }
                        }
                    }
            }
        }

        fun showBiometricPromptMessage(message: String) {
            viewModelScope.launch {
                SnackbarManager.showMessage(SnackbarMessage.Error(message))
            }
        }
    }

data class SplashUiState(
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean = false,
    val isBiometricEnabled: Boolean = false,
)

sealed class SplashDestination {
    object Login : SplashDestination()

    object Main : SplashDestination()
}
