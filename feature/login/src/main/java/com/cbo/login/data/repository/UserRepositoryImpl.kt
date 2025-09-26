package com.cbo.login.data.repository

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.cbo.core.database.dao.UserDao
import com.cbo.core.database.entity.UserEntity
import com.cbo.core.domain.exception.LoginException
import com.cbo.core.data.mapper.UserEntityMapper
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
    private val userEntityMapper: UserEntityMapper
) : UserRepository {
    override suspend fun registerUser(user: RegisterUserModel): Result<Unit> {
        return try {
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
            userDao.insert(entity)
            Result.success(Unit)
        } catch (e: SQLiteConstraintException) {
            Log.e("UserRepositoryImpl", "Violated Constraints -> ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("UserRepositoryImpl", "An error ocurred -> ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getUser(username: String): Result<RegisterUserModel> {
        return try {
            val userEntity = userDao.getUserByUsername(username)

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
        } catch (e: Exception) {
            Log.e("UserRepositoryImpl", "An error occurred -> ${e.message}")
            Result.failure(LoginException.UserNotFoundException())
        }
    }

    override suspend fun isUsernameExists(username: String): Boolean {
        return try {
            userDao.userExists(username)
        } catch (e: Exception) {
            Log.e("UserRepositoryImpl", "An error ocurred -> ${e.message}")
            false
        }
    }

    override suspend fun isEmailRegistered(email: String): Boolean {
        return try {
            return userDao.emailExists(email)
        } catch (e: Exception) {
            Log.e("UserRepositoryImpl", "An error ocurred -> ${e.message}")
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
