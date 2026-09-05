package com.cbo.login.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cbo.core.common.base.UiState
import com.cbo.core.common.util.DateUtil
import com.cbo.core.common.validation.FieldValidation
import com.cbo.core.domain.exception.LoginException
import com.cbo.core.domain.exception.RegistrationException
import com.cbo.core.domain.usecase.SetBiometricEnabledUseCase
import com.cbo.core.logger.AppLogger
import com.cbo.login.R
import com.cbo.login.domain.model.RegisterUserModel
import com.cbo.login.domain.usecase.GetUserUseCase
import com.cbo.login.domain.usecase.LoginUseCase
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
        private val loginUseCase: LoginUseCase,
        private val getUserUseCase: GetUserUseCase,
        private val setBiometricEnabledUseCase: SetBiometricEnabledUseCase,
    ) : ViewModel() {

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
                if (result.isSuccess) {
                    val loginResult = loginUseCase(user.username, user.password)
                    if (loginResult.exceptionOrNull() is LoginException.FirstLoginIsNotCompleted) {
                        setShowBiometricDialog(true)
                    } else if (loginResult.isSuccess) {
                        SnackbarManager.showMessage(SnackbarMessage.Success(messageRes = R.string.welcome_user, formatArgs = listOf(user.username)))
                        onSuccess()
                        _uiState.value = UiState.Success(Unit)
                    } else {
                        val exception = loginResult.exceptionOrNull()
                        AppLogger.e("Auto login failed: ${exception?.message}")
                        val errorMsg = exception?.message
                        if (errorMsg != null) {
                            SnackbarManager.showMessage(SnackbarMessage.Warning(errorMsg))
                            _uiState.value = UiState.Error(errorMsg)
                        } else {
                            SnackbarManager.showMessage(SnackbarMessage.Warning(messageRes = R.string.login_failed))
                            _uiState.value = UiState.Error("Auto login failed")
                        }
                    }
                } else {
                    val exception = result.exceptionOrNull()
                    AppLogger.e(exception?.message ?: "Unknown error")
                    handleRegistrationException(exception)
                    val errorMsg = exception?.message
                    if (errorMsg != null) {
                        SnackbarManager.showMessage(SnackbarMessage.Warning(errorMsg))
                        _uiState.value = UiState.Error(errorMsg)
                    } else {
                        SnackbarManager.showMessage(SnackbarMessage.Warning(messageRes = R.string.unknown_error))
                        _uiState.value = UiState.Error("Unknown error")
                    }
                }
            }

        fun enableBiometricLogin(enabled: Boolean, onSuccess: () -> Unit) {
            viewModelScope.launch {
                val username = _registerState.value.username
                val password = _registerState.value.password
                val userResult = getUserUseCase(username)
                userResult.getOrNull()?.let { user ->
                    setBiometricEnabledUseCase(user.id, enabled)
                } ?: run {
                    AppLogger.e("Enable BiometricLogin: User is null")
                }
                setShowBiometricDialog(false)
                completeLogin(username, password, onSuccess)
            }
        }

        fun setShowBiometricDialog(enabled: Boolean) {
            _registerState.update { it.copy(showBiometricDialog = enabled) }
        }

        private suspend fun completeLogin(username: String, password: String, onSuccess: () -> Unit) {
            val loginResult = loginUseCase(username, password)
            if (loginResult.isSuccess) {
                SnackbarManager.showMessage(SnackbarMessage.Success(messageRes = R.string.welcome_user, formatArgs = listOf(username)))
                onSuccess()
                _uiState.value = UiState.Success(Unit)
            } else {
                val exception = loginResult.exceptionOrNull()
                AppLogger.e("Auto login failed: ${exception?.message}")
                val errorMsg = exception?.message
                if (errorMsg != null) {
                    SnackbarManager.showMessage(SnackbarMessage.Warning(errorMsg))
                    _uiState.value = UiState.Error(errorMsg)
                } else {
                    SnackbarManager.showMessage(SnackbarMessage.Warning(messageRes = R.string.login_failed))
                    _uiState.value = UiState.Error("Auto login failed")
                }
            }
        }


        private fun handleRegistrationException(exception: Throwable?) {
            when (exception) {
                is RegistrationException -> {
                    AppLogger.w("Registration error: ${exception.message}")
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
                FieldValidation(
                    isValid = false,
                    errorMessage = "Username must be at least 3 characters",
                    errorMessageRes = R.string.err_username_min_chars
                )
            }

        fun validateEmail(): FieldValidation =
            if (android.util.Patterns.EMAIL_ADDRESS
                    .matcher(_registerState.value.email)
                    .matches()
            ) {
                FieldValidation(true)
            } else {
                FieldValidation(
                    isValid = false,
                    errorMessage = "Invalid email format",
                    errorMessageRes = R.string.err_invalid_email
                )
            }

        fun validatePassword(): FieldValidation {
            val password = _registerState.value.password
            return if (isValidPassword(password)) {
                FieldValidation(true)
            } else {
                FieldValidation(
                    isValid = false,
                    errorMessage = getPasswordError(password),
                    errorMessageRes = getPasswordErrorRes(password)
                )
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

        private fun getPasswordErrorRes(password: String): Int =
            when {
                password.length < 8 -> R.string.err_password_min_chars
                !password.any { it.isUpperCase() } -> R.string.err_password_uppercase
                !password.any { it.isLowerCase() } -> R.string.err_password_lowercase
                !password.any { it.isDigit() } -> R.string.err_password_number
                else -> R.string.err_password_invalid
            }

        fun validateReTypePassword(): FieldValidation =
            if (_registerState.value.password == _registerState.value.reTypePassword) {
                FieldValidation(true)
            } else {
                FieldValidation(
                    isValid = false,
                    errorMessage = "Passwords are not matching",
                    errorMessageRes = R.string.err_passwords_not_matching
                )
            }

        private fun validateTermsAndConditionsChecker(): FieldValidation =
            if (_registerState.value.termsAndConditionsChecked) {
                FieldValidation(true)
            } else {
                FieldValidation(
                    isValid = false,
                    errorMessage = "Please approve terms and conditions",
                    errorMessageRes = R.string.err_accept_terms
                )
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
    val showBiometricDialog: Boolean = false,
)
