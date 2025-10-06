package com.cbo.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity (
    tableName = "user_settings",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserSettings(
    @PrimaryKey val userId: Int,
    val isFirstLoginDone: Boolean = false,
    val isBiometricsEnabled: Boolean = false,
)
