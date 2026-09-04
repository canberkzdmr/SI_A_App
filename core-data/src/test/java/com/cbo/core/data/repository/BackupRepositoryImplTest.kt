package com.cbo.core.data.repository

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
import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class BackupRepositoryImplTest {

    private val appDatabase: AppDatabase = mock()
    private val noteDao: NoteDao = mock()
    private val categoryDao: CategoryDao = mock()
    private val tagDao: TagDao = mock()
    private val noteLinkDao: NoteLinkDao = mock()
    private val noteTemplateDao: NoteTemplateDao = mock()

    private lateinit var repository: BackupRepositoryImpl
    private val gson = Gson()

    @Before
    fun setup() {
        repository = BackupRepositoryImpl(
            appDatabase = appDatabase,
            noteDao = noteDao,
            categoryDao = categoryDao,
            tagDao = tagDao,
            noteLinkDao = noteLinkDao,
            noteTemplateDao = noteTemplateDao
        )
    }

    @Test
    fun exportBackup_serializesUserDataCorrectly() = runTest {
        val userId = 1

        val categories = listOf(
            CategoryEntity(id = 10, userId = userId, name = "İş", color = "#FF0000", sortOrder = 0)
        )
        val tags = listOf(
            TagEntity(id = 20, userId = userId, name = "Önemli", color = "#00FF00")
        )
        val notes = listOf(
            NoteEntity(
                id = 100,
                userId = userId,
                title = "Proje Notu",
                content = "Detaylar burada",
                categoryId = 10,
                todos = listOf(TodoItemEntity(id = "1", text = "Görev 1", isDone = true))
            )
        )
        val tagMappings = listOf(
            NoteTagCrossRef(noteId = 100, tagId = 20)
        )
        val links = listOf(
            NoteLinkEntity(id = 1, sourceNoteId = 100, targetNoteId = 100)
        )
        val templates = listOf(
            NoteTemplateEntity(id = 1, userId = userId, name = "Toplantı Şablonu", content = "Gündem:")
        )

        whenever(categoryDao.getAllCategoriesForUser(userId)).thenReturn(categories)
        whenever(tagDao.getAllTagsForUser(userId)).thenReturn(tags)
        whenever(noteDao.getAllNotesForBackup(userId)).thenReturn(notes)
        whenever(noteDao.getAllNoteTagCrossRefsForBackup(userId)).thenReturn(tagMappings)
        whenever(noteLinkDao.getAllLinksForUser(userId)).thenReturn(links)
        whenever(noteTemplateDao.getAllTemplatesForUser(userId)).thenReturn(templates)

        val jsonOutput = repository.exportBackup(userId)
        assertNotNull(jsonOutput)
        assertTrue(jsonOutput.isNotBlank())

        val parsed = gson.fromJson(jsonOutput, BackupData::class.java)
        assertEquals(1, parsed.categories.size)
        assertEquals("İş", parsed.categories[0].name)
        assertEquals(1, parsed.tags.size)
        assertEquals("Önemli", parsed.tags[0].name)
        assertEquals(1, parsed.notes.size)
        assertEquals("Proje Notu", parsed.notes[0].title)
        assertEquals(1, parsed.notes[0].todos.size)
        assertEquals("Görev 1", parsed.notes[0].todos[0].text)
        assertEquals(1, parsed.tagMappings.size)
        assertEquals(100, parsed.tagMappings[0].oldNoteId)
        assertEquals(20, parsed.tagMappings[0].oldTagId)
        assertEquals(1, parsed.templates.size)
        assertEquals("Toplantı Şablonu", parsed.templates[0].name)
    }

    @Test
    fun backupData_serializationAndDeserialization_preservesAllFields() {
        val sampleJson = """
            {
              "version": 1,
              "exportedAt": 1700000000000,
              "appVersion": "1.0",
              "categories": [
                { "oldId": 1, "name": "Kişisel", "color": "#123456", "sortOrder": 1 }
              ],
              "tags": [
                { "oldId": 2, "name": "Acil", "color": "#654321" }
              ],
              "notes": [
                {
                  "oldId": 10,
                  "oldCategoryId": 1,
                  "title": "Alışveriş",
                  "content": "Süt, Ekmek",
                  "todos": [
                    { "id": "t1", "text": "Süt al", "isDone": false }
                  ]
                }
              ],
              "tagMappings": [
                { "oldNoteId": 10, "oldTagId": 2 }
              ],
              "noteLinks": [],
              "templates": []
            }
        """.trimIndent()

        val backupData = gson.fromJson(sampleJson, BackupData::class.java)
        assertEquals(1, backupData.categories.size)
        assertEquals("Kişisel", backupData.categories[0].name)
        assertEquals(1, backupData.tags.size)
        assertEquals("Acil", backupData.tags[0].name)
        assertEquals(1, backupData.notes.size)
        assertEquals("Alışveriş", backupData.notes[0].title)
        assertEquals(1, backupData.notes[0].todos.size)
        assertEquals("Süt al", backupData.notes[0].todos[0].text)
    }
}
