package com.cbo.notes.data.mapper

import com.cbo.core.database.entity.NoteTemplateEntity
import com.cbo.notes.domain.model.NoteTemplate
import javax.inject.Inject

class NoteTemplateEntityMapper @Inject constructor() {
    fun toDomain(entity: NoteTemplateEntity): NoteTemplate {
        return NoteTemplate(
            id = entity.id,
            userId = entity.userId,
            name = entity.name,
            content = entity.content,
            createdAt = entity.createdAt
        )
    }

    fun toEntity(domain: NoteTemplate): NoteTemplateEntity {
        return NoteTemplateEntity(
            id = domain.id,
            userId = domain.userId,
            name = domain.name,
            content = domain.content,
            createdAt = domain.createdAt
        )
    }
}
