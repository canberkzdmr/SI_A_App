package com.cbo.notes.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents the structured content of a note with rich text, todos, and images
 */
@Serializable
data class NoteContent(
    val blocks: List<ContentBlock> = emptyList()
) {
    companion object {
        /**
         * Creates a NoteContent from plain text (for backward compatibility)
         */
        fun fromPlainText(text: String): NoteContent {
            if (text.isEmpty()) return NoteContent(emptyList())
            return NoteContent(
                blocks = listOf(
                    ContentBlock.TextBlock(
                        id = generateBlockId(),
                        text = text,
                        styles = emptyList()
                    )
                )
            )
        }

        /**
         * Converts NoteContent to plain text (for previews and search)
         */
        fun NoteContent.toPlainText(): String {
            return blocks.joinToString("\n") { block ->
                when (block) {
                    is ContentBlock.TextBlock -> block.text
                    is ContentBlock.TodoBlock -> "${if (block.isChecked) "☑" else "☐"} ${block.text}"
                    is ContentBlock.ImageBlock -> "[Image: ${block.description ?: ""}]"
                }
            }
        }

        private fun generateBlockId(): String = System.currentTimeMillis().toString()
    }
}

/**
 * Represents different types of content blocks
 */
@Serializable
sealed class ContentBlock {
    abstract val id: String

    @Serializable
    data class TextBlock(
        override val id: String,
        val text: String,
        val styles: List<TextStyleRange> = emptyList()
    ) : ContentBlock()

    @Serializable
    data class TodoBlock(
        override val id: String,
        val text: String,
        val isChecked: Boolean = false,
        val styles: List<TextStyleRange> = emptyList()
    ) : ContentBlock()

    @Serializable
    data class ImageBlock(
        override val id: String,
        val imageUri: String,
        val description: String? = null
    ) : ContentBlock()
}

/**
 * Represents a style applied to a range of text
 */
@Serializable
data class TextStyleRange(
    val start: Int,
    val end: Int,
    val style: TextStyle
)

/**
 * Types of text styling
 */
@Serializable
enum class TextStyle {
    BOLD,
    ITALIC,
    UNDERLINE,
    STRIKETHROUGH
}

