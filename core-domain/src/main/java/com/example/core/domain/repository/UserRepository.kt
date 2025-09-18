package com.example.core.domain.repository

import com.example.core.domain.model.PasswordVerifyModel

interface UserRepository {
    suspend fun getUserPasswordHashByUsername(username: String): Result<PasswordVerifyModel>?
}
