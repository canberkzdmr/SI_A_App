package com.cbo.user.domain.repository

import com.cbo.core.database.entity.UserDetailEntity
import com.cbo.core.database.entity.UserEntity
import com.cbo.core.database.entity.UserWithDetail

interface UserRepository {
    suspend fun getUserWithDetail(userId: Int): Result<UserWithDetail>
    suspend fun updateUser(user: UserEntity): Result<Unit>
    suspend fun upsertUserDetail(detail: UserDetailEntity): Result<Unit>
}