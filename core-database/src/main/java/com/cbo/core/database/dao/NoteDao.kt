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

    @Query("SELECT * FROM notes WHERE ((reminderTime IS NOT NULL AND reminderTime > :currentTime) OR isLocationReminderEnabled = 1) AND isDeleted = 0 ORDER BY reminderTime ASC")
    fun getNotesWithActiveReminders(currentTime: Long = System.currentTimeMillis()): Flow<List<NoteEntity>>

    @Query("UPDATE notes SET isLocationReminderEnabled = :isEnabled, updatedAt = :updatedAt WHERE id = :noteId")
    suspend fun updateLocationReminderEnabled(noteId: Int, isEnabled: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM notes WHERE isLocationReminderEnabled = 1 AND isDeleted = 0")
    suspend fun getNotesWithActiveLocationReminders(): List<NoteEntity>

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

    // =========================================================================
    // STATISTICS QUERIES
    // =========================================================================

    /** Arşivlenmiş not sayısı */
    @Query("SELECT COUNT(*) FROM notes WHERE userId = :userId AND isArchived = 1 AND isDeleted = 0")
    suspend fun getArchivedNotesCount(userId: Int): Int

    /** Favori not sayısı */
    @Query("SELECT COUNT(*) FROM notes WHERE userId = :userId AND isFavorite = 1 AND isArchived = 0 AND isDeleted = 0")
    suspend fun getFavoriteNotesCount(userId: Int): Int

    /** Sabitlenmiş not sayısı */
    @Query("SELECT COUNT(*) FROM notes WHERE userId = :userId AND isPinned = 1 AND isArchived = 0 AND isDeleted = 0")
    suspend fun getPinnedNotesCount(userId: Int): Int

    /** Silinmiş not sayısı */
    @Query("SELECT COUNT(*) FROM notes WHERE userId = :userId AND isDeleted = 1")
    suspend fun getDeletedNotesCount(userId: Int): Int

    /**
     * Günlük not oluşturma sayısı (YYYY-MM-DD formatında).
     * Heatmap ve streak hesaplama için kullanılır.
     */
    @Query("""
        SELECT strftime('%Y-%m-%d', createdAt / 1000, 'unixepoch') as day, COUNT(*) as noteCount
        FROM notes
        WHERE userId = :userId AND isDeleted = 0
        GROUP BY day
        ORDER BY day ASC
    """)
    suspend fun getNotesCountPerDay(userId: Int): List<DayCount>

    /** Haftanın günlerine (1=Pzt..7=Paz) göre not sayısı */
    @Query("""
        SELECT CAST(strftime('%w', createdAt / 1000, 'unixepoch') AS INTEGER) as dayOfWeek,
               COUNT(*) as noteCount
        FROM notes
        WHERE userId = :userId AND isDeleted = 0
        GROUP BY dayOfWeek
    """)
    suspend fun getNotesCountByDayOfWeek(userId: Int): List<DayOfWeekCount>

    /** Saate (0-23) göre not sayısı */
    @Query("""
        SELECT CAST(strftime('%H', createdAt / 1000, 'unixepoch') AS INTEGER) as hour,
               COUNT(*) as noteCount
        FROM notes
        WHERE userId = :userId AND isDeleted = 0
        GROUP BY hour
    """)
    suspend fun getNotesCountByHour(userId: Int): List<HourCount>

    /** Aylık not sayısı (YYYY-MM formatında) — trend grafiği için */
    @Query("""
        SELECT strftime('%Y-%m', createdAt / 1000, 'unixepoch') as month, COUNT(*) as noteCount
        FROM notes
        WHERE userId = :userId AND isDeleted = 0
        GROUP BY month
        ORDER BY month ASC
    """)
    suspend fun getNotesCountPerMonth(userId: Int): List<MonthCount>

    /** Kategori bazlı not dağılımı */
    @Query("""
        SELECT COALESCE(c.name, 'Kategorisiz') as categoryName, COUNT(n.id) as noteCount
        FROM notes n
        LEFT JOIN categories c ON n.categoryId = c.id
        WHERE n.userId = :userId AND n.isArchived = 0 AND n.isDeleted = 0
        GROUP BY categoryName
        ORDER BY noteCount DESC
    """)
    suspend fun getCategoryDistribution(userId: Int): List<CategoryCount>

    /** Renk bazlı not dağılımı */
    @Query("""
        SELECT COALESCE(color, 'Varsayılan') as colorName, COUNT(*) as noteCount
        FROM notes
        WHERE userId = :userId AND isDeleted = 0
        GROUP BY colorName
        ORDER BY noteCount DESC
    """)
    suspend fun getColorDistribution(userId: Int): List<ColorCount>

    /** Aktif hatırlatıcı sayısı (gelecekte olan) */
    @Query("""
        SELECT COUNT(*) FROM notes
        WHERE userId = :userId AND reminderTime IS NOT NULL
        AND reminderTime > :currentTime AND isDeleted = 0
    """)
    suspend fun getActiveReminderCount(userId: Int, currentTime: Long = System.currentTimeMillis()): Int

    /** Süresi geçmiş hatırlatıcı sayısı */
    @Query("""
        SELECT COUNT(*) FROM notes
        WHERE userId = :userId AND reminderTime IS NOT NULL
        AND reminderTime <= :currentTime AND isDeleted = 0
    """)
    suspend fun getExpiredReminderCount(userId: Int, currentTime: Long = System.currentTimeMillis()): Int

    /** Konum hatırlatıcılı not sayısı */
    @Query("""
        SELECT COUNT(*) FROM notes
        WHERE userId = :userId AND isLocationReminderEnabled = 1 AND isDeleted = 0
    """)
    suspend fun getLocationReminderCount(userId: Int): Int

    /**
     * "Eski" notlar — belirli bir tarihten önce oluşturulmuş ve hiç güncellenmemiş notlar.
     * Not sağlığı (hygiene) bölümü için kullanılır.
     */
    @Query("""
        SELECT COUNT(*) FROM notes
        WHERE userId = :userId AND isDeleted = 0 AND isArchived = 0
        AND updatedAt < :staleThreshold
    """)
    suspend fun getStaleNotesCount(userId: Int, staleThreshold: Long): Int

    /**
     * Tüm todo'ları tamamlanmış notların sayısı.
     * Arşivlemeye hazır notlar için kullanılır.
     */
    @Query("""
        SELECT COUNT(DISTINCT id) FROM notes
        WHERE userId = :userId AND isDeleted = 0 AND isArchived = 0
        AND todos != '[]' AND todos != ''
        AND id NOT IN (
            SELECT DISTINCT n2.id FROM notes n2
            WHERE n2.userId = :userId
        )
    """)
    suspend fun getFullyCompletedTodoNotesCount(userId: Int): Int

    /** Bu hafta oluşturulan not sayısı */
    @Query("""
        SELECT COUNT(*) FROM notes
        WHERE userId = :userId AND isDeleted = 0
        AND createdAt >= :weekStart
    """)
    suspend fun getWeeklyNoteCount(userId: Int, weekStart: Long): Int

    /** Bu hafta içinde oluşturulan/güncellenen notların todo'larından tamamlananlar */
    @Query("""
        SELECT todos FROM notes
        WHERE userId = :userId AND isDeleted = 0
        AND updatedAt >= :weekStart
    """)
    suspend fun getWeeklyUpdatedNoteTodos(userId: Int, weekStart: Long): List<String>
}

/**
 * Günlük not sayısı için yardımcı data class.
 * @property day "YYYY-MM-DD" formatında tarih
 * @property noteCount O güne ait not sayısı
 */
data class DayCount(val day: String, val noteCount: Int)

/** Haftanın gününe göre not sayısı (0=Pazar, 1=Pazartesi, ..., 6=Cumartesi) */
data class DayOfWeekCount(val dayOfWeek: Int, val noteCount: Int)

/** Saate göre not sayısı */
data class HourCount(val hour: Int, val noteCount: Int)

/** Aylık not sayısı */
data class MonthCount(val month: String, val noteCount: Int)

/** Kategori bazlı not dağılımı */
data class CategoryCount(val categoryName: String, val noteCount: Int)

/** Renk bazlı not dağılımı */
data class ColorCount(val colorName: String, val noteCount: Int)
