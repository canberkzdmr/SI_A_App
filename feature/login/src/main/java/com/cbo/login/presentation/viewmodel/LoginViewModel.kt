package com.cbo.login.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cbo.core.domain.model.User
import com.cbo.core.domain.preferences.PreferencesRepository
import com.cbo.core.domain.usecase.GetUserSettingsUseCase
import com.cbo.core.domain.usecase.SetBiometricEnabledUseCase
import com.cbo.core.domain.usecase.SetFirstLoginDoneUseCase
import com.cbo.core.domain.usecase.VerifyPasswordUseCase
import com.cbo.core.session.UserSession
import com.cbo.login.domain.usecase.GetUserUseCase
import com.cbo.login.domain.usecase.LoginUseCase
import com.cbo.ui.snackbar.SnackbarManager
import com.cbo.ui.snackbar.SnackbarMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel
    @Inject
    constructor(
        private val getUserUseCase: GetUserUseCase,
        private val verifyPasswordUseCase: VerifyPasswordUseCase,
        private val loginUseCase: LoginUseCase,
        private val userSession: UserSession,
        private val preferencesRepository: PreferencesRepository,
        private val getUserSettingsUseCase: GetUserSettingsUseCase,
        private val setFirstLoginDoneUseCase: SetFirstLoginDoneUseCase,
        private val setBiometricEnabledUseCase: SetBiometricEnabledUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(LoginUiState())
        val uiState: StateFlow<LoginUiState> = _uiState

        private fun loadUserSettings() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                val user = getUser()

                user?.let {
                    val userSettingsResult = getUserSettingsUseCase.invoke(it.id)
                    when {
                        userSettingsResult.isFailure -> {
                            val exception = userSettingsResult.exceptionOrNull()
                            Log.e("LoginViewModel", exception?.message ?: "Unknown error")
                        }
                        userSettingsResult.isSuccess -> {
                            val userSettings = userSettingsResult.getOrNull()
                            userSettings?.let { settings ->

                            } ?: run {
                                Log.e("LoginViewModel", "Get User Settings result is success, but could not retrieve settings.")
                            }
                        }
                    }
                } ?: run {
                    Log.e("LoginViewModel", "init, User not found")
                    _uiState.update {
                        it.copy(
                            isLoading = false, errorMessage = "User not found"
                        )
                    }
                }
            }
        }

        fun onUsernameChanged(username: String) {
            _uiState.update { it.copy(username = username) }
        }

        fun onPasswordChanged(password: String) {
            _uiState.update { it.copy(password = password) }
        }

        fun login() {
            val username = _uiState.value.username
            val password = _uiState.value.password

            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                val user = getUser()

                if (user == null) {
                    Log.e("LoginViewModel", "User not found")
                    SnackbarManager.showMessage(SnackbarMessage.Error("Check user informations"))
                    _uiState.update { it.copy(isLoading = false, errorMessage = "User not found") }
                    return@launch
                }

                val isValid = verifyPasswordUseCase(user.username, password)
                if (!isValid) {
                    Log.i("LoginViewModel", "Invalid password")
                    SnackbarManager.showMessage(SnackbarMessage.Error("Given informations are not correct"))
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Invalid password") }
                    return@launch
                }

                val userSettings = getUserSettingsUseCase(user.id)
                when {
                    userSettings.isSuccess -> {
                        userSettings.getOrNull()?.let {
                            if (!it.isFirstLoginDone) {
                                setFirstLoginDone()
                                setShowBiometricDialog(true)
                                return@launch
                            }
                        }
                    }
                    userSettings.isFailure -> {
                        Log.e("LoginViewModel", "Get User Settings failed: ${userSettings.exceptionOrNull()}")
                    }
                }

                loginUseCase(username, password)
                userSession.setUser(
                    User(id = user.id, username = user.username, email = user.email),
                )
                Log.i("LoginViewModel", "User logged in")
                SnackbarManager.showMessage(SnackbarMessage.Success("Welcome $username!"))

                _uiState.update { it.copy(isLoading = false, isLoggedIn = true, isFirstLoginDone = true) }
            }
        }

        fun enableBiometricLogin(enabled: Boolean) {
            viewModelScope.launch {
                val user = getUser()
                user?.let {
                    setBiometricEnabledUseCase.invoke(it.id, enabled)
                } ?: run {
                    Log.e("LoginViewModel", "Enable BiometricLogin: User is null")
                }
            }
        }

        fun showBiometricPromptMessage(message: String) {
            viewModelScope.launch {
                SnackbarManager.showMessage(SnackbarMessage.Error(message))
            }
        }

        fun setShowBiometricDialog(enabled: Boolean) {
            _uiState.update { it.copy(showBiometricDialog = enabled) }
        }

        fun setFirstLoginDone() {
            viewModelScope.launch {
                val user = getUser()
                user?.let { it ->
                    setFirstLoginDoneUseCase.invoke(it.id, true)
                } ?: run {
                    Log.e("LoginViewModel", "User is null!")
                }
            }
        }

        private suspend fun getUser(): User? {
            val userResult = getUserUseCase(_uiState.value.username)
            userResult.fold(
                onSuccess = {
                    val user = userResult.getOrNull()
                    if (user == null) {
                        Log.w("LoginViewModel", "Get User Result: Success, User: null")
                    }
                    return user
                },
                onFailure = {
                    Log.e("LoginViewModel", "Get User Result: Fail")
                    return null
                },
            )
        }
    }

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
    val isFirstLoginDone: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val showBiometricDialog: Boolean = false,
)
