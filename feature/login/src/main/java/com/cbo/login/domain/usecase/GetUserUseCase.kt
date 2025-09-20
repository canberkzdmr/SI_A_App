package com.cbo.login.domain.usecase

import com.cbo.core.database.dao.UserDao
import com.cbo.core.data.mapper.UserEntityMapper
import com.cbo.core.domain.model.User
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
    private val userDao: UserDao,
    private val userEntityMapper: UserEntityMapper
) {
    suspend operator fun invoke(username: String): Result<User> {
        return try {
            val userEntity = userDao.getUserByUsername(username)
            Result.success(userEntityMapper.toDomain(userEntity))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}