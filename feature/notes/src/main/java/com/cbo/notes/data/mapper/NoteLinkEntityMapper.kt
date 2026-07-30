package com.cbo.notes.data.mapper

import com.cbo.core.database.entity.NoteLinkEntity
import com.cbo.notes.domain.model.NoteLink
import javax.inject.Inject

class NoteLinkEntityMapper @Inject constructor() {
    fun toDomain(entity: NoteLinkEntity): NoteLink {
        return NoteLink(
            id = entity.id,
            sourceNoteId = entity.sourceNoteId,
            targetNoteId = entity.targetNoteId,
            createdAt = entity.createdAt
        )
    }

    fun toEntity(domain: NoteLink): NoteLinkEntity {
        return NoteLinkEntity(
            id = domain.id,
            sourceNoteId = domain.sourceNoteId,
            targetNoteId = domain.targetNoteId,
            createdAt = domain.createdAt
        )
    }
}
