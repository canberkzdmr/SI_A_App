package com.example.login.domain.usecase

import android.util.Log
import com.example.core.domain.model.RegistrationException
import com.example.login.domain.model.User
import com.example.login.domain.repository.UserRepository

class RegisterUserUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(user: User): Result<Unit> {
        if (user.username.isBlank()) {
            return Result.failure(RegistrationException.InvalidUserInputException("Kullanici adi bos birakilamaz"))
        }

        if (!user.termsAndConditionsChecked) {
            Log.i("RegisterUserUseCase", "Terms and Conditions not checked")
            return Result.failure(RegistrationException.TermsAndConditionsCheckerException())
        }

        if (checkUsernameExists(userRepository, user.username)) {
            Log.e("RegisterUseCase",
                RegistrationException.UsernameAlreadyExistsException().message
                    ?: "Kullanici adi daha once alinmis!"
            )
            return Result.failure(RegistrationException.UsernameAlreadyExistsException())
        }

        if (isEmailRegistered(userRepository, user.email)) {
            Log.e("RegisterUseCase",
                RegistrationException.EmailAlreadyExistsException().message
                    ?: "Bu email adresi sistemde mevcut!"
            )
            return Result.failure(RegistrationException.EmailAlreadyExistsException())
        }

        return userRepository.registerUser(user)
    }

    private suspend fun checkUsernameExists(userRepository: UserRepository, username: String): Boolean {
        return userRepository.isUsernameExists(username)
    }

    private suspend fun isEmailRegistered(userRepository: UserRepository, email: String): Boolean {
        return userRepository.isEmailRegistered(email)
    }
}