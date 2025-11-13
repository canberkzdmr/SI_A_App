package com.cbo.user.data.repository

import com.cbo.core.data.mapper.UserDetailEntityMapper
import com.cbo.core.data.mapper.UserWithDetailEntityMapper
import com.cbo.core.database.dao.UserDao
import com.cbo.core.database.dao.UserDetailDao
import com.cbo.core.database.entity.UserEntity
import com.cbo.core.domain.model.UserDetail
import com.cbo.core.domain.model.UserWithDetail
import com.cbo.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userDetailDao: UserDetailDao,
    private val userWithDetailMapper: UserWithDetailEntityMapper,
    private val userDetailMapper: UserDetailEntityMapper,
) : UserRepository {

    override fun getUserWithDetail(userId: Int): Flow<UserWithDetail?> {
        return userDao.getUserWithDetailById(userId).map { entity ->
            entity?.let { userWithDetailMapper.toDomain(it) }
        }
    }

    override suspend fun updateUser(user: UserEntity): Result<Unit> = runCatching {
        userDao.update(user)
    }

    override suspend fun upsertUserDetail(detail: UserDetail): Result<Unit> = runCatching {
        val existingDetail = userDetailDao.getUserDetailByUserId(detail.userId)
        val entity = userDetailMapper.toEntity(detail)
        if (existingDetail == null) {
            userDetailDao.insert(entity)
        } else {
            userDetailDao.update(entity)
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
