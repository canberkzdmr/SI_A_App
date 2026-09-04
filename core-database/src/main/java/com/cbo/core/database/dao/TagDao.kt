package com.cbo.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.cbo.core.database.entity.TagEntity
import com.cbo.core.database.entity.TagWithNotes
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao : BaseDao<TagEntity> {
    
    @Query("SELECT * FROM tags WHERE userId = :userId ORDER BY usageCount DESC, name ASC")
    fun getTagsByUser(userId: Int): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE userId = :userId ORDER BY usageCount DESC, name ASC")
    suspend fun getAllTagsForUser(userId: Int): List<TagEntity>

    @Query("SELECT * FROM tags WHERE id = :tagId")
    suspend fun getTagById(tagId: Int): TagEntity?

    @Query("SELECT * FROM tags WHERE userId = :userId AND name = :name")
    suspend fun getTagByName(userId: Int, name: String): TagEntity?

    @Query("SELECT * FROM tags WHERE userId = :userId AND name LIKE '%' || :query || '%' ORDER BY usageCount DESC, name ASC")
    fun searchTags(userId: Int, query: String): Flow<List<TagEntity>>

    @Transaction
    @Query("SELECT * FROM tags WHERE userId = :userId ORDER BY usageCount DESC, name ASC")
    fun getTagsWithNotes(userId: Int): Flow<List<TagWithNotes>>

    @Transaction
    @Query("SELECT * FROM tags WHERE id = :tagId")
    suspend fun getTagWithNotes(tagId: Int): TagWithNotes?

    @Query("UPDATE tags SET usageCount = (SELECT COUNT(*) FROM note_tag_cross_ref WHERE tagId = :tagId) WHERE id = :tagId")
    suspend fun updateTagUsageCount(tagId: Int)

    @Query("UPDATE tags SET color = :color WHERE id = :tagId")
    suspend fun updateTagColor(tagId: Int, color: String?)

    @Query("SELECT COUNT(*) FROM tags WHERE userId = :userId")
    suspend fun getTagsCount(userId: Int): Int

    @Query("SELECT DISTINCT t.* FROM tags t INNER JOIN note_tag_cross_ref ntc ON t.id = ntc.tagId WHERE ntc.noteId = :noteId")
    suspend fun getTagsByNote(noteId: Int): List<TagEntity>

    @Query("DELETE FROM tags WHERE userId = :userId AND usageCount = 0")
    suspend fun deleteUnusedTags(userId: Int)

    @Query("SELECT * FROM tags WHERE userId = :userId ORDER BY usageCount DESC LIMIT :limit")
    suspend fun getMostUsedTags(userId: Int, limit: Int = 10): List<TagEntity>

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteById(id: Int)

    /**
     * Belirli bir tarih aralığında en çok kullanılan etiketi getirir.
     * Haftalık özet (Weekly Digest) bölümü için kullanılır.
     */
    @Query("""
        SELECT t.* FROM tags t
        INNER JOIN note_tag_cross_ref ntc ON t.id = ntc.tagId
        INNER JOIN notes n ON ntc.noteId = n.id
        WHERE n.userId = :userId AND n.isDeleted = 0
        AND n.updatedAt >= :sinceTimestamp
        GROUP BY t.id
        ORDER BY COUNT(*) DESC
        LIMIT 1
    """)
    suspend fun getMostUsedTagSince(userId: Int, sinceTimestamp: Long): TagEntity?
}
