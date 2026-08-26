package com.cbo.notes.domain.repository

import com.cbo.notes.domain.model.Note
import com.cbo.notes.domain.model.NoteStatistics
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotesByUser(userId: Int): Flow<List<Note>>
    fun getArchivedNotesByUser(userId: Int): Flow<List<Note>>
    fun getDeletedNotesByUser(userId: Int): Flow<List<Note>>
    fun getFavoriteNotesByUser(userId: Int): Flow<List<Note>>
    fun getNotesByCategory(userId: Int, categoryId: Int): Flow<List<Note>>
    suspend fun getNoteById(noteId: Int): Note?
    fun searchNotes(userId: Int, searchQuery: String): Flow<List<Note>>
    
    suspend fun insertNote(note: Note): Result<Note>
    suspend fun updateNote(note: Note): Result<Note>
    
    /** Soft deletes a note - marks it as deleted with a timestamp */
    suspend fun softDeleteNote(noteId: Int): Result<Unit>
    
    /** Restores a soft-deleted note */
    suspend fun restoreDeletedNote(noteId: Int): Result<Unit>
    
    /** Permanently deletes a note from the database */
    suspend fun permanentlyDeleteNote(noteId: Int): Result<Unit>
    
    /** Permanently deletes all notes that have been soft-deleted for longer than the retention period */
    suspend fun cleanupExpiredDeletedNotes(): Result<Int>
    
    @Deprecated("Use softDeleteNote instead for soft delete behavior")
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
    
    // Reminder operations
    /** Sets or updates the reminder time for a note */
    suspend fun setReminder(
        noteId: Int, 
        reminderTime: Long, 
        repeat: com.cbo.notes.domain.model.ReminderRepeat = com.cbo.notes.domain.model.ReminderRepeat.NONE, 
        priority: com.cbo.notes.domain.model.ReminderPriority = com.cbo.notes.domain.model.ReminderPriority.DEFAULT
    ): Result<Unit>
    
    /** Removes the reminder from a note */
    suspend fun removeReminder(noteId: Int): Result<Unit>
    
    /** Gets all notes with active reminders (future reminders) */
    fun getNotesWithActiveReminders(): Flow<List<Note>>
    
    /** Gets notes with reminders scheduled between the specified times */
    suspend fun getNotesWithRemindersBetween(startTime: Long, endTime: Long): List<Note>
    
    /** Enables or disables the location reminder for a note */
    suspend fun setLocationReminderEnabled(noteId: Int, isEnabled: Boolean): Result<Unit>
    
    /** Gets all notes with active location reminders */
    suspend fun getNotesWithActiveLocationReminders(): List<Note>

    // -------------------------------------------------------------------------
    // Statistics
    // -------------------------------------------------------------------------

    /**
     * Kullanıcıya ait tüm istatistikleri tek seferde hesaplayıp döndürür.
     * Paralel coroutine'lerle tüm DAO sorgularını çalıştırır.
     */
    suspend fun getNoteStatistics(userId: Int): NoteStatistics
}
