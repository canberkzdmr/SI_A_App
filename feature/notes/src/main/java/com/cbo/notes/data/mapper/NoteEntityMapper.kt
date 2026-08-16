package com.cbo.notes.data.mapper

import com.cbo.core.database.entity.NoteEntity
import com.cbo.core.database.entity.NoteWithDetails
import com.cbo.core.database.entity.TodoItemEntity
import com.cbo.notes.domain.model.Note
import com.cbo.notes.domain.model.TodoItem
import com.cbo.notes.domain.model.ReminderRepeat
import com.cbo.notes.domain.model.ReminderPriority
import javax.inject.Inject

class NoteEntityMapper @Inject constructor(
    private val categoryEntityMapper: CategoryEntityMapper,
    private val tagEntityMapper: TagEntityMapper
) {
    
    fun toDomain(entity: NoteEntity): Note {
        return Note(
            id = entity.id,
            userId = entity.userId,
            title = entity.title,
            content = entity.content,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            isPinned = entity.isPinned,
            isArchived = entity.isArchived,
            isFavorite = entity.isFavorite,
            isDeleted = entity.isDeleted,
            deletedAt = entity.deletedAt,
            reminderTime = entity.reminderTime,
            reminderRepeat = entity.reminderRepeat?.let { runCatching { ReminderRepeat.valueOf(it) }.getOrDefault(ReminderRepeat.NONE) } ?: ReminderRepeat.NONE,
            reminderPriority = entity.reminderPriority?.let { runCatching { ReminderPriority.valueOf(it) }.getOrDefault(ReminderPriority.DEFAULT) } ?: ReminderPriority.DEFAULT,
            reminderLatitude = entity.reminderLatitude,
            reminderLongitude = entity.reminderLongitude,
            reminderLocationName = entity.reminderLocationName,
            reminderRadius = entity.reminderRadius,
            isLocationReminderEnabled = entity.isLocationReminderEnabled,
            zettelId = entity.zettelId,
            attachments = entity.attachments,
            color = entity.color,
            todos = entity.todos.map { TodoItem(it.id, it.text, it.isDone) }
        )
    }
    
    fun toDomain(noteWithDetails: NoteWithDetails): Note {
        return Note(
            id = noteWithDetails.note.id,
            userId = noteWithDetails.note.userId,
            title = noteWithDetails.note.title,
            content = noteWithDetails.note.content,
            category = noteWithDetails.category?.let { categoryEntityMapper.toDomain(it) },
            tags = noteWithDetails.tags.map { tagEntityMapper.toDomain(it) },
            createdAt = noteWithDetails.note.createdAt,
            updatedAt = noteWithDetails.note.updatedAt,
            isPinned = noteWithDetails.note.isPinned,
            isArchived = noteWithDetails.note.isArchived,
            isFavorite = noteWithDetails.note.isFavorite,
            isDeleted = noteWithDetails.note.isDeleted,
            deletedAt = noteWithDetails.note.deletedAt,
            reminderTime = noteWithDetails.note.reminderTime,
            reminderRepeat = noteWithDetails.note.reminderRepeat?.let { runCatching { ReminderRepeat.valueOf(it) }.getOrDefault(ReminderRepeat.NONE) } ?: ReminderRepeat.NONE,
            reminderPriority = noteWithDetails.note.reminderPriority?.let { runCatching { ReminderPriority.valueOf(it) }.getOrDefault(ReminderPriority.DEFAULT) } ?: ReminderPriority.DEFAULT,
            reminderLatitude = noteWithDetails.note.reminderLatitude,
            reminderLongitude = noteWithDetails.note.reminderLongitude,
            reminderLocationName = noteWithDetails.note.reminderLocationName,
            reminderRadius = noteWithDetails.note.reminderRadius,
            isLocationReminderEnabled = noteWithDetails.note.isLocationReminderEnabled,
            zettelId = noteWithDetails.note.zettelId,
            attachments = noteWithDetails.note.attachments,
            color = noteWithDetails.note.color,
            todos = noteWithDetails.note.todos.map { TodoItem(it.id, it.text, it.isDone) }
        )
    }
    
    fun toEntity(domain: Note): NoteEntity {
        return NoteEntity(
            id = domain.id,
            userId = domain.userId,
            title = domain.title,
            content = domain.content,
            categoryId = domain.category?.id,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            isPinned = domain.isPinned,
            isArchived = domain.isArchived,
            isFavorite = domain.isFavorite,
            isDeleted = domain.isDeleted,
            deletedAt = domain.deletedAt,
            reminderTime = domain.reminderTime,
            reminderRepeat = domain.reminderRepeat.name,
            reminderPriority = domain.reminderPriority.name,
            reminderLatitude = domain.reminderLatitude,
            reminderLongitude = domain.reminderLongitude,
            reminderLocationName = domain.reminderLocationName,
            reminderRadius = domain.reminderRadius,
            isLocationReminderEnabled = domain.isLocationReminderEnabled,
            zettelId = domain.zettelId,
            attachments = domain.attachments,
            color = domain.color,
            todos = domain.todos.map { TodoItemEntity(it.id, it.text, it.isDone) }
        )
    }
}
