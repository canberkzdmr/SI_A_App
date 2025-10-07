package com.cbo.login.domain.usecase

import com.cbo.core.database.dao.UserDao
import com.cbo.core.data.mapper.UserEntityMapper
import com.cbo.core.database.entity.UserEntity
import com.cbo.core.domain.exception.LoginException
import com.cbo.core.domain.model.User
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
    private val userDao: UserDao,
    private val userEntityMapper: UserEntityMapper
) {
    suspend operator fun invoke(username: String): Result<User> {
        return try {
            val userEntity = userDao.getUserByUsername(username)
            userEntity?.let {
                Result.success(userEntityMapper.toDomain(userEntity))
            } ?: run {
                Result.failure(LoginException.UserNotFoundException())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class GetUserEntityUseCase @Inject constructor(
    private val userDao: UserDao,
) {
    suspend operator fun invoke (userName: String): Result<UserEntity> {
        return try {
            userDao.getUserByUsername(userName)?.let {
                Result.success(it)
            } ?: run {
                Result.failure(Throwable("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}