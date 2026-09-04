package com.cbo.core.data.repository

import androidx.room.withTransaction
import com.cbo.core.database.dao.CategoryDao
import com.cbo.core.database.dao.NoteDao
import com.cbo.core.database.dao.NoteLinkDao
import com.cbo.core.database.dao.NoteTemplateDao
import com.cbo.core.database.dao.TagDao
import com.cbo.core.database.database.AppDatabase
import com.cbo.core.database.entity.CategoryEntity
import com.cbo.core.database.entity.NoteEntity
import com.cbo.core.database.entity.NoteLinkEntity
import com.cbo.core.database.entity.NoteTagCrossRef
import com.cbo.core.database.entity.NoteTemplateEntity
import com.cbo.core.database.entity.TagEntity
import com.cbo.core.database.entity.TodoItemEntity
import com.cbo.core.domain.model.BackupData
import com.cbo.core.domain.model.CategoryBackupDto
import com.cbo.core.domain.model.NoteBackupDto
import com.cbo.core.domain.model.NoteLinkBackupDto
import com.cbo.core.domain.model.NoteTagMappingDto
import com.cbo.core.domain.model.NoteTemplateBackupDto
import com.cbo.core.domain.model.RestoreSummary
import com.cbo.core.domain.model.TagBackupDto
import com.cbo.core.domain.model.TodoItemBackupDto
import com.cbo.core.domain.repository.BackupRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepositoryImpl @Inject constructor(
    private val appDatabase: AppDatabase,
    private val noteDao: NoteDao,
    private val categoryDao: CategoryDao,
    private val tagDao: TagDao,
    private val noteLinkDao: NoteLinkDao,
    private val noteTemplateDao: NoteTemplateDao,
) : BackupRepository {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    override suspend fun exportBackup(userId: Int): String = withContext(Dispatchers.IO) {
        val categories = categoryDao.getAllCategoriesForUser(userId).map {
            CategoryBackupDto(
                oldId = it.id,
                name = it.name,
                color = it.color,
                description = it.description,
                sortOrder = it.sortOrder
            )
        }

        val tags = tagDao.getAllTagsForUser(userId).map {
            TagBackupDto(
                oldId = it.id,
                name = it.name,
                color = it.color
            )
        }

        val notes = noteDao.getAllNotesForBackup(userId).map { note ->
            NoteBackupDto(
                oldId = note.id,
                oldCategoryId = note.categoryId,
                title = note.title,
                content = note.content,
                createdAt = note.createdAt,
                updatedAt = note.updatedAt,
                isPinned = note.isPinned,
                isArchived = note.isArchived,
                isFavorite = note.isFavorite,
                reminderTime = note.reminderTime,
                reminderRepeat = note.reminderRepeat,
                reminderPriority = note.reminderPriority,
                reminderLatitude = note.reminderLatitude,
                reminderLongitude = note.reminderLongitude,
                reminderLocationName = note.reminderLocationName,
                reminderRadius = note.reminderRadius,
                isLocationReminderEnabled = note.isLocationReminderEnabled,
                zettelId = note.zettelId,
                attachments = note.attachments,
                color = note.color,
                todos = note.todos.map { todo ->
                    TodoItemBackupDto(
                        id = todo.id,
                        text = todo.text,
                        isDone = todo.isDone
                    )
                }
            )
        }

        val tagMappings = noteDao.getAllNoteTagCrossRefsForBackup(userId).map {
            NoteTagMappingDto(
                oldNoteId = it.noteId,
                oldTagId = it.tagId
            )
        }

        val noteLinks = noteLinkDao.getAllLinksForUser(userId).map {
            NoteLinkBackupDto(
                oldSourceNoteId = it.sourceNoteId,
                oldTargetNoteId = it.targetNoteId
            )
        }

        val templates = noteTemplateDao.getAllTemplatesForUser(userId).map {
            NoteTemplateBackupDto(
                name = it.name,
                content = it.content
            )
        }

        val backupData = BackupData(
            version = 1,
            exportedAt = System.currentTimeMillis(),
            categories = categories,
            tags = tags,
            notes = notes,
            tagMappings = tagMappings,
            noteLinks = noteLinks,
            templates = templates
        )

        gson.toJson(backupData)
    }

    override suspend fun restoreBackup(userId: Int, jsonContent: String): RestoreSummary = withContext(Dispatchers.IO) {
        val backupData = gson.fromJson(jsonContent, BackupData::class.java)
            ?: throw IllegalArgumentException("Geçersiz yedek dosyası biçimi")

        appDatabase.withTransaction {
            // 1. Categories
            val categoryIdMap = mutableMapOf<Int, Int>()
            val existingCategories = categoryDao.getAllCategoriesForUser(userId).associateBy { it.name }
            var importedCategoriesCount = 0

            for (catDto in backupData.categories) {
                val existing = existingCategories[catDto.name]
                if (existing != null) {
                    categoryIdMap[catDto.oldId] = existing.id
                } else {
                    val newEntity = CategoryEntity(
                        id = 0,
                        userId = userId,
                        name = catDto.name,
                        color = catDto.color,
                        description = catDto.description,
                        sortOrder = catDto.sortOrder
                    )
                    val newId = categoryDao.insert(newEntity).toInt()
                    categoryIdMap[catDto.oldId] = newId
                    importedCategoriesCount++
                }
            }

            // 2. Tags
            val tagIdMap = mutableMapOf<Int, Int>()
            val existingTags = tagDao.getAllTagsForUser(userId).associateBy { it.name }
            var importedTagsCount = 0

            for (tagDto in backupData.tags) {
                val existing = existingTags[tagDto.name]
                if (existing != null) {
                    tagIdMap[tagDto.oldId] = existing.id
                } else {
                    val newEntity = TagEntity(
                        id = 0,
                        userId = userId,
                        name = tagDto.name,
                        color = tagDto.color
                    )
                    val newId = tagDao.insert(newEntity).toInt()
                    tagIdMap[tagDto.oldId] = newId
                    importedTagsCount++
                }
            }

            // 3. Notes
            val noteIdMap = mutableMapOf<Int, Int>()
            var importedNotesCount = 0

            for (noteDto in backupData.notes) {
                val mappedCategoryId = noteDto.oldCategoryId?.let { categoryIdMap[it] }
                val noteEntity = NoteEntity(
                    id = 0,
                    userId = userId,
                    title = noteDto.title,
                    content = noteDto.content,
                    categoryId = mappedCategoryId,
                    createdAt = noteDto.createdAt,
                    updatedAt = noteDto.updatedAt,
                    isPinned = noteDto.isPinned,
                    isArchived = noteDto.isArchived,
                    isFavorite = noteDto.isFavorite,
                    isDeleted = false,
                    deletedAt = null,
                    reminderTime = noteDto.reminderTime,
                    reminderRepeat = noteDto.reminderRepeat,
                    reminderPriority = noteDto.reminderPriority,
                    reminderLatitude = noteDto.reminderLatitude,
                    reminderLongitude = noteDto.reminderLongitude,
                    reminderLocationName = noteDto.reminderLocationName,
                    reminderRadius = noteDto.reminderRadius,
                    isLocationReminderEnabled = noteDto.isLocationReminderEnabled,
                    zettelId = noteDto.zettelId,
                    attachments = noteDto.attachments,
                    color = noteDto.color,
                    todos = noteDto.todos.map { todo ->
                        TodoItemEntity(
                            id = todo.id,
                            text = todo.text,
                            isDone = todo.isDone
                        )
                    }
                )
                val newNoteId = noteDao.insert(noteEntity).toInt()
                noteIdMap[noteDto.oldId] = newNoteId
                importedNotesCount++
            }

            // 4. Note - Tag Cross References
            for (mapping in backupData.tagMappings) {
                val newNoteId = noteIdMap[mapping.oldNoteId]
                val newTagId = tagIdMap[mapping.oldTagId]
                if (newNoteId != null && newTagId != null) {
                    noteDao.insertNoteTagCrossRef(NoteTagCrossRef(noteId = newNoteId, tagId = newTagId))
                }
            }

            // 5. Note Links
            var importedLinksCount = 0
            for (link in backupData.noteLinks) {
                val newSourceId = noteIdMap[link.oldSourceNoteId]
                val newTargetId = noteIdMap[link.oldTargetNoteId]
                if (newSourceId != null && newTargetId != null) {
                    noteLinkDao.insert(NoteLinkEntity(sourceNoteId = newSourceId, targetNoteId = newTargetId))
                    importedLinksCount++
                }
            }

            // 6. Templates
            var importedTemplatesCount = 0
            val existingTemplates = noteTemplateDao.getAllTemplatesForUser(userId).map { it.name }.toSet()
            for (tpl in backupData.templates) {
                if (tpl.name !in existingTemplates) {
                    noteTemplateDao.insert(
                        NoteTemplateEntity(
                            id = 0,
                            userId = userId,
                            name = tpl.name,
                            content = tpl.content
                        )
                    )
                    importedTemplatesCount++
                }
            }

            RestoreSummary(
                notesCount = importedNotesCount,
                categoriesCount = importedCategoriesCount,
                tagsCount = importedTagsCount,
                linksCount = importedLinksCount,
                templatesCount = importedTemplatesCount
            )
        }
    }
}
