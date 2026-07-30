package com.cbo.notes.domain.model

data class NoteTemplate(
    val id: Int = 0,
    val userId: Int,
    val name: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)
