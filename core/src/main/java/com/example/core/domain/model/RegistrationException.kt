package com.example.core.domain.model

sealed class RegistrationException(message: String): Exception(message) {
    class UsernameAlreadyExistsException: RegistrationException("Gecerli bir kullanici adi girin")
    class EmailAlreadyExistsException: RegistrationException("Gecerli bir e-posta adresi girin")
    class TermsAndConditionsCheckerException: RegistrationException("Kullanici kosullarinin kabul edilmesi zorunludur")
    class InvalidUserInputException(reason: String): RegistrationException(reason)
}