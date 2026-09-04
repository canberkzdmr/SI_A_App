package com.cbo.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.cbo.core.database.entity.NoteLinkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteLinkDao : BaseDao<NoteLinkEntity> {
    @Query("SELECT * FROM note_links WHERE sourceNoteId = :sourceId")
    fun getLinksFromNote(sourceId: Int): Flow<List<NoteLinkEntity>>

    @Query("SELECT * FROM note_links WHERE targetNoteId = :targetId")
    fun getLinksToNote(targetId: Int): Flow<List<NoteLinkEntity>>

    @Query("DELETE FROM note_links WHERE sourceNoteId = :sourceId AND targetNoteId = :targetId")
    suspend fun deleteLink(sourceId: Int, targetId: Int)

    @Query("DELETE FROM note_links WHERE sourceNoteId = :noteId OR targetNoteId = :noteId")
    suspend fun deleteAllLinksForNote(noteId: Int)

    @Query("""
        SELECT l.* FROM note_links l
        INNER JOIN notes n ON l.sourceNoteId = n.id
        WHERE n.userId = :userId AND n.isDeleted = 0
    """)
    suspend fun getAllLinksForUser(userId: Int): List<NoteLinkEntity>

    // =========================================================================
    // STATISTICS QUERIES
    // =========================================================================

    /**
     * Belirli bir kullanıcıya ait toplam bağlantı sayısı.
     * Yalnızca silinmemiş notlara ait bağlantılar sayılır.
     */
    @Query("""
        SELECT COUNT(*) FROM note_links l
        INNER JOIN notes n ON l.sourceNoteId = n.id
        WHERE n.userId = :userId AND n.isDeleted = 0
    """)
    suspend fun getTotalLinkCountForUser(userId: Int): Int

    /**
     * En çok referans verilen (hedef) notlar — Zettelkasten hub analizi.
     * @return List of (noteId, incomingLinkCount) pairs
     */
    @Query("""
        SELECT l.targetNoteId as noteId, COUNT(*) as linkCount
        FROM note_links l
        INNER JOIN notes n ON l.targetNoteId = n.id
        WHERE n.userId = :userId AND n.isDeleted = 0
        GROUP BY l.targetNoteId
        ORDER BY linkCount DESC
        LIMIT :limit
    """)
    suspend fun getMostLinkedNotes(userId: Int, limit: Int = 5): List<NoteLinkCount>

    /**
     * Hiç bağlantısı olmayan not sayısı (izole / orphan notlar).
     * Kaynak veya hedef olarak hiç yer almayan aktif notlar.
     */
    @Query("""
        SELECT COUNT(*) FROM notes n
        WHERE n.userId = :userId AND n.isDeleted = 0
        AND n.id NOT IN (
            SELECT sourceNoteId FROM note_links
            UNION
            SELECT targetNoteId FROM note_links
        )
    """)
    suspend fun getOrphanNoteCount(userId: Int): Int
}

/** En çok bağlantı alan not için yardımcı data class */
data class NoteLinkCount(val noteId: Int, val linkCount: Int)
