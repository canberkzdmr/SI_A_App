package com.cbo.user.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.domain.model.User
import com.example.core.session.UserSession
import com.example.core.session.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val userSession: UserSession,
) : ViewModel() {
    val currentUser: StateFlow<User?> = userSession.currentUser

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events = _events.asSharedFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        // Simulate network/data loading
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            delay(1000) // simulate network delay
            userSession.currentUser.collect { user ->
                if (user != null) {
                    _uiState.update {
                        it.copy(
                            username = user.username,
                            email = user.email,
                            isLoading = false,
                        )
                    }
                } else {
                    _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun logout() {
        // Handle logout logic
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                username = null,
                email = null,
                isLoading = false
            )
            logoutUseCase.invoke()
            userSession.clearUser()
            _events.emit(ProfileEvent.LoggedOut)
        }
    }

    fun editProfile(username: String, email: String) {
        // Update profile data
        _uiState.value = _uiState.value.copy(
            username = username,
            email = email
        )
    }

    fun changePassword() {
        // Logic to change password
    }

    fun deleteAccount() {
        // Logic to delete account
        _uiState.value = ProfileUiState(isLoading = true) // show shimmer while processing
    }

    fun themeChange() {
        // Logic to change theme
    }

    fun languageChange() {
        // Logic to change language
    }

    fun manageCategories() {
        // Logic for categories
    }

    fun exportNotes() {
        // Logic to export notes
    }

    fun enableBiometrics() {
        // Logic to enable biometrics
    }

    fun contactSupport() {
        // Logic to contact support
    }

}

data class ProfileUiState(
    val username: String? = "",
    val email: String? = "",
    val lastLoginDate: String? = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed class ProfileEvent {
    object LoggedOut : ProfileEvent()
}
