package com.example.core.data.repository

import android.util.Log
import com.example.core.data.dao.UserDao
import com.example.core.domain.model.LoginException
import com.example.core.domain.model.PasswordVerifyModel
import com.example.core.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl
@Inject
constructor(
    private val userDao: UserDao
): UserRepository {

    override suspend fun getUserPasswordHashByUsername(username: String): Result<PasswordVerifyModel>? {
        return try {
            userDao.getPasswordHashAndSaltByUsername(username)?.let {
                return Result.success(it)
            }

            Result.failure(LoginException.UserNotFoundException())
        } catch (e: Exception) {
            Log.e("UserRepositoryImpl", "An error occurred -> ${e.message}")
            return null
        }
    }
}