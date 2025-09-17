package com.cbo.user.domain.repository

import com.example.core.data.model.UserDetailEntity
import com.example.core.data.model.UserEntity
import com.example.core.data.model.UserWithDetail

interface UserRepository {
    suspend fun getUserWithDetail(userId: Int): Result<UserWithDetail>
    suspend fun updateUser(user: UserEntity): Result<Unit>
    suspend fun upsertUserDetail(detail: UserDetailEntity): Result<Unit>
}