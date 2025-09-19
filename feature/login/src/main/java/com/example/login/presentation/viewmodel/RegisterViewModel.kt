package com.example.login.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.base.UiState
import com.example.core.common.util.DateUtil
import com.example.core.common.validation.FieldValidation
import com.example.login.domain.model.User
import com.example.login.domain.usecase.RegisterUserUseCase
import com.example.ui.snackbar.SnackbarManager
import com.example.ui.snackbar.SnackbarMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val uiState: StateFlow<UiState<Unit>> = _uiState
    private val _registerState = MutableStateFlow(RegisterState())
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    fun register(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val user =
                User(
                    id = 0,
                    username = _registerState.value.username,
                    password = _registerState.value.password,
                    email = _registerState.value.email,
                    termsAndConditionsChecked = _registerState.value.termsAndConditionsChecked,
                    lastPasswordChangeDate = DateUtil.fullDate(),
                    registerDate = DateUtil.fullDate(),
                )
            val result = registerUserUseCase.invoke(user)
            _uiState.value = when {
                result.isSuccess -> {
                    onSuccess()
                    UiState.Success(Unit)
                }

                result.isFailure -> {
                    Log.e("RegisterViewmodel", result.exceptionOrNull()?.message ?: "Bilinmeyen hata")
                    SnackbarManager.showMessage(SnackbarMessage.Error(result.exceptionOrNull()?.message ?: "Bilinmeyen hata"))
                    UiState.Error(
                        result.exceptionOrNull()?.message ?: "Bilinmeyen hata"
                    )
                }

                else -> {
                    Log.e("RegisterViewmodel", result.exceptionOrNull()?.message ?: "Bilinmeyen hata")
                    SnackbarManager.showMessage(SnackbarMessage.Error(result.exceptionOrNull()?.message ?: "Bilinmeyen hata"))
                    UiState.Error(
                        result.exceptionOrNull()?.message ?: "Bilinmeyen hata"
                    )
                }
            }
        }
    }

    fun updateUsername(name: String) {
        _registerState.value = _registerState.value.copy(username = name)
    }

    fun updateEmail(email: String) {
        _registerState.value = _registerState.value.copy(email = email)
    }

    fun updatePassword(password: String) {
        _registerState.value = _registerState.value.copy(password = password)
    }

    fun updateRetypePassword(retypePassword: String) {
        _registerState.value = _registerState.value.copy(reTypePassword = retypePassword)
    }

    fun updateTermsAndConditionsChecker(termsAndConditionsChecked: Boolean) {
        _registerState.value =
            _registerState.value.copy(termsAndConditionsChecked = termsAndConditionsChecked)
    }

    fun isRegistrationValid(): Boolean {
        var isValid = false
        viewModelScope.launch {
            if (validateUserName().isValid &&
                validateEmail().isValid &&
                validatePassword().isValid &&
                validateReTypePassword().isValid &&
                validateTermsAndConditionsChecker().isValid
            ) {
                isValid = true
            }
        }
        return isValid
    }

    fun validateUserName(): FieldValidation {
        return if (_registerState.value.username.length >= 3) FieldValidation(true)
        else FieldValidation(false, "Username must be at least 3 characters")
    }

    fun validateEmail(): FieldValidation {
        return if (android.util.Patterns.EMAIL_ADDRESS.matcher(_registerState.value.email)
                .matches()
        ) FieldValidation(true)
        else FieldValidation(false, "Invalid email format")
    }

    fun validatePassword(): FieldValidation {
        val lengthValid = _registerState.value.password.length >= 8
        val hasUpperCase = _registerState.value.password.any { it.isUpperCase() }
        val hasLowerCase = _registerState.value.password.any { it.isLowerCase() }
        val hasDigit = _registerState.value.password.any { it.isDigit() }

        return if (lengthValid && hasUpperCase && hasLowerCase && hasDigit) FieldValidation(true)
        else FieldValidation(
            false,
            getPasswordError()
        )
    }

    private fun getPasswordError(): String? {
        return when {
            _registerState.value.password.length < 8 -> "Must be at least 8 characters"
            !_registerState.value.password.any { it.isUpperCase() } -> "Include at least one uppercase letter"
            !_registerState.value.password.any { it.isLowerCase() } -> "Include at least one lowercase letter"
            !_registerState.value.password.any { it.isDigit() } -> "Include at least one number"
            else -> null
        }
    }

    fun validateReTypePassword(): FieldValidation {
        return if (_registerState.value.password.contentEquals(_registerState.value.reTypePassword)) FieldValidation(
            true
        )
        else FieldValidation(false, "Passwords are not matching")
    }

    private fun validateTermsAndConditionsChecker(): FieldValidation {
        return if (_registerState.value.termsAndConditionsChecked) {
            FieldValidation(true)
        } else {
            Log.d(TAG, "Terms and conditions have not been checked")
            FieldValidation(false, "Please approve terms and conditions")
        }
    }
}

data class RegisterState(
    val username: String = "",
    val password: String = "",
    val reTypePassword: String = "",
    val email: String = "",
    val termsAndConditionsChecked: Boolean = false,
    val isValid: Boolean = false,
)
