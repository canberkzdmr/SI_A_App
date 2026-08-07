package com.cbo.notes.domain.model

import java.util.UUID

/**
 * Represents a single todo/checklist item attached to a Note.
 */
data class TodoItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isDone: Boolean = false
)
