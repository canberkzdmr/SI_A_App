package com.cbo.user.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.domain.usecase.GetUserUseCase
import com.example.core.session.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events = _events.asSharedFlow()

    fun logout() {
        viewModelScope.launch {
            logoutUseCase.invoke()
            _events.emit(ProfileEvent.LoggedOut)
        }
    }
}

data class ProfileUiState(
    val username: String = "",
    val email: String = "",
    val lastLoginDate: String = "",
    val registerDate: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed class ProfileEvent {
    object LoggedOut : ProfileEvent()
}
