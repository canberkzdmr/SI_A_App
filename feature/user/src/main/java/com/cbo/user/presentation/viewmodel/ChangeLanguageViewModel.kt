package com.cbo.user.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cbo.core.data.locale.LocaleManager
import com.cbo.core.data.prefs.LanguagePreferencesManager
import com.cbo.core.domain.model.SupportedLanguage
import com.cbo.core.domain.usecase.language.GetAllSupportedLanguagesUseCase
import com.cbo.core.domain.usecase.language.GetAppLanguageUseCase
import com.cbo.core.domain.usecase.language.SetAppLanguageUseCase
import com.cbo.core.session.UserSession
import com.cbo.ui.components.AppSwitchOption
import com.cbo.ui.snackbar.SnackbarManager
import com.cbo.ui.snackbar.SnackbarMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangeLanguageViewModel @Inject constructor(
    private val getAllSupportedLanguages: GetAllSupportedLanguagesUseCase,
    private val getAppLanguage: GetAppLanguageUseCase,
    private val setAppLanguageUseCase: SetAppLanguageUseCase,
    private val localeManager: LocaleManager,
    private val languagePreferencesManager: LanguagePreferencesManager,
    private val session: UserSession,
): ViewModel() {
    
    private val _recreateActivityEvent = MutableSharedFlow<Unit>(replay = 0)
    val recreateActivityEvent: SharedFlow<Unit> = _recreateActivityEvent.asSharedFlow()

    private val _uiState = MutableStateFlow(ChangeLanguageUiState())
    val uiState: StateFlow<ChangeLanguageUiState> = _uiState

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            loadLanguages()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun loadLanguages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val user = session.currentUser.firstOrNull()
            user?.let {
                getAllSupportedLanguages()
                    .catch { error ->
                        Log.e("ChangeLanguageViewModel", "Error loading languages", error)
                        _uiState.update { it.copy(
                            isLoading = false,
                            errorMessage = "Failed to load languages"
                        ) }
                    }
                    .map { languages ->
                        Log.d("ChangeLanguageViewModel", "languages retrieved: $languages")
                        val currentLanguageResult = getAppLanguage(user.id)
                        val currentLanguageCode = currentLanguageResult.getOrElse { "en" }

                        val options = languages.map { language ->
                            AppSwitchOption(
                                id = language.code,
                                label = language.displayName,
                                enabled = language.isEnabled
                            )
                        }

                        ChangeLanguageUiState(
                            isLoading = false,
                            languages = languages,
                            languageOptions = options,
                            selectedLanguageCode = currentLanguageCode
                        )
                    }
                    .collect { newState ->
                        _uiState.value = newState
                    }
            } ?: run {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "No active user session"
                ) }
            }
        }
    }

    fun setAppLanguage(languageCode: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            session.currentUser.firstOrNull()?.let { user ->
                setAppLanguageUseCase(userId = user.id, languageCode = languageCode).fold(
                    onSuccess = {
                        // Save to SharedPreferences for synchronous access on next app start
                        languagePreferencesManager.saveLanguagePreference(user.id, languageCode)
                        
                        // Apply the language change to the system
                        localeManager.setAppLocale(languageCode)
                        
                        _uiState.update { it.copy(isLoading = false, selectedLanguageCode = languageCode ?: "en") }
                        val languageName = _uiState.value.languages
                            .firstOrNull { it.code == languageCode }
                            ?.displayName ?: "System default"
                        
                        Log.d("ChangeLanguageViewModel", "Language changed to: $languageCode, emitting recreate event...")
                        
                        // Emit event to recreate the activity to apply the new locale immediately
                        _recreateActivityEvent.emit(Unit)
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to change language") }
                        SnackbarManager.showMessage(SnackbarMessage.Error("Failed to change language"))
                        Log.e("ChangeLanguageViewModel", "Error changing language", error)
                    }
                )
            } ?: run {
                _uiState.update { it.copy(isLoading = false, errorMessage = "No active user session") }
                Log.e("ChangeLanguageViewModel", "No active user session")
            }
        }
    }
}

data class ChangeLanguageUiState(
    val isLoading: Boolean = false,
    val languages: List<SupportedLanguage> = emptyList(),
    val languageOptions: List<AppSwitchOption> = emptyList(),
    val selectedLanguageCode: String = "en",
    val errorMessage: String? = null
)