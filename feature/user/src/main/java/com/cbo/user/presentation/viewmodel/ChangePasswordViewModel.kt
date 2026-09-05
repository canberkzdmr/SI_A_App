package com.cbo.user.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.cbo.core.logger.AppLogger
import androidx.lifecycle.viewModelScope
import com.cbo.core.domain.model.User
import com.cbo.core.session.domain.usecase.GetActiveUserUseCase
import com.cbo.user.domain.usecase.ChangePasswordUseCase
import com.cbo.ui.snackbar.SnackbarManager
import com.cbo.ui.snackbar.SnackbarMessage
import com.cbo.user.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val getActiveUserUseCase: GetActiveUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    private var currentUser: User? = null

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            getActiveUserUseCase()
                .catch { 
                    AppLogger.e("Error loading active user", it)
                    SnackbarManager.showMessage(SnackbarMessage.Error(stringRes = R.string.failed_to_load_user_data))
                }
                .collect { user ->
                    currentUser = user
                }
        }
    }

    fun onCurrentPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(
            currentPassword = password
        )
    }

    fun onNewPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(
            newPassword = password
        )
    }

    fun onConfirmPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = password
        )
    }

    fun onCurrentPasswordVisibilityToggle() {
        _uiState.value = _uiState.value.copy(
            isCurrentPasswordVisible = !_uiState.value.isCurrentPasswordVisible
        )
    }

    fun onNewPasswordVisibilityToggle() {
        _uiState.value = _uiState.value.copy(
            isNewPasswordVisible = !_uiState.value.isNewPasswordVisible
        )
    }

    fun onConfirmPasswordVisibilityToggle() {
        _uiState.value = _uiState.value.copy(
            isConfirmPasswordVisible = !_uiState.value.isConfirmPasswordVisible
        )
    }

    fun validateForm(): Boolean {
        val state = _uiState.value
        
        // Validate current password
        if (state.currentPassword.isEmpty()) {
            viewModelScope.launch {
                SnackbarManager.showMessage(SnackbarMessage.Error(stringRes = R.string.err_current_password_required))
            }
            return false
        }

        // Validate new password
        if (state.newPassword.isEmpty()) {
            viewModelScope.launch {
                SnackbarManager.showMessage(SnackbarMessage.Error(stringRes = R.string.err_new_password_required))
            }
            return false
        }

        if (!isValidPassword(state.newPassword)) {
            viewModelScope.launch {
                SnackbarManager.showMessage(SnackbarMessage.Error(stringRes = getPasswordValidationErrorRes(state.newPassword)))
            }
            return false
        }

        // Validate confirm password
        if (state.confirmPassword.isEmpty()) {
            viewModelScope.launch {
                SnackbarManager.showMessage(SnackbarMessage.Error(stringRes = R.string.err_confirm_password_required))
            }
            return false
        }

        if (state.newPassword != state.confirmPassword) {
            viewModelScope.launch {
                SnackbarManager.showMessage(SnackbarMessage.Error(stringRes = R.string.err_passwords_do_not_match))
            }
            return false
        }

        // Check if new password is same as current password
        if (state.currentPassword == state.newPassword) {
            viewModelScope.launch {
                SnackbarManager.showMessage(SnackbarMessage.Error(stringRes = R.string.err_new_password_same_as_current))
            }
            return false
        }

        return true
    }

    fun changePassword() {
        if (!validateForm()) {
            return
        }

        val user = currentUser
        if (user == null) {
            viewModelScope.launch {
                SnackbarManager.showMessage(SnackbarMessage.Error(stringRes = R.string.err_user_not_found))
            }
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            changePasswordUseCase(
                userId = user.id,
                currentPassword = _uiState.value.currentPassword,
                newPassword = _uiState.value.newPassword
            ).fold(
                onSuccess = {
                    AppLogger.i("Password changed successfully")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isPasswordChanged = true
                    )
                    SnackbarManager.showMessage(SnackbarMessage.Success(stringRes = R.string.password_changed_success))
                },
                onFailure = { error ->
                    AppLogger.e("Failed to change password", error)
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    SnackbarManager.showMessage(SnackbarMessage.Error(stringRes = R.string.failed_to_change_password))
                }
            )
        }
    }


    private fun isValidPassword(password: String): Boolean {
        val lengthValid = password.length >= 8
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        
        return lengthValid && hasUpperCase && hasLowerCase && hasDigit
    }

    private fun getPasswordValidationErrorRes(password: String): Int {
        return when {
            password.length < 8 -> R.string.err_password_min_length
            !password.any { it.isUpperCase() } -> R.string.err_password_uppercase
            !password.any { it.isLowerCase() } -> R.string.err_password_lowercase
            !password.any { it.isDigit() } -> R.string.err_password_number
            else -> R.string.err_password_invalid
        }
    }
}

data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isCurrentPasswordVisible: Boolean = false,
    val isNewPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isPasswordChanged: Boolean = false
)
