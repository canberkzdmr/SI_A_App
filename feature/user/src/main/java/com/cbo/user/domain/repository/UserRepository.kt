package com.cbo.user.domain.repository

import com.cbo.core.database.entity.UserEntity
import com.cbo.core.domain.model.UserDetail
import com.cbo.core.domain.model.UserWithDetail
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserWithDetail(userId: Int): Flow<UserWithDetail?>
    suspend fun updateUser(user: UserEntity): Result<Unit>
    suspend fun upsertUserDetail(detail: UserDetail): Result<Unit>
    suspend fun updateUserPassword(userId: Int, newPasswordHash: ByteArray, newSalt: ByteArray): Result<Unit>
    suspend fun verifyUserPassword(userId: Int, password: String): Result<Boolean>
}