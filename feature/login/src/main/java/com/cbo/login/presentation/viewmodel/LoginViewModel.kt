package com.cbo.login.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cbo.core.domain.exception.LoginException
import com.cbo.core.domain.model.User
import com.cbo.core.domain.usecase.SetBiometricEnabledUseCase
import com.cbo.core.domain.usecase.SetFirstLoginDoneUseCase
import com.cbo.core.logger.AppLogger
import com.cbo.login.R
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
        private val loginUseCase: LoginUseCase,
        private val setFirstLoginDoneUseCase: SetFirstLoginDoneUseCase,
        private val setBiometricEnabledUseCase: SetBiometricEnabledUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(LoginUiState())
        val uiState: StateFlow<LoginUiState> = _uiState

        fun onUsernameChanged(username: String) {
            _uiState.update { it.copy(username = username) }
        }

        fun onPasswordChanged(password: String) {
            _uiState.update { it.copy(password = password) }
        }

        fun login() =
            viewModelScope.launch {
                val username = _uiState.value.username
                val password = _uiState.value.password

                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                loginUseCase.invoke(username, password).fold(
                    onSuccess = {
                        AppLogger.i("User logged in")
                        SnackbarManager.showMessage(SnackbarMessage.Success(messageRes = R.string.welcome_user, formatArgs = listOf(username)))
                        _uiState.update { it.copy(isLoading = false, isLoggedIn = true, isFirstLoginDone = true) }
                    },
                    onFailure = { exception ->
                        when (exception) {
                            is LoginException.FirstLoginIsNotCompleted -> {
                                setShowBiometricDialog(true)
                                return@launch
                            }
                            else -> {
                                val errorMsg = exception.message
                                if (errorMsg != null) {
                                    SnackbarManager.showMessage(SnackbarMessage.Error(errorMsg))
                                } else {
                                    SnackbarManager.showMessage(SnackbarMessage.Error(messageRes = R.string.login_failed))
                                }
                                _uiState.update { it.copy(isLoading = false, errorMessage = errorMsg) }
                            }
                        }
                    },
                )
            }

        fun enableBiometricLogin(enabled: Boolean) {
            viewModelScope.launch {
                val user = getUser()
                user?.let {
                    setBiometricEnabledUseCase.invoke(it.id, enabled)
                } ?: run {
                    AppLogger.e("Enable BiometricLogin: User is null")
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
                user?.let {
                    setFirstLoginDoneUseCase.invoke(it.id, true)
                } ?: run {
                    AppLogger.e("User is null!")
                }
            }
        }

        private suspend fun getUser(): User? {
            val userResult = getUserUseCase(_uiState.value.username)
            userResult.fold(
                onSuccess = {
                    val user = userResult.getOrNull()
                    if (user == null) {
                        AppLogger.w("Get User Result: Success, User: null")
                    }
                    return user
                },
                onFailure = {
                    AppLogger.e("Get User Result: Fail")
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
