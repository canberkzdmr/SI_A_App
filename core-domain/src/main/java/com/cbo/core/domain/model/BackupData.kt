package com.cbo.core.domain.model

data class BackupData(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0",
    val categories: List<CategoryBackupDto> = emptyList(),
    val tags: List<TagBackupDto> = emptyList(),
    val notes: List<NoteBackupDto> = emptyList(),
    val tagMappings: List<NoteTagMappingDto> = emptyList(),
    val noteLinks: List<NoteLinkBackupDto> = emptyList(),
    val templates: List<NoteTemplateBackupDto> = emptyList()
)

data class CategoryBackupDto(
    val oldId: Int,
    val name: String,
    val color: String? = null,
    val description: String? = null,
    val sortOrder: Int = 0
)

data class TagBackupDto(
    val oldId: Int,
    val name: String,
    val color: String? = null
)

data class TodoItemBackupDto(
    val id: String,
    val text: String,
    val isDone: Boolean
)

data class NoteBackupDto(
    val oldId: Int,
    val oldCategoryId: Int? = null,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isFavorite: Boolean = false,
    val reminderTime: Long? = null,
    val reminderRepeat: String? = null,
    val reminderPriority: String? = null,
    val reminderLatitude: Double? = null,
    val reminderLongitude: Double? = null,
    val reminderLocationName: String? = null,
    val reminderRadius: Float? = null,
    val isLocationReminderEnabled: Boolean = false,
    val zettelId: String? = null,
    val attachments: List<String> = emptyList(),
    val color: String? = null,
    val todos: List<TodoItemBackupDto> = emptyList()
)

data class NoteTagMappingDto(
    val oldNoteId: Int,
    val oldTagId: Int
)

data class NoteLinkBackupDto(
    val oldSourceNoteId: Int,
    val oldTargetNoteId: Int
)

data class NoteTemplateBackupDto(
    val name: String,
    val content: String
)

data class RestoreSummary(
    val notesCount: Int,
    val categoriesCount: Int,
    val tagsCount: Int,
    val linksCount: Int,
    val templatesCount: Int
)
