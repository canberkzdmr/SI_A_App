package com.cbo.notes.domain.repository

import com.cbo.notes.domain.model.Note
import com.cbo.notes.domain.model.NoteLink
import kotlinx.coroutines.flow.Flow

interface NoteLinkRepository {
    fun getLinksFromNote(sourceId: Int): Flow<List<NoteLink>>
    fun getLinksToNote(targetId: Int): Flow<List<NoteLink>>
    fun getBacklinksForNote(noteId: Int): Flow<List<Note>>
    suspend fun addLink(link: NoteLink): Long
    suspend fun deleteLink(sourceId: Int, targetId: Int)
    suspend fun deleteAllLinksForNote(noteId: Int)
}
