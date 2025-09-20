package com.cbo.core.domain.repository

import com.cbo.core.domain.model.PasswordVerifyModel

interface UserRepository {
    suspend fun getUserPasswordHashByUsername(username: String): Result<PasswordVerifyModel>?
}
