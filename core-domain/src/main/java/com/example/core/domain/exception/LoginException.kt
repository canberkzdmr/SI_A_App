package com.example.core.domain.exception

sealed class LoginException : Exception() {
    class UserNotFoundException : LoginException()
    class InvalidCredentialsException : LoginException()
    class UserNotActiveException : LoginException()
    class DatabaseException : LoginException()
}
