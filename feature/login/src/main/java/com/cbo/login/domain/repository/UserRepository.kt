package com.cbo.login.domain.repository

import com.cbo.login.domain.model.User

interface UserRepository {
    suspend fun registerUser(user: User): Result<Unit>
    suspend fun getUser(username: String): Result<User>?
    suspend fun isUsernameExists(username: String): Boolean
    suspend fun isEmailRegistered(email: String): Boolean
}