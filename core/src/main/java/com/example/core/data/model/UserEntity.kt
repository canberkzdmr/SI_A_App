package com.example.core.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username", "email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val passwordHash: ByteArray,
    val salt: ByteArray,
    val email: String,
    val registrationDate: String,
    val lastPasswordChangeDate: String,
)