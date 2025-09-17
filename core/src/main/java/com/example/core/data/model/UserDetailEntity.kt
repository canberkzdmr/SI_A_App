package com.example.core.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_details",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"], unique = true)]
)
data class UserDetailEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int, // foreign key to UserEntity
    val fullName: String?,
    val avatarUrl: String?, // can be a URL or base64 string
    val phoneNumber: String?,
    val address: String?,
    val bio: String?,
    val dateOfBirth: String?,
    val gender: String?,
)
