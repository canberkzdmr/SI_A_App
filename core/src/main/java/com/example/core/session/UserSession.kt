package com.example.core.session

import com.example.core.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSession @Inject constructor() {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    fun setUser(user: User) {
        _currentUser.value = user
    }

    fun clearUser() {
        _currentUser.value = null
    }
}