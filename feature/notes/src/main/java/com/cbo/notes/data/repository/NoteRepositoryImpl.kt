package com.cbo.notes.data.repository

import android.util.Log
import com.cbo.notes.data.mapper.NoteEntityMapper
import com.cbo.core.database.dao.NoteDao
import com.cbo.core.database.dao.TagDao
import com.cbo.core.database.entity.NoteTagCrossRef
import com.cbo.notes.data.mapper.TagEntityMapper
import com.cbo.notes.domain.model.Note
import com.cbo.notes.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao,
    private val tagDao: TagDao,
    private val noteEntityMapper: NoteEntityMapper,
    private val tagEntityMapper: TagEntityMapper,
) : NoteRepository {

    override fun getNotesByUser(userId: Int): Flow<List<Note>> {
        return noteDao.getNotesWithDetailsByUser(userId).map { entities ->
            entities.map { noteEntityMapper.toDomain(it) }
        }
    }

    override fun getArchivedNotesByUser(userId: Int): Flow<List<Note>> {
        return noteDao.getArchivedNotesByUser(userId).map { entities ->
            entities.map { noteEntityMapper.toDomain(it) }
        }
    }

    override fun getDeletedNotesByUser(userId: Int): Flow<List<Note>> {
        // TODO this flow will be update once deleted notes logic implemented
        return flowOf(emptyList())
    }

    override fun getFavoriteNotesByUser(userId: Int): Flow<List<Note>> {
        return noteDao.getFavoriteNotesByUser(userId).map { entities ->
            entities.map { noteEntityMapper.toDomain(it) }
        }
    }

    override fun getNotesByCategory(userId: Int, categoryId: Int): Flow<List<Note>> {
        return noteDao.getNotesByCategory(userId, categoryId).map { entities ->
            entities.map { noteEntityMapper.toDomain(it) }
        }
    }

    override suspend fun getNoteById(noteId: Int): Note? {
        return try {
            noteDao.getNoteWithDetails(noteId)?.let {
                noteEntityMapper.toDomain(it)
            }
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error getting note by id: ${e.message}")
            null
        }
    }

    override fun searchNotes(userId: Int, searchQuery: String): Flow<List<Note>> {
        return noteDao.searchNotes(userId, searchQuery).map { entities ->
            entities.map { noteEntityMapper.toDomain(it) }
        }
    }

    override suspend fun insertNote(note: Note): Result<Note> {
        return try {
            val entity = noteEntityMapper.toEntity(note)
            val insertedId: Long = noteDao.insert(entity)
            val insertedNote = note.copy(id = insertedId.toInt())
            
            // Add tags if any
            if (note.tags.isNotEmpty()) {
                note.tags.forEach { tag ->
                    noteDao.insertNoteTagCrossRef(NoteTagCrossRef(insertedId.toInt(), tag.id))
                    tagDao.updateTagUsageCount(tag.id)
                }
            }
            
            Result.success(insertedNote)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error inserting note: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateNote(note: Note): Result<Note> {
        return try {
            val entity = noteEntityMapper.toEntity(note)
            val tagList = tagDao.getTagsByNote(note.id) // tags in db
            if (tagList.isNotEmpty()) {
                for (tag in tagList) { // tags in db
                    if (!note.tags.contains(tagEntityMapper.toDomain(tag))) { // if not in updated tag list remove from cross ref
                        noteDao.deleteNoteTagCrossRef(NoteTagCrossRef(note.id, tag.id))
                        tagDao.updateTagUsageCount(tag.id)
                    }
                }
            }
            if (note.tags.isNotEmpty()) {
                for (tag in note.tags) { // updated tag list
                    if (!tagList.contains(tagEntityMapper.toEntity(tag))) { // if in updated list but not in db insert it
                        noteDao.insertNoteTagCrossRef(NoteTagCrossRef(note.id, tag.id))
                        tagDao.updateTagUsageCount(tag.id)
                    }
                }
            }

            noteDao.update(entity)
            Result.success(note)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error updating note: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun deleteNote(noteId: Int): Result<Unit> {
        return try {
            // Get existing tags to update their usage count
            val existingTags = tagDao.getTagsByNote(noteId)
            
            noteDao.deleteById(noteId)
            
            // Update tag usage counts
            existingTags.forEach { tag ->
                tagDao.updateTagUsageCount(tag.id)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error deleting note: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updatePinnedStatus(noteId: Int, isPinned: Boolean): Result<Unit> {
        return try {
            noteDao.updatePinnedStatus(noteId, isPinned)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error updating pinned status: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateFavoriteStatus(noteId: Int, isFavorite: Boolean): Result<Unit> {
        return try {
            noteDao.updateFavoriteStatus(noteId, isFavorite)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error updating favorite status: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateArchivedStatus(noteId: Int, isArchived: Boolean): Result<Unit> {
        return try {
            noteDao.updateArchivedStatus(noteId, isArchived)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error updating archived status: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateNoteCategory(noteId: Int, categoryId: Int?): Result<Unit> {
        return try {
            noteDao.updateNoteCategory(noteId, categoryId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error updating note category: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun addTagsToNote(noteId: Int, tagIds: List<Int>): Result<Unit> {
        return try {
            tagIds.forEach { tagId ->
                noteDao.insertNoteTagCrossRef(NoteTagCrossRef(noteId, tagId))
                tagDao.updateTagUsageCount(tagId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error adding tags to note: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun removeTagsFromNote(noteId: Int, tagIds: List<Int>): Result<Unit> {
        return try {
            tagIds.forEach { tagId ->
                noteDao.deleteNoteTagCrossRef(NoteTagCrossRef(noteId, tagId))
                tagDao.updateTagUsageCount(tagId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error removing tags from note: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateNoteTags(noteId: Int, tagIds: List<Int>): Result<Unit> {
        return try {
            // Get existing tags
            val existingTags = tagDao.getTagsByNote(noteId)
            
            // Remove all existing tags
            noteDao.deleteAllTagsForNote(noteId)
            
            // Add new tags
            tagIds.forEach { tagId ->
                noteDao.insertNoteTagCrossRef(NoteTagCrossRef(noteId, tagId))
            }
            
            // Update usage counts for all affected tags
            existingTags.forEach { tag ->
                tagDao.updateTagUsageCount(tag.id)
            }
            tagIds.forEach { tagId ->
                tagDao.updateTagUsageCount(tagId)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error updating note tags: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getNotesCount(userId: Int): Int {
        return try {
            noteDao.getNotesCount(userId)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error getting notes count: ${e.message}")
            0
        }
    }

    override suspend fun getNotesCountByCategory(userId: Int, categoryId: Int): Int {
        return try {
            noteDao.getNotesCountByCategory(userId, categoryId)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error getting notes count by category: ${e.message}")
            0
        }
    }

    override suspend fun deleteAllArchivedNotes(userId: Int): Result<Unit> {
        return try {
            noteDao.deleteAllArchivedNotes(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NoteRepositoryImpl", "Error deleting archived notes: ${e.message}")
            Result.failure(e)
        }
    }
}
