package com.example.core.domain.model

sealed class LoginException(message: String): Exception(message) {
    class UserNotFoundException: LoginException("User not found")
}