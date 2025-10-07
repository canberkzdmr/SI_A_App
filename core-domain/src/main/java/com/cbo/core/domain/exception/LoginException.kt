package com.cbo.core.domain.exception

sealed class LoginException(message: String) : Exception(message) {
    class UserNotFoundException : LoginException("No user found matching the entered information")
    class InvalidCredentialsException : LoginException("The information you entered is incorrect")
    class UserNotActiveException : LoginException("User login is required")
    class FirstLoginIsNotCompleted : LoginException("The user has not logged in before")
    class UnknownException : LoginException("Unknown error")
}
