package com.example.core.domain.exception

sealed class RegistrationException(message: String) : Exception(message) {
    class UserAlreadyExistsException : RegistrationException("Kullanici adi daha once alinmis")
    class WeakPasswordException :
        RegistrationException("Parola asagidaki kriterlerden en az 3'unu karsilamali\n\t-Buyuk Harf\n\t-Kucuk Harf\n\t-Rakam\n\t-Ozel karakter")

    class InvalidEmailException : RegistrationException("Gecersiz bir email adresi girdiniz")
    class DatabaseException : RegistrationException("Bilinmeyen Hata")
    class InvalidUserInputException() : RegistrationException("Kullanici bilgileri hatali")
    class TermsAndConditionsCheckerException() :
        RegistrationException("Kayit olmak icin kullanim kosullarini kabul etmelisiniz")

    class UsernameAlreadyExistsException() : RegistrationException("Kullanici adi daha once alinmis")

    class EmailAlreadyExistsException : RegistrationException("Email adresi daha once kullanilmis")
}
