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
}
