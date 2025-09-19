package com.example.login.domain.usecase

import android.util.Log
import com.example.core.common.validation.FieldValidation
import com.example.core.domain.exception.RegistrationException

import com.example.login.domain.model.User
import com.example.login.domain.repository.UserRepository

class RegisterUserUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(user: User): Result<Unit> {
        Log.d("RegisterUserUseCase", "Register user in progress")
        if (user.username.isBlank()) {
            return Result.failure(RegistrationException.InvalidUserInputException())
        }

        if (!user.termsAndConditionsChecked) {
            Log.i("RegisterUserUseCase", "Terms and Conditions not checked")
            return Result.failure(RegistrationException.TermsAndConditionsCheckerException())
        }

        if (checkUsernameExists(userRepository, user.username)) {
            Log.i("RegisterUserUseCase",
                RegistrationException.UsernameAlreadyExistsException().message
                    ?: "Kullanici adi daha once alinmis!"
            )
            return Result.failure(RegistrationException.UsernameAlreadyExistsException())
        }

        if (isEmailRegistered(userRepository, user.email)) {
            Log.e("RegisterUserUseCase",
                RegistrationException.EmailAlreadyExistsException().message
                    ?: "Bu email adresi sistemde mevcut!"
            )
            return Result.failure(RegistrationException.EmailAlreadyExistsException())
        }

        if (!isValidPassword(user.password)) {
            return Result.failure(RegistrationException.WeakPasswordException())
        }

        return userRepository.registerUser(user)
    }

    private suspend fun checkUsernameExists(userRepository: UserRepository, username: String): Boolean {
        return userRepository.isUsernameExists(username)
    }

    private suspend fun isEmailRegistered(userRepository: UserRepository, email: String): Boolean {
        return userRepository.isEmailRegistered(email)
    }

    private fun isValidPassword(password: String): Boolean {
        val lengthValid = password.length >= 8
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }

        return lengthValid && hasUpperCase && hasLowerCase && hasDigit
    }
}