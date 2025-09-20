package com.cbo.core.domain.exception

sealed class LoginException(message : String) : Exception(message) {
    class UserNotFoundException : LoginException("Girdiginiz bilgilerle eslesen kullanici bulunamadi")
    class InvalidCredentialsException : LoginException("Girdiginiz bilgiler hatali")
    class UserNotActiveException : LoginException("Kullanici girisi yapilmasi gerekmektedir")
    class DatabaseException : LoginException("Bilinmeyen hata")
}
