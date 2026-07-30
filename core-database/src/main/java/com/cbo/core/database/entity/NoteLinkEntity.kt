package com.cbo.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "note_links",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["sourceNoteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["targetNoteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sourceNoteId"]),
        Index(value = ["targetNoteId"])
    ]
)
data class NoteLinkEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sourceNoteId: Int,
    val targetNoteId: Int,
    val createdAt: Long = System.currentTimeMillis()
)
