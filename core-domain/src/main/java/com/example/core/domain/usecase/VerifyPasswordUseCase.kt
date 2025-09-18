package com.example.core.domain.usecase

import com.example.core.domain.model.PasswordVerifyModel
import com.example.core.domain.repository.UserRepository
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject

class VerifyPasswordUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        username: String,
        enteredPassword: String
    ): Boolean {
        if (enteredPassword.isEmpty() || enteredPassword.isBlank()) {
            return false
        }

        val result = userRepository.getUserPasswordHashByUsername(username)
        result?.let { result ->
            if (result.isSuccess) {
                val passwordVerifyModel = result.getOrNull()
                passwordVerifyModel?.let { passwordVerifyModel ->
                    val spec = PBEKeySpec(enteredPassword.toCharArray(), passwordVerifyModel.salt, 10000, 256)
                    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    val enteredHash = factory.generateSecret(spec).encoded
                    return passwordVerifyModel.passwordHash.contentEquals(enteredHash)
                }
            } else {
                return false
            }
        }
        return false
    }
}
