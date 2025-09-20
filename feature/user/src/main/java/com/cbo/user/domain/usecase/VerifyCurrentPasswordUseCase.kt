package com.cbo.user.domain.usecase

import com.cbo.user.domain.repository.UserRepository
import javax.inject.Inject

class VerifyCurrentPasswordUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: Int, currentPassword: String): Result<Boolean> {
        return userRepository.verifyUserPassword(userId, currentPassword)
    }
}
