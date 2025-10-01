package com.cbo.login.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cbo.core.common.base.UiState
import com.cbo.core.common.util.DateUtil
import com.cbo.core.common.validation.FieldValidation
import com.cbo.core.domain.exception.RegistrationException
import com.cbo.login.domain.model.RegisterUserModel
import com.cbo.login.domain.usecase.RegisterUserUseCase
import com.cbo.ui.snackbar.SnackbarManager
import com.cbo.ui.snackbar.SnackbarMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel
    @Inject
    constructor(
        private val registerUserUseCase: RegisterUserUseCase,
    ) : ViewModel() {
        private val TAG = "RegisterViewModel"

        private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
        val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

        private val _registerState = MutableStateFlow(RegisterState())
        val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

        // --- Registration ---
        fun register(onSuccess: () -> Unit) =
            viewModelScope.launch {
                _uiState.value = UiState.Loading

                val user =
                    with(_registerState.value) {
                        RegisterUserModel(
                            id = 0,
                            username = username,
                            password = password,
                            retypePassword = reTypePassword,
                            email = email,
                            termsAndConditionsChecked = termsAndConditionsChecked,
                            lastPasswordChangeDate = DateUtil.fullDate(),
                            registerDate = DateUtil.fullDate(),
                        )
                    }

                val result = registerUserUseCase(user)
                _uiState.value = handleRegisterResult(result, onSuccess)
            }

        private fun handleRegisterResult(
            result: Result<Unit>,
            onSuccess: () -> Unit,
        ): UiState<Unit> =
            when {
                result.isSuccess -> {
                    onSuccess()
                    UiState.Success(Unit)
                }
                result.isFailure -> {
                    val exception = result.exceptionOrNull()
                    Log.e(TAG, exception?.message ?: "Unknown error")
                    handleRegistrationException(exception)
                    viewModelScope.launch {
                        SnackbarManager.showMessage(
                            SnackbarMessage.Warning(exception?.message ?: "Unknown error"),
                        )
                    }
                    UiState.Error(exception?.message ?: "Unknown error")
                }
                else -> {
                    viewModelScope.launch { SnackbarManager.showMessage(SnackbarMessage.Error("Unknown error")) }
                    UiState.Error("Unknown error")
                }
            }

        private fun handleRegistrationException(exception: Throwable?) {
            when (exception) {
                is RegistrationException -> {
                    Log.w(TAG, "Registration error: ${exception.message}")
                }
            }
        }

        // --- Field updates ---
        fun updateUsername(name: String) = updateField { copy(username = name) }

        fun updateEmail(email: String) = updateField { copy(email = email) }

        fun updatePassword(password: String) = updateField { copy(password = password) }

        fun updateRetypePassword(reTypePassword: String) = updateField { copy(reTypePassword = reTypePassword) }

        fun updateTermsAndConditionsChecker(checked: Boolean) = updateField { copy(termsAndConditionsChecked = checked) }

        /**
         * IMPORTANT: compute the updated state first, then compute isValid based on the updated state.
         * This prevents validating "old" values.
         */
        private fun updateField(update: RegisterState.() -> RegisterState) {
            _registerState.update { current ->
                val updated = current.update() // apply the field change
                updated.copy(isValid = validateAllFields(updated)) // validate using updated state
            }
        }

        // --- Validation helpers ---
        private fun validateAllFields(state: RegisterState): Boolean =
            state.username.length >= 3 &&
                android.util.Patterns.EMAIL_ADDRESS
                    .matcher(state.email)
                    .matches() &&
                isValidPassword(state.password) &&
                state.password == state.reTypePassword &&
                state.termsAndConditionsChecked

        private fun isValidPassword(password: String): Boolean =
            password.length >= 8 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isLowerCase() } &&
                password.any { it.isDigit() }

        fun validateUserName(): FieldValidation =
            if (_registerState.value.username.length >= 3) {
                FieldValidation(true)
            } else {
                FieldValidation(false, "Username must be at least 3 characters")
            }

        fun validateEmail(): FieldValidation =
            if (android.util.Patterns.EMAIL_ADDRESS
                    .matcher(_registerState.value.email)
                    .matches()
            ) {
                FieldValidation(true)
            } else {
                FieldValidation(false, "Invalid email format")
            }

        fun validatePassword(): FieldValidation {
            val password = _registerState.value.password
            return if (isValidPassword(password)) {
                FieldValidation(true)
            } else {
                FieldValidation(false, getPasswordError(password))
            }
        }

        private fun getPasswordError(password: String): String =
            when {
                password.length < 8 -> "Must be at least 8 characters"
                !password.any { it.isUpperCase() } -> "Include at least one uppercase letter"
                !password.any { it.isLowerCase() } -> "Include at least one lowercase letter"
                !password.any { it.isDigit() } -> "Include at least one number"
                else -> "Invalid password"
            }

        fun validateReTypePassword(): FieldValidation =
            if (_registerState.value.password == _registerState.value.reTypePassword) {
                FieldValidation(true)
            } else {
                FieldValidation(false, "Passwords are not matching")
            }

        private fun validateTermsAndConditionsChecker(): FieldValidation =
            if (_registerState.value.termsAndConditionsChecked) {
                FieldValidation(true)
            } else {
                FieldValidation(false, "Please approve terms and conditions")
            }
    }

// --- State Data Class ---
data class RegisterState(
    val username: String = "",
    val password: String = "",
    val reTypePassword: String = "",
    val email: String = "",
    val termsAndConditionsChecked: Boolean = false,
    val isValid: Boolean = false,
)
