package com.cbo.notes.domain.model

data class Category(
    val id: Int = 0,
    val userId: Int,
    val name: String,
    val color: String? = null,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0,
    val notesCount: Int = 0
)
