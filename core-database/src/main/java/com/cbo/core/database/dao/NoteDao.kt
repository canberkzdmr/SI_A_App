package com.cbo.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.cbo.core.database.entity.NoteEntity
import com.cbo.core.database.entity.NoteTagCrossRef
import com.cbo.core.database.entity.NoteWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao : BaseDao<NoteEntity> {
    
    @Query("SELECT * FROM notes WHERE userId = :userId AND isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotesByUser(userId: Int): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE userId = :userId AND isArchived = 1 ORDER BY updatedAt DESC")
    fun getArchivedNotesByUser(userId: Int): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE userId = :userId AND isFavorite = 1 AND isArchived = 0 ORDER BY updatedAt DESC")
    fun getFavoriteNotesByUser(userId: Int): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE userId = :userId AND categoryId = :categoryId AND isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotesByCategory(userId: Int, categoryId: Int): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: Int): NoteEntity?

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteWithDetails(noteId: Int): NoteWithDetails?

    @Transaction
    @Query("SELECT * FROM notes WHERE userId = :userId AND isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotesWithDetailsByUser(userId: Int): Flow<List<NoteWithDetails>>

    @Query("SELECT * FROM notes WHERE userId = :userId AND (title LIKE '%' || :searchQuery || '%' OR content LIKE '%' || :searchQuery || '%') AND isArchived = 0 ORDER BY updatedAt DESC")
    fun searchNotes(userId: Int, searchQuery: String): Flow<List<NoteEntity>>

    @Query("UPDATE notes SET isPinned = :isPinned, updatedAt = :updatedAt WHERE id = :noteId")
    suspend fun updatePinnedStatus(noteId: Int, isPinned: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :noteId")
    suspend fun updateFavoriteStatus(noteId: Int, isFavorite: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET isArchived = :isArchived, updatedAt = :updatedAt WHERE id = :noteId")
    suspend fun updateArchivedStatus(noteId: Int, isArchived: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET categoryId = :categoryId, updatedAt = :updatedAt WHERE id = :noteId")
    suspend fun updateNoteCategory(noteId: Int, categoryId: Int?, updatedAt: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNoteTagCrossRef(crossRef: NoteTagCrossRef)

    @Delete
    suspend fun deleteNoteTagCrossRef(crossRef: NoteTagCrossRef)

    @Query("DELETE FROM note_tag_cross_ref WHERE noteId = :noteId")
    suspend fun deleteAllTagsForNote(noteId: Int)

    @Query("SELECT COUNT(*) FROM notes WHERE userId = :userId AND isArchived = 0")
    suspend fun getNotesCount(userId: Int): Int

    @Query("SELECT COUNT(*) FROM notes WHERE userId = :userId AND categoryId = :categoryId AND isArchived = 0")
    suspend fun getNotesCountByCategory(userId: Int, categoryId: Int): Int

    @Query("DELETE FROM notes WHERE userId = :userId AND isArchived = 1")
    suspend fun deleteAllArchivedNotes(userId: Int)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Int)
}
