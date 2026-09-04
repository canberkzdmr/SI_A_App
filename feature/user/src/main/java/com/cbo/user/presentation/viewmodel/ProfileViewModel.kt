package com.cbo.user.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cbo.core.domain.model.User
import com.cbo.core.domain.model.UserWithDetail
import com.cbo.user.domain.usecase.GetUserWithDetailUseCase
import com.cbo.core.domain.usecase.SetBiometricEnabledUseCase
import com.cbo.core.domain.usecase.theme.ToggleDarkThemeUseCase
import com.cbo.core.session.UserSession
import com.cbo.core.session.domain.usecase.GetActiveUserUseCase
import com.cbo.core.session.domain.usecase.LogoutUseCase
import com.cbo.ui.snackbar.SnackbarManager
import com.cbo.ui.snackbar.SnackbarMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.cbo.core.domain.usecase.ExportBackupUseCase
import com.cbo.core.domain.usecase.RestoreBackupUseCase

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val userSession: UserSession,
    private val getActiveUserUseCase: GetActiveUserUseCase,
    private val getUserWithDetailUseCase: GetUserWithDetailUseCase,
    private val setBiometricEnabledUseCase: SetBiometricEnabledUseCase,
    private val toggleDarkThemeUseCase: ToggleDarkThemeUseCase,
    private val exportBackupUseCase: ExportBackupUseCase,
    private val restoreBackupUseCase: RestoreBackupUseCase,
) : ViewModel() {
    val currentUser: Flow<User?> = userSession.currentUser
    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events = _events.asSharedFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            delay(500)
            getActiveUserUseCase()
                .catch { 
                    Log.e("ProfileViewModel", "Error loading active user", it)
                    _uiState.update { state -> state.copy(isLoading = false, errorMessage = "Failed to load user data") }
                }
                .collect { user: User? ->
                    user?.let { user ->
                        Log.d("ProfileViewModel", "Retrieved User: ${user.username}(${user.id})")
                        getUserWithDetailUseCase(user.id)
                            .catch { error ->
                                Log.e("ProfileViewModel", "Failed to load user details", error)
                                _uiState.value = _uiState.value.copy(
                                    username = user.username,
                                    email = user.email,
                                    isLoading = false,
                                    errorMessage = "Failed to load profile details"
                                )
                            }
                            .map { userWithDetail ->
                                userWithDetail?.let {
                                    ProfileUiState(
                                        userId = userWithDetail.user.id,
                                        username = userWithDetail.user.username,
                                        email = userWithDetail.user.email,
                                        avatarUrl = userWithDetail.userDetail?.avatarUrl.orEmpty(),
                                        isLoading = false,
                                        isBiometricEnabled = userWithDetail.userSettings.isBiometricsEnabled
                                    )
                                } ?: run {
                                    Log.e("ProfielViewModel", "Error while retrieving user details. User detail is null!")
                                    ProfileUiState(
                                        username = user.username,
                                        email = user.email,
                                        isLoading = false,
                                        errorMessage = "Failed to load profile details"
                                    )
                                }
                            }
                            .collect { newState ->
                                _uiState.value = newState
                            }
                    } ?: run {
                        _uiState.value = _uiState.value.copy(isLoading = false)
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
            SnackbarManager.showMessage(SnackbarMessage.Info("Logged out successfully!"))
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

    fun themeChange(currentEffectiveDarkTheme: Boolean) {
        viewModelScope.launch {
            // currentEffectiveDarkTheme is provided by the UI (it reflects either system theme or override)
            // so we can toggle from what the user currently sees.
            // (Fresh install: override is null -> we will create an override on first toggle.)
            toggleDarkThemeUseCase(currentEffectiveDarkTheme = currentEffectiveDarkTheme)
        }
    }

    fun languageChange() {
        // Logic to change language
    }

    fun manageCategories() {
        // Logic for categories
    }

    fun exportBackup(onReady: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val userId = _uiState.value.userId
                if (userId <= 0) {
                    SnackbarManager.showMessage(SnackbarMessage.Error("Kullanıcı oturumu bulunamadı."))
                    return@launch
                }
                val json = exportBackupUseCase(userId)
                onReady(json)
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to export backup", e)
                SnackbarManager.showMessage(SnackbarMessage.Error("Yedek alınamadı: ${e.message}"))
            }
        }
    }

    fun restoreBackup(jsonContent: String) {
        viewModelScope.launch {
            try {
                val userId = _uiState.value.userId
                if (userId <= 0) {
                    SnackbarManager.showMessage(SnackbarMessage.Error("Kullanıcı oturumu bulunamadı."))
                    return@launch
                }
                val summary = restoreBackupUseCase(userId, jsonContent)
                SnackbarManager.showMessage(
                    SnackbarMessage.Success(
                        "Yedek başarıyla geri yüklendi: ${summary.notesCount} not, ${summary.categoriesCount} kategori, ${summary.tagsCount} etiket"
                    )
                )
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to restore backup", e)
                SnackbarManager.showMessage(SnackbarMessage.Error("Yedek geri yüklenemedi: ${e.message}"))
            }
        }
    }

    fun toggleBiometrics() {
        viewModelScope.launch {
            setBiometricEnabledUseCase.invoke(userId = _uiState.value.userId, enabled = !_uiState.value.isBiometricEnabled)
            _uiState.update { it.copy(isBiometricEnabled = !it.isBiometricEnabled) }
        }
    }

    fun contactSupport() {
        // Logic to contact support
    }

}

data class ProfileUiState(
    val userId: Int = -1,
    val username: String? = "",
    val email: String? = "",
    val avatarUrl: String = "",
    val lastLoginDate: String? = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isBiometricEnabled: Boolean = false,
)

sealed class ProfileEvent {
    object LoggedOut : ProfileEvent()
}
