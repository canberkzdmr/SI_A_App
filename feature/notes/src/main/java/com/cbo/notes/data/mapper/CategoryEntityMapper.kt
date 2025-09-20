package com.cbo.notes.data.mapper

import com.cbo.core.database.entity.CategoryEntity
import com.cbo.core.database.entity.CategoryWithNotes
import com.cbo.notes.domain.model.Category
import javax.inject.Inject

class CategoryEntityMapper @Inject constructor() {
    
    fun toDomain(entity: CategoryEntity, notesCount: Int = 0): Category {
        return Category(
            id = entity.id,
            userId = entity.userId,
            name = entity.name,
            color = entity.color,
            description = entity.description,
            createdAt = entity.createdAt,
            sortOrder = entity.sortOrder,
            notesCount = notesCount
        )
    }
    
    fun toDomain(categoryWithNotes: CategoryWithNotes): Category {
        return Category(
            id = categoryWithNotes.category.id,
            userId = categoryWithNotes.category.userId,
            name = categoryWithNotes.category.name,
            color = categoryWithNotes.category.color,
            description = categoryWithNotes.category.description,
            createdAt = categoryWithNotes.category.createdAt,
            sortOrder = categoryWithNotes.category.sortOrder,
            notesCount = categoryWithNotes.notes.size
        )
    }
    
    fun toEntity(domain: Category): CategoryEntity {
        return CategoryEntity(
            id = domain.id,
            userId = domain.userId,
            name = domain.name,
            color = domain.color,
            description = domain.description,
            createdAt = domain.createdAt,
            sortOrder = domain.sortOrder
        )
    }
}
