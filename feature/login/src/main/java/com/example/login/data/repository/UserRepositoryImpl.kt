package com.example.login.data.repository

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.example.core.data.dao.UserDao
import com.example.core.data.model.UserEntity
import com.example.core.domain.model.LoginException
import com.example.core.domain.model.User
import com.example.login.domain.repository.UserRepository
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject

class UserRepositoryImpl
@Inject
constructor(
    private val userDao: UserDao,
) : UserRepository {
    override suspend fun registerUser(user: User): Result<Unit> {
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

    override suspend fun getUser(username: String): Result<User>? {
        return try {
            val user = userDao.getUserByUsername(username)?.let {
                User(
                    id = it.id,
                    username = it.username,
                    password = it.passwordHash.toString(),
                    email = it.email,
                    lastPasswordChangeDate = it.lastPasswordChangeDate,
                    registerDate = it.registrationDate,
                    termsAndConditionsChecked = true
                )
            }

            user?.let {
                return Result.success(
                    it
                )
            }
            Result.failure(LoginException.UserNotFoundException())
        } catch (e: Exception) {
            Log.e("UserRepositoryImpl", "An error occurred -> ${e.message}")
            return null
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
