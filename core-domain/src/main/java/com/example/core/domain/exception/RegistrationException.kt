package com.example.core.domain.exception

sealed class RegistrationException : Exception() {
    class UserAlreadyExistsException : RegistrationException()
    class WeakPasswordException : RegistrationException()
    class InvalidEmailException : RegistrationException()
    class DatabaseException : RegistrationException()
    class InvalidUserInputException(message: String) : RegistrationException()
    class TermsAndConditionsCheckerException : RegistrationException()
    class UsernameAlreadyExistsException : RegistrationException()
    class EmailAlreadyExistsException : RegistrationException()
}
