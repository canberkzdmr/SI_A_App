package com.cbo.notes.data.mapper

import com.cbo.core.database.entity.TagEntity
import com.cbo.core.database.entity.TagWithNotes
import com.cbo.notes.domain.model.Tag
import javax.inject.Inject

class TagEntityMapper @Inject constructor() {
    
    fun toDomain(entity: TagEntity): Tag {
        return Tag(
            id = entity.id,
            userId = entity.userId,
            name = entity.name,
            color = entity.color,
            createdAt = entity.createdAt,
            usageCount = entity.usageCount
        )
    }
    
    fun toDomain(tagWithNotes: TagWithNotes): Tag {
        return Tag(
            id = tagWithNotes.tag.id,
            userId = tagWithNotes.tag.userId,
            name = tagWithNotes.tag.name,
            color = tagWithNotes.tag.color,
            createdAt = tagWithNotes.tag.createdAt,
            usageCount = tagWithNotes.notes.size
        )
    }
    
    fun toEntity(domain: Tag): TagEntity {
        return TagEntity(
            id = domain.id,
            userId = domain.userId,
            name = domain.name,
            color = domain.color,
            createdAt = domain.createdAt,
            usageCount = domain.usageCount
        )
    }
}
