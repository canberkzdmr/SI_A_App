package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.core.database.entity.UserEntity
import com.example.core.database.entity.UserWithDetail
import com.example.core.domain.model.PasswordVerifyModel
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao: BaseDao<UserEntity> {

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE username = :username)")
    suspend fun userExists(username: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    suspend fun emailExists(email: String): Boolean

    @Query("SELECT passwordHash, salt FROM USERS WHERE username = :username")
    suspend fun getPasswordHashAndSaltByUsername(username: String): PasswordVerifyModel?

    @Query("SELECT passwordHash, salt FROM USERS WHERE email = :email")
    suspend fun getPasswordHashByEmail(email: String): PasswordVerifyModel?

    @Query("SELECT * FROM users WHERE isActive = 1 LIMIT 1")
    fun getActiveUserFlow(): Flow<UserEntity?>

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isActive = 0")
    suspend fun deactivateAllUsers()

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): UserEntity?

    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserWithDetailById(userId: Int): UserWithDetail?

    @Transaction
    @Query("SELECT * FROM users")
    suspend fun getAllUsersWithDetails(): List<UserWithDetail>
}
