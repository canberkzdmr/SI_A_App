package com.cbo.login.data.repository

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Transaction
import androidx.room.withTransaction
import com.cbo.core.database.dao.UserDao
import com.cbo.core.database.entity.UserEntity
import com.cbo.core.domain.exception.LoginException
import com.cbo.core.data.mapper.UserEntityMapper
import com.cbo.core.database.dao.UserSettingsDao
import com.cbo.core.database.database.AppDatabase
import com.cbo.core.database.entity.UserSettingsEntity
import com.cbo.core.logger.AppLogger
import com.cbo.login.domain.model.RegisterUserModel
import com.cbo.login.domain.repository.UserRepository
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject

class UserRepositoryImpl
@Inject
constructor(
    private val userDao: UserDao,
    private val userSettingsDao: UserSettingsDao,
    private val userEntityMapper: UserEntityMapper,
    private val db: AppDatabase,
) : UserRepository {

    @Transaction
    override suspend fun registerUser(user: RegisterUserModel): Result<Unit> {
        return try {
            db.withTransaction {
                val salt = generateSalt()
                val hash = hashPassword(user.password, salt)
                val entity =
                    UserEntity(
                        username = user.username,
                        passwordHash = hash,
                        salt = salt,
                        email = user.email,
                        lastPasswordChangeDate = user.lastPasswordChangeDate,
                        registrationDate = user.registerDate,
                        isActive = false,
                    )
                val id = userDao.insert(entity).toInt()
                userSettingsDao.insertOrUpdate(UserSettingsEntity(userId = id))
                Result.success(Unit)
            }
        } catch (e: SQLiteConstraintException) {
            AppLogger.e("Violated Constraints -> ${e.message}", throwable = e)
            Result.failure(e)
        } catch (e: Exception) {
            AppLogger.e("An error ocurred -> ${e.message}", throwable = e)
            Result.failure(e)
        }
    }

    override suspend fun getUser(username: String): Result<RegisterUserModel> {
        return try {
            val userEntity = userDao.getUserByUsername(username)

            userEntity?.let {
                val user = RegisterUserModel(
                    id = userEntity.id,
                    username = userEntity.username,
                    password = "", // Don't return password hash
                    retypePassword = "",
                    email = userEntity.email,
                    lastPasswordChangeDate = userEntity.lastPasswordChangeDate,
                    registerDate = userEntity.registrationDate,
                    termsAndConditionsChecked = true
                )
                Result.success(user)
            } ?: run {
                Result.failure(LoginException.UserNotFoundException())
            }
        } catch (e: Exception) {
            AppLogger.e("An error occurred -> ${e.message}", throwable = e)
            Result.failure(LoginException.UserNotFoundException())
        }
    }

    override suspend fun isUsernameExists(username: String): Boolean {
        return try {
            userDao.userExists(username)
        } catch (e: Exception) {
            AppLogger.e("An error ocurred -> ${e.message}", throwable = e)
            false
        }
    }

    override suspend fun isEmailRegistered(email: String): Boolean {
        return try {
            return userDao.emailExists(email)
        } catch (e: Exception) {
            AppLogger.e("An error ocurred -> ${e.message}", throwable = e)
            false
        }
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
