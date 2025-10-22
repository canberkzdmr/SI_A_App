package com.cbo.notes.data.mapper

import com.cbo.core.database.entity.NoteEntity
import com.cbo.core.database.entity.NoteWithDetails
import com.cbo.notes.domain.model.Note
import com.cbo.notes.domain.model.NoteContent
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import javax.inject.Inject

class NoteEntityMapper @Inject constructor(
    private val categoryEntityMapper: CategoryEntityMapper,
    private val tagEntityMapper: TagEntityMapper
) {
    
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    fun toDomain(entity: NoteEntity): Note {
        val richContent = try {
            if (entity.content.startsWith("{") && entity.content.contains("\"blocks\"")) {
                json.decodeFromString<NoteContent>(entity.content)
            } else {
                null // Plain text, will be converted on demand
            }
        } catch (e: Exception) {
            null // If parsing fails, treat as plain text
        }
        
        return Note(
            id = entity.id,
            userId = entity.userId,
            title = entity.title,
            content = entity.content,
            richContent = richContent,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            isPinned = entity.isPinned,
            isArchived = entity.isArchived,
            isFavorite = entity.isFavorite
        )
    }
    
    fun toDomain(noteWithDetails: NoteWithDetails): Note {
        val richContent = try {
            if (noteWithDetails.note.content.startsWith("{") && noteWithDetails.note.content.contains("\"blocks\"")) {
                json.decodeFromString<NoteContent>(noteWithDetails.note.content)
            } else {
                null // Plain text, will be converted on demand
            }
        } catch (e: Exception) {
            null // If parsing fails, treat as plain text
        }
        
        return Note(
            id = noteWithDetails.note.id,
            userId = noteWithDetails.note.userId,
            title = noteWithDetails.note.title,
            content = noteWithDetails.note.content,
            richContent = richContent,
            category = noteWithDetails.category?.let { categoryEntityMapper.toDomain(it) },
            tags = noteWithDetails.tags.map { tagEntityMapper.toDomain(it) },
            createdAt = noteWithDetails.note.createdAt,
            updatedAt = noteWithDetails.note.updatedAt,
            isPinned = noteWithDetails.note.isPinned,
            isArchived = noteWithDetails.note.isArchived,
            isFavorite = noteWithDetails.note.isFavorite
        )
    }
    
    fun toEntity(domain: Note): NoteEntity {
        // Serialize rich content if present, otherwise use plain content
        val contentToStore = if (domain.richContent != null) {
            json.encodeToString(domain.richContent)
        } else {
            domain.content
        }
        
        return NoteEntity(
            id = domain.id,
            userId = domain.userId,
            title = domain.title,
            content = contentToStore,
            categoryId = domain.category?.id,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            isPinned = domain.isPinned,
            isArchived = domain.isArchived,
            isFavorite = domain.isFavorite
        )
    }
}
