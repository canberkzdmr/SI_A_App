package com.example.core.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.core.data.model.UserEntity
import com.example.core.domain.model.PasswordVerifyModel

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
}