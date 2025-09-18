package com.example.login.domain.usecase

import com.example.core.database.dao.UserDao
import com.example.core.data.mapper.UserEntityMapper
import com.example.core.domain.model.User
import com.example.core.domain.usecase.VerifyPasswordUseCase
import com.example.core.session.domain.repository.SessionRepository
import javax.inject.Inject

class LoginUseCase
    @Inject
    constructor(
        private val userDao: UserDao,
        private val verifyPasswordUseCase: VerifyPasswordUseCase,
        private val sessionRepository: SessionRepository,
        private val userEntityMapper: UserEntityMapper,
    ) {
        suspend operator fun invoke(
            username: String,
            password: String,
        ): Result<User> {
            val userEntity =
                userDao.getUserByUsername(username)
                    ?: return Result.failure(Exception("User not found"))

            val isValid = verifyPasswordUseCase(username, password)

            return if (isValid) {
                val user = userEntityMapper.toDomain(userEntity)
                sessionRepository.setActiveUser(userEntity) // mark as active
                return Result.success(user)
            } else {
                Result.failure(Exception("Invalid password"))
            }
        }
    }
