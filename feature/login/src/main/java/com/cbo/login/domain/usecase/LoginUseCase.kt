package com.cbo.login.domain.usecase

import com.cbo.core.database.dao.UserDao
import com.cbo.core.data.mapper.UserEntityMapper
import com.cbo.core.domain.model.User
import com.cbo.core.domain.usecase.VerifyPasswordUseCase
import com.cbo.core.session.domain.repository.SessionRepository
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
