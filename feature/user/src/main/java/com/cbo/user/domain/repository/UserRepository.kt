package com.cbo.user.domain.repository

import com.cbo.core.database.entity.UserDetailEntity
import com.cbo.core.database.entity.UserEntity
import com.cbo.core.database.entity.UserWithDetail

interface UserRepository {
    suspend fun getUserWithDetail(userId: Int): Result<UserWithDetail>
    suspend fun updateUser(user: UserEntity): Result<Unit>
    suspend fun upsertUserDetail(detail: UserDetailEntity): Result<Unit>
    suspend fun updateUserPassword(userId: Int, newPasswordHash: ByteArray, newSalt: ByteArray): Result<Unit>
    suspend fun verifyUserPassword(userId: Int, password: String): Result<Boolean>
}