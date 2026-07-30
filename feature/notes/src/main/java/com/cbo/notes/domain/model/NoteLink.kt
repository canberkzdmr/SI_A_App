package com.cbo.notes.domain.model

data class NoteLink(
    val id: Int = 0,
    val sourceNoteId: Int,
    val targetNoteId: Int,
    val createdAt: Long = System.currentTimeMillis()
)
