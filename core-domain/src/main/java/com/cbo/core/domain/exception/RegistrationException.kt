package com.cbo.core.domain.exception

sealed class RegistrationException(
    message: String,
) : Exception(message) {
    class UserAlreadyExistsException : RegistrationException("Username has already been taken")

    class WeakPasswordException :
        RegistrationException(
            "Password must meet at least 3 of the following criteria:\n\t- Uppercase letter\n\t- Lowercase letter\n\t- Digit\n\t- Special character",
        )

    class PasswordMismatchException : RegistrationException("Password and confirm password do not match")

    class InvalidEmailException : RegistrationException("You have entered an invalid email address")

    class EmptyEmailException : RegistrationException("Email address cannot be empty.")

    class DatabaseException : RegistrationException("Unknown error")

    class InvalidUserInputException : RegistrationException("User information is invalid")

    class TermsAndConditionsCheckerException : RegistrationException("You must accept the terms and conditions to register")

    class UsernameAlreadyExistsException : RegistrationException("Username has already been taken")

    class EmailAlreadyExistsException : RegistrationException("Email address has already been used")
}
