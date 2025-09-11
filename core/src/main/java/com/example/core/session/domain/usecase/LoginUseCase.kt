package com.example.core.session.domain.usecase

import com.example.core.data.dao.UserDao
import com.example.core.data.mapper.toDomain
import com.example.core.domain.model.User
import com.example.core.domain.usecase.VerifyPasswordUseCase
import com.example.core.session.domain.model.Session
import com.example.core.session.domain.repository.SessionRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val userDao: UserDao,
    private val verifyPasswordUseCase: VerifyPasswordUseCase,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(username: String, password: String): Result<User> {
        val userEntity = userDao.getUserByUsername(username)
            ?: return Result.failure(Exception("User not found"))

        val isValid = verifyPasswordUseCase(username, password)

        return if (isValid) {
            val user = userEntity.toDomain()
            sessionRepository.saveSession(Session(user.id, user.username, user.email))
            Result.success(user)
        } else {
            Result.failure(Exception("Invalid password"))
        }
    }
}