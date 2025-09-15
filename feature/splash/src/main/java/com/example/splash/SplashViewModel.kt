package com.example.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.session.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
): ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination?>(null)
    val destination: StateFlow<SplashDestination?> = _destination

    init {
        viewModelScope.launch {
            sessionRepository.getSession().collect { session ->
                _destination.value = if (session != null) {
                    SplashDestination.Main
                } else {
                    SplashDestination.Login
                }
            }
        }
    }
}

sealed class SplashDestination {
    object Login: SplashDestination()
    object Main: SplashDestination()
}