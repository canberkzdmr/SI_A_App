package com.cbo.user.domain.usecase

import com.cbo.user.domain.repository.UserRepository
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val verifyCurrentPasswordUseCase: VerifyCurrentPasswordUseCase
) {
    suspend operator fun invoke(
        userId: Int,
        currentPassword: String,
        newPassword: String
    ): Result<Unit> {
        // First verify the current password
        return verifyCurrentPasswordUseCase(userId, currentPassword)
            .fold(
                onSuccess = { isValid ->
                    if (!isValid) {
                        Result.failure(Exception("Current password is incorrect"))
                    } else {
                        // Validate new password
                        if (!isValidPassword(newPassword)) {
                            Result.failure(Exception("New password does not meet requirements"))
                        } else {
                            // Generate new salt and hash
                            val newSalt = generateSalt()
                            val newPasswordHash = hashPassword(newPassword, newSalt)
                            
                            // Update password in repository
                            userRepository.updateUserPassword(userId, newPasswordHash, newSalt)
                        }
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
    }
    
    private fun isValidPassword(password: String): Boolean {
        val lengthValid = password.length >= 8
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        
        return lengthValid && hasUpperCase && hasLowerCase && hasDigit
    }
    
    private fun generateSalt(): ByteArray {
        return ByteArray(16).apply {
            SecureRandom().nextBytes(this)
        }
    }
    
    private fun hashPassword(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, 10000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded
    }
}
