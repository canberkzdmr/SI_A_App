package com.cbo.notes.domain.model

data class Note(
    val id: Int = 0,
    val userId: Int,
    val title: String,
    val content: String, // For backward compatibility and search
    val richContent: NoteContent? = null, // New structured content
    val category: Category? = null,
    val tags: List<Tag> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isFavorite: Boolean = false
) {
    /**
     * Gets the display content - prefers rich content if available
     */
    fun getDisplayContent(): NoteContent {
        return richContent ?: NoteContent.fromPlainText(content)
    }
}
