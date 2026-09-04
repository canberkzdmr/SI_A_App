package com.cbo.core.data.repository

import com.cbo.core.database.dao.UserDao
import com.cbo.core.domain.exception.LoginException
import com.cbo.core.domain.model.PasswordVerifyModel
import com.cbo.core.domain.repository.UserRepository
import com.cbo.core.logger.AppLogger
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    override suspend fun getUserPasswordHashByUsername(username: String): Result<PasswordVerifyModel>? {
        return try {
            userDao.getPasswordHashAndSaltByUsername(username)?.let {
                return Result.success(it)
            }

            Result.failure(LoginException.UserNotFoundException())
        } catch (e: Exception) {
            AppLogger.e("An error occurred -> ${e.message}", throwable = e)
            return null
        }
    }
}
