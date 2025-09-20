package com.cbo.notes.domain.model

data class Note(
    val id: Int = 0,
    val userId: Int,
    val title: String,
    val content: String,
    val category: Category? = null,
    val tags: List<Tag> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isFavorite: Boolean = false
)
