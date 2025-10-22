package com.cbo.notes.presentation.component.richtext

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cbo.notes.domain.model.ContentBlock
import com.cbo.notes.domain.model.NoteContent
import com.cbo.notes.domain.model.TextStyle
import com.cbo.notes.domain.model.TextStyleRange
import com.cbo.ui.theme.MemCloudApplicationTheme
import com.cbo.notes.presentation.component.richtext.FormattingHandlers

/**
 * Main rich text editor component that supports text formatting, todos, and images
 * Uses a unified text editor for better UX with text and todos in one block
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RichTextEditor(
    content: NoteContent,
    onContentChange: (NoteContent) -> Unit,
    modifier: Modifier = Modifier,
    onProvideFormattingHandlers: ((FormattingHandlers) -> Unit)? = null
) {
    val listState = rememberLazyListState()
    
    Column(modifier = modifier) {
        // Simple Toolbar - Just for adding images
        RichTextToolbar(
            onAddImageBlock = {
                val newBlock = ContentBlock.ImageBlock(
                    id = System.currentTimeMillis().toString(),
                    imageUri = "" // Empty, will be set by ImageBlockEditor
                )
                onContentChange(content.copy(blocks = content.blocks + newBlock))
            },
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider()
        
        // Content - Improved text editor + images
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Improved text editor for all text and todos
            item {
                ImprovedTextEditor(
                    content = content,
                    onContentChange = onContentChange,
                    modifier = Modifier.fillMaxWidth(),
                    onProvideFormattingHandlers = onProvideFormattingHandlers
                )
            }
            
            // Separate image blocks
            val imageBlocks = content.blocks.filterIsInstance<ContentBlock.ImageBlock>()
            itemsIndexed(
                items = imageBlocks,
                key = { _, block -> block.id }
            ) { _, block ->
                val blockIndex = content.blocks.indexOf(block)
                ImageBlockEditor(
                    block = block,
                    onBlockChange = { updatedBlock ->
                        val newBlocks = content.blocks.toMutableList()
                        newBlocks[blockIndex] = updatedBlock
                        onContentChange(content.copy(blocks = newBlocks))
                    },
                    onDeleteBlock = {
                        val newBlocks = content.blocks.toMutableList()
                        newBlocks.removeAt(blockIndex)
                        onContentChange(content.copy(blocks = newBlocks))
                    }
                )
            }
        }
    }
}

/**
 * Simplified toolbar - just for adding images
 */
@Composable
private fun RichTextToolbar(
    onAddImageBlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image block
            IconButton(onClick = onAddImageBlock) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Add image"
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Info text
            Text(
                text = "Use toolbar in text area for formatting & todos",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            )
        }
    }
}

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview(showBackground = true, name = "Empty Editor")
@Composable
private fun RichTextEditorEmptyPreview() {
    MemCloudApplicationTheme {
        RichTextEditor(
            content = NoteContent(emptyList()),
            onContentChange = {},
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Simple Text")
@Composable
private fun RichTextEditorSimpleTextPreview() {
    MemCloudApplicationTheme {
        RichTextEditor(
            content = NoteContent(
                blocks = listOf(
                    ContentBlock.TextBlock(
                        id = "1",
                        text = "This is a simple text block without any formatting."
                    )
                )
            ),
            onContentChange = {},
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Formatted Text")
@Composable
private fun RichTextEditorFormattedTextPreview() {
    MemCloudApplicationTheme {
        RichTextEditor(
            content = NoteContent(
                blocks = listOf(
                    ContentBlock.TextBlock(
                        id = "1",
                        text = "This text has bold, italic, and underline formatting!",
                        styles = listOf(
                            TextStyleRange(0, 9, TextStyle.BOLD),
                            TextStyleRange(14, 18, TextStyle.ITALIC),
                            TextStyleRange(20, 26, TextStyle.BOLD),
                            TextStyleRange(28, 34, TextStyle.ITALIC),
                            TextStyleRange(40, 49, TextStyle.UNDERLINE)
                        )
                    )
                )
            ),
            onContentChange = {},
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Todo List")
@Composable
private fun RichTextEditorTodoListPreview() {
    MemCloudApplicationTheme {
        RichTextEditor(
            content = NoteContent(
                blocks = listOf(
                    ContentBlock.TextBlock(
                        id = "1",
                        text = "Shopping List:",
                        styles = listOf(TextStyleRange(0, 14, TextStyle.BOLD))
                    ),
                    ContentBlock.TodoBlock(
                        id = "2",
                        text = "Buy milk",
                        isChecked = true
                    ),
                    ContentBlock.TodoBlock(
                        id = "3",
                        text = "Buy eggs",
                        isChecked = true
                    ),
                    ContentBlock.TodoBlock(
                        id = "4",
                        text = "Buy bread",
                        isChecked = false
                    ),
                    ContentBlock.TodoBlock(
                        id = "5",
                        text = "Buy coffee",
                        isChecked = false
                    )
                )
            ),
            onContentChange = {},
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Mixed Content")
@Composable
private fun RichTextEditorMixedContentPreview() {
    MemCloudApplicationTheme {
        RichTextEditor(
            content = NoteContent(
                blocks = listOf(
                    ContentBlock.TextBlock(
                        id = "1",
                        text = "Meeting Notes - Q4 Planning",
                        styles = listOf(TextStyleRange(0, 28, TextStyle.BOLD))
                    ),
                    ContentBlock.TextBlock(
                        id = "2",
                        text = "Discussed the following items with the team:"
                    ),
                    ContentBlock.TodoBlock(
                        id = "3",
                        text = "Finalize product roadmap",
                        isChecked = true
                    ),
                    ContentBlock.TodoBlock(
                        id = "4",
                        text = "Review budget allocation",
                        isChecked = false
                    ),
                    ContentBlock.TodoBlock(
                        id = "5",
                        text = "Schedule design review",
                        isChecked = false
                    ),
                    ContentBlock.TextBlock(
                        id = "6",
                        text = "Action Items:",
                        styles = listOf(TextStyleRange(0, 13, TextStyle.BOLD))
                    ),
                    ContentBlock.TextBlock(
                        id = "7",
                        text = "John will prepare the technical specs by Friday. Sarah will coordinate with the design team for the new features.",
                        styles = listOf(
                            TextStyleRange(0, 4, TextStyle.ITALIC),
                            TextStyleRange(54, 60, TextStyle.UNDERLINE),
                            TextStyleRange(62, 67, TextStyle.ITALIC)
                        )
                    )
                )
            ),
            onContentChange = {},
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "With Images")
@Composable
private fun RichTextEditorWithImagesPreview() {
    MemCloudApplicationTheme {
        RichTextEditor(
            content = NoteContent(
                blocks = listOf(
                    ContentBlock.TextBlock(
                        id = "1",
                        text = "Travel Photos",
                        styles = listOf(TextStyleRange(0, 13, TextStyle.BOLD))
                    ),
                    ContentBlock.ImageBlock(
                        id = "2",
                        imageUri = "",
                        description = "Sunset at the beach"
                    ),
                    ContentBlock.TextBlock(
                        id = "3",
                        text = "What an amazing day! The sunset was absolutely beautiful."
                    ),
                    ContentBlock.ImageBlock(
                        id = "4",
                        imageUri = "",
                        description = "Mountain view"
                    )
                )
            ),
            onContentChange = {},
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "All Styles Combined")
@Composable
private fun RichTextEditorAllStylesPreview() {
    MemCloudApplicationTheme {
        RichTextEditor(
            content = NoteContent(
                blocks = listOf(
                    ContentBlock.TextBlock(
                        id = "1",
                        text = "Bold text",
                        styles = listOf(TextStyleRange(0, 9, TextStyle.BOLD))
                    ),
                    ContentBlock.TextBlock(
                        id = "2",
                        text = "Italic text",
                        styles = listOf(TextStyleRange(0, 11, TextStyle.ITALIC))
                    ),
                    ContentBlock.TextBlock(
                        id = "3",
                        text = "Underlined text",
                        styles = listOf(TextStyleRange(0, 15, TextStyle.UNDERLINE))
                    ),
                    ContentBlock.TextBlock(
                        id = "4",
                        text = "Strikethrough text",
                        styles = listOf(TextStyleRange(0, 18, TextStyle.STRIKETHROUGH))
                    ),
                    ContentBlock.TextBlock(
                        id = "5",
                        text = "Bold and italic combined!",
                        styles = listOf(
                            TextStyleRange(0, 25, TextStyle.BOLD),
                            TextStyleRange(0, 25, TextStyle.ITALIC)
                        )
                    )
                )
            ),
            onContentChange = {},
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Full Screen")
@Composable
private fun RichTextEditorFullScreenPreview() {
    MemCloudApplicationTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            RichTextEditor(
                content = NoteContent(
                    blocks = listOf(
                        ContentBlock.TextBlock(
                            id = "1",
                            text = "Project Plan",
                            styles = listOf(TextStyleRange(0, 12, TextStyle.BOLD))
                        ),
                        ContentBlock.TextBlock(
                            id = "2",
                            text = "Phase 1: Research and Planning"
                        ),
                        ContentBlock.TodoBlock(
                            id = "3",
                            text = "Market research",
                            isChecked = true
                        ),
                        ContentBlock.TodoBlock(
                            id = "4",
                            text = "Competitor analysis",
                            isChecked = true
                        ),
                        ContentBlock.TodoBlock(
                            id = "5",
                            text = "Define requirements",
                            isChecked = false
                        ),
                        ContentBlock.TextBlock(
                            id = "6",
                            text = "Phase 2: Design",
                            styles = listOf(TextStyleRange(0, 15, TextStyle.BOLD))
                        ),
                        ContentBlock.TodoBlock(
                            id = "7",
                            text = "Create wireframes",
                            isChecked = false
                        ),
                        ContentBlock.TodoBlock(
                            id = "8",
                            text = "Design mockups",
                            isChecked = false
                        ),
                        ContentBlock.TextBlock(
                            id = "9",
                            text = "Notes: Remember to get feedback from the design team before finalizing.",
                            styles = listOf(TextStyleRange(0, 6, TextStyle.ITALIC))
                        )
                    )
                ),
                onContentChange = {},
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }
    }
}

