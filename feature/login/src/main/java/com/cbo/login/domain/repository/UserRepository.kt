package com.cbo.login.domain.repository

import com.cbo.login.domain.model.RegisterUserModel

interface UserRepository {
    suspend fun registerUser(user: RegisterUserModel): Result<Unit>
    suspend fun getUser(username: String): Result<RegisterUserModel>?
    suspend fun isUsernameExists(username: String): Boolean
    suspend fun isEmailRegistered(email: String): Boolean
}