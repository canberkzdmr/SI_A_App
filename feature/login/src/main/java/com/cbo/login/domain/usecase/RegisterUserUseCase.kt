package com.cbo.login.domain.usecase

import com.cbo.core.domain.exception.RegistrationException
import com.cbo.core.logger.AppLogger
import com.cbo.login.domain.model.RegisterUserModel
import com.cbo.login.domain.repository.UserRepository

class RegisterUserUseCase(private val userRepository: UserRepository) {

    suspend operator fun invoke(user: RegisterUserModel): Result<Unit> {
        AppLogger.d("Register user in progress")

        return when {
            user.username.isBlank() ->
                Result.failure(RegistrationException.InvalidUserInputException())

            user.email.isBlank() ->
                Result.failure(RegistrationException.EmptyEmailException())

            !android.util.Patterns.EMAIL_ADDRESS.matcher(user.email).matches() ->
                Result.failure(RegistrationException.InvalidEmailException())

            user.password != user.retypePassword ->
                Result.failure(RegistrationException.PasswordMismatchException())

            !user.termsAndConditionsChecked ->
                Result.failure(RegistrationException.TermsAndConditionsCheckerException())

            userRepository.isUsernameExists(user.username) ->
                Result.failure(RegistrationException.UsernameAlreadyExistsException())

            userRepository.isEmailRegistered(user.email) ->
                Result.failure(RegistrationException.EmailAlreadyExistsException())

            !isValidPassword(user.password) ->
                Result.failure(RegistrationException.WeakPasswordException())

            else -> userRepository.registerUser(user)
        }
    }

    private fun isValidPassword(password: String): Boolean =
        password.length >= 8 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isLowerCase() } &&
                password.any { it.isDigit() }
}
