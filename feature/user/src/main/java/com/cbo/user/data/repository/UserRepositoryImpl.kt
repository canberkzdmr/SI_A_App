package com.cbo.user.data.repository

import com.cbo.user.domain.repository.UserRepository
import com.cbo.core.database.dao.UserDao
import com.cbo.core.database.dao.UserDetailDao
import com.cbo.core.database.entity.UserDetailEntity
import com.cbo.core.database.entity.UserEntity
import com.cbo.core.database.entity.UserWithDetail
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userDetailDao: UserDetailDao,
) : UserRepository {

    override suspend fun getUserWithDetail(userId: Int): Result<UserWithDetail> = runCatching {
        userDao.getUserWithDetailById(userId)
            ?: throw Exception("User not found")
    }

    override suspend fun updateUser(user: UserEntity): Result<Unit> = runCatching {
        userDao.update(user)
    }

    override suspend fun upsertUserDetail(detail: UserDetailEntity): Result<Unit> = runCatching {
        val existingDetail = userDetailDao.getUserDetailByUserId(detail.userId)
        if (existingDetail == null) {
            userDetailDao.insert(detail)
        } else {
            userDetailDao.update(detail)
        }
    }
}
