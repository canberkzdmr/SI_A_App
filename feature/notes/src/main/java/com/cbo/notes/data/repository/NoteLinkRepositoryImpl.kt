package com.cbo.notes.data.repository

import com.cbo.core.database.dao.NoteDao
import com.cbo.core.database.dao.NoteLinkDao
import com.cbo.notes.data.mapper.NoteEntityMapper
import com.cbo.notes.data.mapper.NoteLinkEntityMapper
import com.cbo.notes.domain.model.Note
import com.cbo.notes.domain.model.NoteLink
import com.cbo.notes.domain.repository.NoteLinkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteLinkRepositoryImpl @Inject constructor(
    private val linkDao: NoteLinkDao,
    private val noteDao: NoteDao,
    private val linkMapper: NoteLinkEntityMapper,
    private val noteMapper: NoteEntityMapper
) : NoteLinkRepository {

    override fun getLinksFromNote(sourceId: Int): Flow<List<NoteLink>> {
        return linkDao.getLinksFromNote(sourceId).map { list ->
            list.map { linkMapper.toDomain(it) }
        }
    }

    override fun getLinksToNote(targetId: Int): Flow<List<NoteLink>> {
        return linkDao.getLinksToNote(targetId).map { list ->
            list.map { linkMapper.toDomain(it) }
        }
    }

    override fun getBacklinksForNote(noteId: Int): Flow<List<Note>> {
        return noteDao.getBacklinksForNote(noteId).map { list ->
            list.map { noteMapper.toDomain(it) }
        }
    }

    override suspend fun addLink(link: NoteLink): Long {
        return linkDao.insert(linkMapper.toEntity(link))
    }

    override suspend fun deleteLink(sourceId: Int, targetId: Int) {
        linkDao.deleteLink(sourceId, targetId)
    }

    override suspend fun deleteAllLinksForNote(noteId: Int) {
        linkDao.deleteAllLinksForNote(noteId)
    }
}
