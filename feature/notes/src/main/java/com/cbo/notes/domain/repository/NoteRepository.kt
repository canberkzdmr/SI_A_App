package com.cbo.notes.domain.repository

import com.cbo.notes.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotesByUser(userId: Int): Flow<List<Note>>
    fun getArchivedNotesByUser(userId: Int): Flow<List<Note>>
    fun getFavoriteNotesByUser(userId: Int): Flow<List<Note>>
    fun getNotesByCategory(userId: Int, categoryId: Int): Flow<List<Note>>
    suspend fun getNoteById(noteId: Int): Note?
    fun searchNotes(userId: Int, searchQuery: String): Flow<List<Note>>
    
    suspend fun insertNote(note: Note): Result<Note>
    suspend fun updateNote(note: Note): Result<Note>
    suspend fun deleteNote(noteId: Int): Result<Unit>
    
    suspend fun updatePinnedStatus(noteId: Int, isPinned: Boolean): Result<Unit>
    suspend fun updateFavoriteStatus(noteId: Int, isFavorite: Boolean): Result<Unit>
    suspend fun updateArchivedStatus(noteId: Int, isArchived: Boolean): Result<Unit>
    suspend fun updateNoteCategory(noteId: Int, categoryId: Int?): Result<Unit>
    
    suspend fun addTagsToNote(noteId: Int, tagIds: List<Int>): Result<Unit>
    suspend fun removeTagsFromNote(noteId: Int, tagIds: List<Int>): Result<Unit>
    suspend fun updateNoteTags(noteId: Int, tagIds: List<Int>): Result<Unit>
    
    suspend fun getNotesCount(userId: Int): Int
    suspend fun getNotesCountByCategory(userId: Int, categoryId: Int): Int
    suspend fun deleteAllArchivedNotes(userId: Int): Result<Unit>
}
