package com.cbo.user.data.repository

import com.cbo.user.domain.repository.UserRepository
import com.cbo.core.database.dao.UserDao
import com.cbo.core.database.dao.UserDetailDao
import com.cbo.core.database.entity.UserDetailEntity
import com.cbo.core.database.entity.UserEntity
import com.cbo.core.database.entity.UserWithDetail
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
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

    override suspend fun updateUserPassword(userId: Int, newPasswordHash: ByteArray, newSalt: ByteArray): Result<Unit> = runCatching {
        val user = userDao.getUserById(userId)
            ?: throw Exception("User not found")
        val currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val updatedUser = user.copy(
            passwordHash = newPasswordHash,
            salt = newSalt,
            lastPasswordChangeDate = currentDate
        )
        userDao.update(updatedUser)
    }

    override suspend fun verifyUserPassword(userId: Int, password: String): Result<Boolean> = runCatching {
        val user = userDao.getUserById(userId)
            ?: throw Exception("User not found")
        val hashedPassword = hashPassword(password, user.salt)
        hashedPassword.contentEquals(user.passwordHash)
    }

    private fun generateSalt(): ByteArray {
        return ByteArray(16).apply {
            SecureRandom().nextBytes(this)
        }
    }

    private fun hashPassword(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, 10000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded
    }
}
