package com.cbo.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "supported_languages",
    indices = [Index(value = ["code"], unique = true)],
)
data class SupportedLanguageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val code: String,
    val displayName: String,
    val nativeName: String,
    val isEnabled: Boolean = true,
    val sortOrder: Int = 0,
)
