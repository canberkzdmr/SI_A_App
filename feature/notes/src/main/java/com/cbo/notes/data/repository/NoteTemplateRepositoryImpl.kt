package com.cbo.notes.data.repository

import com.cbo.core.database.dao.NoteTemplateDao
import com.cbo.notes.data.mapper.NoteTemplateEntityMapper
import com.cbo.notes.domain.model.NoteTemplate
import com.cbo.notes.domain.repository.NoteTemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteTemplateRepositoryImpl @Inject constructor(
    private val dao: NoteTemplateDao,
    private val mapper: NoteTemplateEntityMapper
) : NoteTemplateRepository {

    override fun getTemplatesForUser(userId: Int): Flow<List<NoteTemplate>> {
        return dao.getTemplatesForUser(userId).map { list ->
            list.map { mapper.toDomain(it) }
        }
    }

    override suspend fun getTemplateById(id: Int): NoteTemplate? {
        return dao.getTemplateById(id)?.let { mapper.toDomain(it) }
    }

    override suspend fun addTemplate(template: NoteTemplate): Long {
        return dao.insert(mapper.toEntity(template))
    }

    override suspend fun deleteTemplateById(id: Int) {
        dao.deleteTemplateById(id)
    }
}
