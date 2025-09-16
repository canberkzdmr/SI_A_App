package com.example.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.domain.model.User
import com.example.core.session.UserSession
import com.example.core.session.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userSession: UserSession
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState

    init {
        viewModelScope.launch {
            // Give splash screen a short delay for better UX (e.g., logo animation)
            delay(1000)

            val user = userSession.currentUser.value
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isLoggedIn = user != null
                )
            }
        }
    }
}

data class SplashUiState(
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean = false
)

sealed class SplashDestination {
    object Login: SplashDestination()
    object Main: SplashDestination()
}