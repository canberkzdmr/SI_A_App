package com.cbo.notes.domain.repository

import com.cbo.notes.domain.model.NoteTemplate
import kotlinx.coroutines.flow.Flow

interface NoteTemplateRepository {
    fun getTemplatesForUser(userId: Int): Flow<List<NoteTemplate>>
    suspend fun getTemplateById(id: Int): NoteTemplate?
    suspend fun addTemplate(template: NoteTemplate): Long
    suspend fun deleteTemplateById(id: Int)
}
