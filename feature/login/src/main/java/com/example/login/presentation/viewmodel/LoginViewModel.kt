package com.example.login.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.domain.model.User
import com.example.core.domain.usecase.VerifyPasswordUseCase
import com.example.core.session.UserSession
import com.example.login.domain.usecase.LoginUseCase
import com.example.login.domain.usecase.GetUserUseCase
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
    private val userSession: UserSession
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
        val username = _uiState.value.username
        val password = _uiState.value.password

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val userResult = getUserUseCase(username)

            userResult?.let {
                if (userResult.isFailure) {
                    Log.e("LoginViewModel", "User not found")
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "User not found")
                    }
                    return@launch
                }

                val user = userResult.getOrNull()

                user?.let { user ->
                val isValid = verifyPasswordUseCase(user.username, password)
                    if (isValid) {
                        Log.i("LoginViewModel", "User logged in")
                        loginUseCase(username, password)
                        userSession.setUser(
                            User(
                                id = user.id,
                                username = user.username,
                                email = user.email,
                            )
                        )
                        _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                    } else {
                        Log.i("LoginViewModel", "Invalid password")
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = "Invalid password")
                        }
                    }
                }
            }
        }
    }
}

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null
)