package com.cbo.user.domain.repository

import com.example.core.database.entity.UserDetailEntity
import com.example.core.database.entity.UserEntity
import com.example.core.database.entity.UserWithDetail

interface UserRepository {
    suspend fun getUserWithDetail(userId: Int): Result<UserWithDetail>
    suspend fun updateUser(user: UserEntity): Result<Unit>
    suspend fun upsertUserDetail(detail: UserDetailEntity): Result<Unit>
}