package com.cbo.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.cbo.core.database.entity.NoteEntity
import com.cbo.core.database.entity.NoteTagCrossRef
import com.cbo.core.database.entity.NoteWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao : BaseDao<NoteEntity> {
    
    // Regular notes (not archived, not deleted)
    @Query("SELECT * FROM notes WHERE userId = :userId AND isArchived = 0 AND isDeleted = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotesByUser(userId: Int): Flow<List<NoteEntity>>

    // Archived notes (not deleted)
    @Query("SELECT * FROM notes WHERE userId = :userId AND isArchived = 1 AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun getArchivedNotesByUser(userId: Int): Flow<List<NoteEntity>>

    // Soft deleted notes (within retention period)
    @Query("SELECT * FROM notes WHERE userId = :userId AND isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedNotesByUser(userId: Int): Flow<List<NoteEntity>>

    // Soft deleted notes with details (within retention period)
    @Transaction
    @Query("SELECT * FROM notes WHERE userId = :userId AND isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedNotesWithDetailsByUser(userId: Int): Flow<List<NoteWithDetails>>

    @Query("SELECT * FROM notes WHERE userId = :userId AND isFavorite = 1 AND isArchived = 0 AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun getFavoriteNotesByUser(userId: Int): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE userId = :userId AND categoryId = :categoryId AND isArchived = 0 AND isDeleted = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotesByCategory(userId: Int, categoryId: Int): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :noteId AND isDeleted = 0")
    suspend fun getNoteById(noteId: Int): NoteEntity?

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteWithDetails(noteId: Int): NoteWithDetails?

    @Transaction
    @Query("SELECT * FROM notes WHERE userId = :userId AND isArchived = 0 AND isDeleted = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotesWithDetailsByUser(userId: Int): Flow<List<NoteWithDetails>>

    @Query("SELECT * FROM notes WHERE userId = :userId AND (title LIKE '%' || :searchQuery || '%' OR content LIKE '%' || :searchQuery || '%') AND isArchived = 0 AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun searchNotes(userId: Int, searchQuery: String): Flow<List<NoteEntity>>

    @Query("UPDATE notes SET isPinned = :isPinned, updatedAt = :updatedAt WHERE id = :noteId")
    suspend fun updatePinnedStatus(noteId: Int, isPinned: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :noteId")
    suspend fun updateFavoriteStatus(noteId: Int, isFavorite: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET isArchived = :isArchived, updatedAt = :updatedAt WHERE id = :noteId")
    suspend fun updateArchivedStatus(noteId: Int, isArchived: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET categoryId = :categoryId, updatedAt = :updatedAt WHERE id = :noteId")
    suspend fun updateNoteCategory(noteId: Int, categoryId: Int?, updatedAt: Long = System.currentTimeMillis())

    // Soft delete - marks note as deleted with timestamp
    @Query("UPDATE notes SET isDeleted = 1, deletedAt = :deletedAt, isPinned = 0 WHERE id = :noteId")
    suspend fun softDeleteNote(noteId: Int, deletedAt: Long = System.currentTimeMillis())

    // Restore soft-deleted note
    @Query("UPDATE notes SET isDeleted = 0, deletedAt = NULL WHERE id = :noteId")
    suspend fun restoreDeletedNote(noteId: Int)

    // Permanently delete notes older than specified timestamp
    @Query("DELETE FROM notes WHERE isDeleted = 1 AND deletedAt < :expirationTimestamp")
    suspend fun permanentlyDeleteExpiredNotes(expirationTimestamp: Long): Int

    // Permanently delete a specific note
    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun permanentlyDeleteNote(noteId: Int)

    // Get expired deleted notes (for cleanup)
    @Query("SELECT * FROM notes WHERE isDeleted = 1 AND deletedAt < :expirationTimestamp")
    suspend fun getExpiredDeletedNotes(expirationTimestamp: Long): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNoteTagCrossRef(crossRef: NoteTagCrossRef)

    @Delete
    suspend fun deleteNoteTagCrossRef(crossRef: NoteTagCrossRef)

    @Query("DELETE FROM note_tag_cross_ref WHERE noteId = :noteId")
    suspend fun deleteAllTagsForNote(noteId: Int)

    @Query("SELECT COUNT(*) FROM notes WHERE userId = :userId AND isArchived = 0 AND isDeleted = 0")
    suspend fun getNotesCount(userId: Int): Int

    @Query("SELECT COUNT(*) FROM notes WHERE userId = :userId AND categoryId = :categoryId AND isArchived = 0 AND isDeleted = 0")
    suspend fun getNotesCountByCategory(userId: Int, categoryId: Int): Int

    @Query("DELETE FROM notes WHERE userId = :userId AND isArchived = 1")
    suspend fun deleteAllArchivedNotes(userId: Int)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Int)

    // Reminder operations
    @Query("UPDATE notes SET reminderTime = :reminderTime, reminderRepeat = :reminderRepeat, reminderPriority = :reminderPriority, updatedAt = :updatedAt WHERE id = :noteId")
    suspend fun updateReminder(
        noteId: Int, 
        reminderTime: Long?, 
        reminderRepeat: String? = "NONE", 
        reminderPriority: String? = "DEFAULT", 
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("SELECT * FROM notes WHERE reminderTime IS NOT NULL AND reminderTime > :currentTime AND isDeleted = 0 ORDER BY reminderTime ASC")
    fun getNotesWithActiveReminders(currentTime: Long = System.currentTimeMillis()): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :noteId AND reminderTime IS NOT NULL")
    suspend fun getNoteWithReminder(noteId: Int): NoteEntity?

    @Query("SELECT * FROM notes WHERE reminderTime BETWEEN :startTime AND :endTime AND isDeleted = 0")
    suspend fun getNotesWithRemindersBetween(startTime: Long, endTime: Long): List<NoteEntity>
    @Query("""
        SELECT n.* FROM notes n 
        INNER JOIN note_links l ON n.id = l.sourceNoteId 
        WHERE l.targetNoteId = :noteId AND n.isDeleted = 0
    """)
    fun getBacklinksForNote(noteId: Int): Flow<List<NoteEntity>>
}
