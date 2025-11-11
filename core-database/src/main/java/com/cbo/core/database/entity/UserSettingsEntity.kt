package com.cbo.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.cbo.core.domain.model.ViewMode

@Entity (
    tableName = "user_settings",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SupportedLanguageEntity::class,
            parentColumns = ["code"],
            childColumns = ["preferredLanguage"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserSettingsEntity(
    @PrimaryKey val userId: Int,
    val isFirstLoginDone: Boolean = false,
    val isBiometricsEnabled: Boolean = false,
    val notesViewMode: ViewMode = ViewMode.LIST,
    val preferredLanguage: String = "en" // Set english as default
)
