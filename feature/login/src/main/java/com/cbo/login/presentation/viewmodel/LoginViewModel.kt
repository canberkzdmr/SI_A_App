package com.cbo.login.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cbo.core.domain.model.User
import com.cbo.core.domain.usecase.VerifyPasswordUseCase
import com.cbo.core.session.UserSession
import com.cbo.login.domain.usecase.GetUserUseCase
import com.cbo.login.domain.usecase.LoginUseCase
import com.cbo.ui.snackbar.SnackbarHostProvider
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
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(LoginUiState())
        val uiState: StateFlow<LoginUiState> = _uiState

        fun onUsernameChanged(username: String) {
            _uiState.update { it.copy(username = username) }
        }

        fun onPasswordChanged(password: String) {
            _uiState.update { it.copy(password = password) }
        }

        fun login() {
            Log.i("LoginViewModel", "Login clicked")

            val username = _uiState.value.username
            val password = _uiState.value.password

            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                val userResult = getUserUseCase(username)
                val user = userResult.getOrNull()

                if (userResult.isFailure || user == null) {
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

                Log.i("LoginViewModel", "User logged in")
                SnackbarManager.showMessage(SnackbarMessage.Success("Welcome $username!"))

                loginUseCase(username, password)
                userSession.setUser(
                    User(id = user.id, username = user.username, email = user.email),
                )

                _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
            }
        }
    }

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
)
