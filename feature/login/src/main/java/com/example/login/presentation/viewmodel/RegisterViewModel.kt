package com.example.login.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.base.BaseUiState
import com.example.core.util.DateUtil
import com.example.login.domain.model.User
import com.example.login.domain.usecase.RegisterUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel
    @Inject
    constructor(
        private val registerUserUseCase: RegisterUserUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(RegisterUiState())
        val uiState: StateFlow<RegisterUiState> = _uiState

        val username = MutableStateFlow("")
        val password = MutableStateFlow("")

        fun register(onSuccess: () -> Unit) {
            viewModelScope.launch {
                val user =
                    User(
                        id = 0,
                        username = _uiState.value.username,
                        password = _uiState.value.password,
                        email = _uiState.value.email,
                        lastPasswordChangeDate = DateUtil.fullDate(),
                        registerDate = DateUtil.fullDate(),
                    )
                if (registerUserUseCase(user)) onSuccess()
            }
        }

        fun updateUsername(name: String) {
            _uiState.value.copy(username = name)
        }
    }

data class RegisterUiState(
    val username: String = "",
    val password: String = "",
    val reTypePassword: String = "",
    val email: String = "",
    val termsAndConditionsChecked: Boolean = false,
    val isValid: Boolean = false,
) : BaseUiState()
