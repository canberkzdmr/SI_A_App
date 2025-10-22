package com.cbo.notes.presentation.component.richtext

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cbo.notes.domain.model.ContentBlock
import com.cbo.notes.domain.model.NoteContent
import com.cbo.notes.domain.model.TextStyle
import com.cbo.notes.domain.model.TextStyleRange
import com.cbo.ui.theme.MemCloudApplicationTheme
import androidx.compose.ui.text.AnnotatedString

data class FormattingHandlers(
    val activeStyles: Set<TextStyle>,
    val onToggleStyle: (TextStyle) -> Unit,
    val onAddTodo: () -> Unit
)

/**
 * Improved unified editor - blocks flow together seamlessly with better interactivity
 */
@Composable
fun ImprovedTextEditor(
    content: NoteContent,
    onContentChange: (NoteContent) -> Unit,
    modifier: Modifier = Modifier,
    onProvideFormattingHandlers: ((FormattingHandlers) -> Unit)? = null
) {
    var focusedBlockId by remember { mutableStateOf<String?>(null) }
    var currentSelection by remember { mutableStateOf(TextRange.Zero) }
    var onApplyStyle by remember { mutableStateOf<((TextStyle) -> Unit)?>(null) }
    
    // Track active styles for typing (styles that should apply to new text)
    var activeTypingStyles by remember { mutableStateOf<Set<TextStyle>>(emptySet()) }
    var onGetActiveTypingStyles by remember { mutableStateOf<(() -> Set<TextStyle>)?>(null) }
    var onSetActiveTypingStyles by remember { mutableStateOf<((Set<TextStyle>) -> Unit)?>(null) }
    
    // Get non-image blocks for the unified editor
    val textAndTodoBlocks = content.blocks.filterNot { it is ContentBlock.ImageBlock }
    
    // Get active styles from focused block at current selection or typing styles
    val activeStyles = remember(focusedBlockId, content.blocks, currentSelection, activeTypingStyles) {
        val hasSelection = currentSelection.start != currentSelection.end
        
        if (hasSelection) {
            content.blocks.find { it.id == focusedBlockId }?.let { block ->
                if (block is ContentBlock.TextBlock) {
                    val start = minOf(currentSelection.start, currentSelection.end)
                    val end = maxOf(currentSelection.start, currentSelection.end)
                    
                    block.styles.filter { styleRange ->
                        styleRange.start <= start && styleRange.end >= end
                    }.map { it.style }.toSet()
                } else emptySet()
            } ?: emptySet()
        } else {
            activeTypingStyles
        }
    }
    
    // Provide handlers to parent so toolbar can be rendered outside
    val toggleStyleHandler: (TextStyle) -> Unit = { style ->
        val hasSelection = currentSelection.start != currentSelection.end
        if (hasSelection) {
            onApplyStyle?.invoke(style)
        } else {
            val currentTyping = onGetActiveTypingStyles?.invoke() ?: emptySet()
            val newTyping = if (style in currentTyping) currentTyping - style else currentTyping + style
            onSetActiveTypingStyles?.invoke(newTyping)
            activeTypingStyles = newTyping
        }
    }
    val addTodoHandler: () -> Unit = {
        val newBlock = ContentBlock.TodoBlock(
            id = System.currentTimeMillis().toString(),
            text = "",
            isChecked = false
        )
        onContentChange(content.copy(blocks = content.blocks + newBlock))
        focusedBlockId = newBlock.id
    }
    LaunchedEffect(activeStyles) {
        onProvideFormattingHandlers?.invoke(
            FormattingHandlers(
                activeStyles = activeStyles,
                onToggleStyle = toggleStyleHandler,
                onAddTodo = addTodoHandler
            )
        )
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Render blocks seamlessly
            if (textAndTodoBlocks.isEmpty()) {
                LaunchedEffect(Unit) {
                    val firstBlock = ContentBlock.TextBlock(
                        id = System.currentTimeMillis().toString(),
                        text = ""
                    )
                    onContentChange(content.copy(blocks = listOf(firstBlock)))
                    focusedBlockId = firstBlock.id
                }
                Text(
                    text = "Start typing your note...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            } else {
                textAndTodoBlocks.forEachIndexed { index, block ->
                    when (block) {
                        is ContentBlock.TextBlock -> {
                            CompactTextBlock(
                                block = block,
                                isFocused = block.id == focusedBlockId,
                                onBlockChange = { updatedBlock ->
                                    val blockIndex = content.blocks.indexOf(block)
                                    val newBlocks = content.blocks.toMutableList()
                                    newBlocks[blockIndex] = updatedBlock
                                    onContentChange(content.copy(blocks = newBlocks))
                                },
                                onFocusChanged = { hasFocus ->
                                    if (hasFocus) {
                                        focusedBlockId = block.id
                                    } else {
                                        activeTypingStyles = emptySet()
                                    }
                                },
                                onSelectionChanged = { selection ->
                                    currentSelection = selection
                                },
                                onStyleToggleCallback = { callback ->
                                    onApplyStyle = callback
                                },
                                onActiveTypingStylesCallback = { getCallback, setCallback ->
                                    onGetActiveTypingStyles = getCallback
                                    onSetActiveTypingStyles = setCallback
                                },
                                onEnterPressed = {
                                    val blockIndex = content.blocks.indexOf(block)
                                    val newBlock = ContentBlock.TextBlock(
                                        id = System.currentTimeMillis().toString(),
                                        text = ""
                                    )
                                    val newBlocks = content.blocks.toMutableList()
                                    newBlocks.add(blockIndex + 1, newBlock)
                                    onContentChange(content.copy(blocks = newBlocks))
                                    focusedBlockId = newBlock.id
                                },
                                onDeleteEmpty = {
                                    if (textAndTodoBlocks.size > 1) {
                                        val blockIndex = content.blocks.indexOf(block)
                                        val newBlocks = content.blocks.toMutableList()
                                        newBlocks.removeAt(blockIndex)
                                        onContentChange(content.copy(blocks = newBlocks))
                                        if (index > 0) {
                                            focusedBlockId = textAndTodoBlocks[index - 1].id
                                        }
                                    }
                                }
                            )
                        }
                        is ContentBlock.TodoBlock -> {
                            CompactTodoBlock(
                                block = block,
                                isFocused = block.id == focusedBlockId,
                                onBlockChange = { updatedBlock ->
                                    val blockIndex = content.blocks.indexOf(block)
                                    val newBlocks = content.blocks.toMutableList()
                                    newBlocks[blockIndex] = updatedBlock
                                    onContentChange(content.copy(blocks = newBlocks))
                                },
                                onFocusChanged = { hasFocus ->
                                    if (hasFocus) focusedBlockId = block.id
                                },
                                onEnterPressed = {
                                    val blockIndex = content.blocks.indexOf(block)
                                    val newBlocks = content.blocks.toMutableList()
                                    
                                    if (block.text.isEmpty()) {
                                        val newBlock = ContentBlock.TextBlock(
                                            id = System.currentTimeMillis().toString(),
                                            text = ""
                                        )
                                        newBlocks[blockIndex] = newBlock
                                        onContentChange(content.copy(blocks = newBlocks))
                                        focusedBlockId = newBlock.id
                                    } else {
                                        val newBlock = ContentBlock.TodoBlock(
                                            id = System.currentTimeMillis().toString(),
                                            text = "",
                                            isChecked = false
                                        )
                                        newBlocks.add(blockIndex + 1, newBlock)
                                        onContentChange(content.copy(blocks = newBlocks))
                                        focusedBlockId = newBlock.id
                                    }
                                },
                                onDeleteEmpty = {
                                    if (textAndTodoBlocks.size > 1) {
                                        val blockIndex = content.blocks.indexOf(block)
                                        val newBlocks = content.blocks.toMutableList()
                                        newBlocks.removeAt(blockIndex)
                                        onContentChange(content.copy(blocks = newBlocks))
                                        if (index > 0) {
                                            focusedBlockId = textAndTodoBlocks[index - 1].id
                                        }
                                    }
                                }
                            )
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

/**
 * Compact text block without card borders
 */
@Composable
private fun CompactTextBlock(
    block: ContentBlock.TextBlock,
    isFocused: Boolean,
    onBlockChange: (ContentBlock.TextBlock) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    onSelectionChanged: (TextRange) -> Unit,
    onStyleToggleCallback: ((TextStyle) -> Unit) -> Unit,
    onActiveTypingStylesCallback: (() -> Set<TextStyle>, (Set<TextStyle>) -> Unit) -> Unit,
    onEnterPressed: () -> Unit,
    onDeleteEmpty: () -> Unit
) {
    var textFieldValue by remember(block.id) {
        mutableStateOf(TextFieldValue(text = block.text, selection = TextRange(block.text.length)))
    }
    val focusRequester = remember { FocusRequester() }
    
    // Store current text field value in a ref so callback can access latest value
    val textFieldValueRef = remember { mutableStateOf(textFieldValue) }
    textFieldValueRef.value = textFieldValue
    
    // Store current block styles in a ref too
    val currentStylesRef = remember { mutableStateOf(block.styles) }
    currentStylesRef.value = block.styles
    
    // Track active typing styles (styles to apply to new text)
    var activeTypingStyles by remember { mutableStateOf<Set<TextStyle>>(emptySet()) }
    
    // Track last text length to detect new text being typed
    var lastTextLength by remember { mutableStateOf(block.text.length) }
    
    // Track last selection to detect if user moved cursor vs typed text
    var lastSelection by remember { mutableStateOf(TextRange.Zero) }
    
    // Track where each active style range started (position in text)
    val activeRangeStarts = remember { mutableStateMapOf<TextStyle, Int>() }
    
    // Sync text changes and apply active typing styles to new text
    LaunchedEffect(textFieldValue.text) {
        if (textFieldValue.text != block.text) {
            val newText = textFieldValue.text
            
            // Only process if text was added (not deleted)
            if (newText.length > lastTextLength && activeTypingStyles.isNotEmpty()) {
                val cursor = textFieldValue.selection.start
                
                // Build new style ranges
                val updatedStyles = block.styles.toMutableList()
                
                // For each active style
                activeTypingStyles.forEach { style ->
                    val rangeStart = activeRangeStarts[style]
                    
                    if (rangeStart != null) {
                        // Find and update the existing range for this style
                        val rangeIndex = updatedStyles.indexOfFirst { 
                            it.style == style && it.start == rangeStart 
                        }
                        
                        if (rangeIndex >= 0) {
                            // Extend the range to cursor position
                            updatedStyles[rangeIndex] = updatedStyles[rangeIndex].copy(end = cursor)
                        } else {
                            // Range not found, shouldn't happen but handle it
                            updatedStyles.add(TextStyleRange(rangeStart, cursor, style))
                        }
                    } else {
                        // This is the first character with this style, start a new range
                        val startPos = lastTextLength
                        activeRangeStarts[style] = startPos
                        updatedStyles.add(TextStyleRange(startPos, cursor, style))
                    }
                }
                
                // Update with new styles
                onBlockChange(block.copy(text = newText, styles = updatedStyles))
            } else {
                // Text was deleted or no change, just update text
                onBlockChange(block.copy(text = newText, styles = block.styles))
            }
            
            lastTextLength = newText.length
        }
    }
    
    // Sync selection changes
    LaunchedEffect(textFieldValue.selection) {
        onSelectionChanged(textFieldValue.selection)
        lastSelection = textFieldValue.selection
    }
    
    // Provide active typing styles callbacks
    LaunchedEffect(block.id) {
        onActiveTypingStylesCallback(
            { activeTypingStyles },
            { newStyles -> 
                // When styles change, manage active ranges
                val removedStyles = activeTypingStyles - newStyles
                val addedStyles = newStyles - activeTypingStyles
                
                // Clear tracking for disabled styles
                removedStyles.forEach { style ->
                    activeRangeStarts.remove(style)
                }
                
                // Clear tracking for newly enabled styles (they'll start fresh)
                addedStyles.forEach { style ->
                    activeRangeStarts.remove(style)
                }
                
                activeTypingStyles = newStyles
            }
        )
    }
    
    // Provide style toggle callback - use ref to get current selection and styles
    LaunchedEffect(block.id) {
        onStyleToggleCallback { style ->
            val currentValue = textFieldValueRef.value
            val currentStyles = currentStylesRef.value
            val start = minOf(currentValue.selection.start, currentValue.selection.end)
            val end = maxOf(currentValue.selection.start, currentValue.selection.end)
            
            // If no selection, apply to entire text
            val actualStart = if (start == end) 0 else start
            val actualEnd = if (start == end) currentValue.text.length else end
            
            if (actualEnd > actualStart && currentValue.text.isNotEmpty()) {
                // Check if this exact range already has this exact style
                val existingStyleRange = currentStyles.find { styleRange ->
                    styleRange.style == style &&
                    styleRange.start == actualStart &&
                    styleRange.end == actualEnd
                }
                
                val newStyles = if (existingStyleRange != null) {
                    // Remove this exact style range - keep all other styles
                    currentStyles.filterNot { it === existingStyleRange }
                } else {
                    // Add new style range to existing styles
                    currentStyles + TextStyleRange(actualStart, actualEnd, style)
                }
                
                // Update with new styles, preserve text
                onBlockChange(block.copy(text = currentValue.text, styles = newStyles))
            }
        }
    }
    
    // Request focus when needed
    LaunchedEffect(isFocused) {
        if (isFocused) {
            focusRequester.requestFocus()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Always render BasicTextField to keep FocusRequester attached
        BasicTextField(
            value = textFieldValue,
            onValueChange = { textFieldValue = it },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { onFocusChanged(it.isFocused) }
                .onKeyEvent { keyEvent ->
                    when {
                        keyEvent.type == KeyEventType.KeyDown &&
                        (keyEvent.key == Key.Enter || keyEvent.key == Key.NumPadEnter) -> {
                            onEnterPressed()
                            true
                        }
                        keyEvent.type == KeyEventType.KeyDown &&
                        keyEvent.key == Key.Backspace &&
                        textFieldValue.text.isEmpty() -> {
                            onDeleteEmpty()
                            true
                        }
                        else -> false
                    }
                },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            visualTransformation = if (block.styles.isNotEmpty()) {
                // Apply visual transformation to show styled text
                StyledTextVisualTransformation(block.styles)
            } else {
                androidx.compose.ui.text.input.VisualTransformation.None
            }
        )
    }
}

/**
 * Compact to-do block without card borders
 */
@Composable
private fun CompactTodoBlock(
    block: ContentBlock.TodoBlock,
    isFocused: Boolean,
    onBlockChange: (ContentBlock.TodoBlock) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    onEnterPressed: () -> Unit,
    onDeleteEmpty: () -> Unit
) {
    var text by remember(block.id) { mutableStateOf(block.text) }
    val focusRequester = remember { FocusRequester() }
    
    // Sync text changes
    LaunchedEffect(text) {
        if (text != block.text) {
            onBlockChange(block.copy(text = text))
        }
    }
    
    // Request focus when needed
    LaunchedEffect(isFocused) {
        if (isFocused) {
            focusRequester.requestFocus()
        }
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Interactive checkbox
        Checkbox(
            checked = block.isChecked,
            onCheckedChange = { checked ->
                onBlockChange(block.copy(isChecked = checked))
            },
            modifier = Modifier.size(24.dp)
        )
        
        // Text field (single line only)
        BasicTextField(
            value = text,
            onValueChange = { newText ->
                // Force single line - replace newlines with spaces
                text = newText.replace("\n", " ")
            },
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .onFocusChanged { onFocusChanged(it.isFocused) }
                .onKeyEvent { keyEvent ->
                    when {
                        keyEvent.type == KeyEventType.KeyDown &&
                        (keyEvent.key == Key.Enter || keyEvent.key == Key.NumPadEnter) -> {
                            onEnterPressed()
                            true
                        }
                        keyEvent.type == KeyEventType.KeyDown &&
                        keyEvent.key == Key.Backspace &&
                        text.isEmpty() -> {
                            onDeleteEmpty()
                            true
                        }
                        else -> false
                    }
                },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { onEnterPressed() }),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = if (block.isChecked)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                else
                    MaterialTheme.colorScheme.onSurface,
                textDecoration = if (block.isChecked) TextDecoration.LineThrough else null
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
        )
    }
}

/**
 * Improved formatting toolbar with active state indicators
 */
@Composable
fun ImprovedFormattingToolbar(
    activeStyles: Set<TextStyle>,
    onToggleStyle: (TextStyle) -> Unit,
    onAddTodo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // To-do
        IconButton(onClick = onAddTodo) {
            Icon(Icons.Default.CheckBox, "Add todo")
        }
        
        VerticalDivider(modifier = Modifier.height(36.dp))
        
        // Bold
        FormattingIconButton(
            icon = Icons.Default.FormatBold,
            contentDescription = "Bold",
            isActive = TextStyle.BOLD in activeStyles,
            onClick = { onToggleStyle(TextStyle.BOLD) }
        )
        
        // Italic
        FormattingIconButton(
            icon = Icons.Default.FormatItalic,
            contentDescription = "Italic",
            isActive = TextStyle.ITALIC in activeStyles,
            onClick = { onToggleStyle(TextStyle.ITALIC) }
        )
        
        // Underline
        FormattingIconButton(
            icon = Icons.Default.FormatUnderlined,
            contentDescription = "Underline",
            isActive = TextStyle.UNDERLINE in activeStyles,
            onClick = { onToggleStyle(TextStyle.UNDERLINE) }
        )
        
        // Strikethrough
        FormattingIconButton(
            icon = Icons.Default.FormatStrikethrough,
            contentDescription = "Strikethrough",
            isActive = TextStyle.STRIKETHROUGH in activeStyles,
            onClick = { onToggleStyle(TextStyle.STRIKETHROUGH) }
        )
    }
}

/**
 * Icon button with active state highlighting
 */
@Composable
private fun FormattingIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .background(
                color = if (isActive) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.small
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isActive)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Build styled text for display
 */
@Composable
private fun buildStyledText(text: String, styles: List<TextStyleRange>) = buildAnnotatedString {
    if (styles.isEmpty()) {
        append(text)
    } else {
        // Create character map for styles
        val styleMap = mutableMapOf<Int, MutableList<TextStyle>>()
        styles.forEach { styleRange ->
            for (i in styleRange.start until minOf(styleRange.end, text.length)) {
                styleMap.getOrPut(i) { mutableListOf() }.add(styleRange.style)
            }
        }
        
        // Build annotated string
        var currentIndex = 0
        while (currentIndex < text.length) {
            val currentStyles = styleMap[currentIndex] ?: emptyList()
            var endIndex = currentIndex + 1
            
            while (endIndex < text.length && (styleMap[endIndex] ?: emptyList()) == currentStyles) {
                endIndex++
            }
            
            if (currentStyles.isNotEmpty()) {
                withStyle(style = combineStyles(currentStyles)) {
                    append(text.substring(currentIndex, endIndex))
                }
            } else {
                append(text.substring(currentIndex, endIndex))
            }
            
            currentIndex = endIndex
        }
    }
}

/**
 * Combine multiple styles
 */
@Composable
private fun combineStyles(styles: List<TextStyle>): SpanStyle {
    var spanStyle = SpanStyle()
    styles.forEach { style ->
        spanStyle = when (style) {
            TextStyle.BOLD -> spanStyle.copy(fontWeight = FontWeight.Bold)
            TextStyle.ITALIC -> spanStyle.copy(fontStyle = FontStyle.Italic)
            TextStyle.UNDERLINE -> spanStyle.copy(
                textDecoration = spanStyle.textDecoration?.plus(TextDecoration.Underline) ?: TextDecoration.Underline
            )
            TextStyle.STRIKETHROUGH -> spanStyle.copy(
                textDecoration = spanStyle.textDecoration?.plus(TextDecoration.LineThrough) ?: TextDecoration.LineThrough
            )
        }
    }
    return spanStyle
}

/**
 * Visual transformation that applies text styles
 */
private class StyledTextVisualTransformation(
    private val styles: List<TextStyleRange>
) : androidx.compose.ui.text.input.VisualTransformation {
    
    override fun filter(text: AnnotatedString): androidx.compose.ui.text.input.TransformedText {
        // Build styled text
        val styledText = buildAnnotatedString {
            append(text.text)
            
            // Apply each style range
            styles.forEach { styleRange ->
                val start = styleRange.start.coerceIn(0, text.text.length)
                val end = styleRange.end.coerceIn(0, text.text.length)
                
                if (start < end) {
                    val spanStyle = when (styleRange.style) {
                        TextStyle.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
                        TextStyle.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
                        TextStyle.UNDERLINE -> SpanStyle(textDecoration = TextDecoration.Underline)
                        TextStyle.STRIKETHROUGH -> SpanStyle(textDecoration = TextDecoration.LineThrough)
                    }
                    
                    addStyle(spanStyle, start, end)
                }
            }
        }
        
        return androidx.compose.ui.text.input.TransformedText(
            styledText,
            androidx.compose.ui.text.input.OffsetMapping.Identity
        )
    }
}

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview(showBackground = true, name = "Empty Editor")
@Composable
private fun ImprovedTextEditorEmptyPreview() {
    MemCloudApplicationTheme {
        ImprovedTextEditor(
            content = NoteContent(emptyList()),
            onContentChange = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Simple Text")
@Composable
private fun ImprovedTextEditorSimpleTextPreview() {
    MemCloudApplicationTheme {
        ImprovedTextEditor(
            content = NoteContent(
                blocks = listOf(
                    ContentBlock.TextBlock(
                        id = "1",
                        text = "This is a simple text block."
                    )
                )
            ),
            onContentChange = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Styled Text")
@Composable
private fun ImprovedTextEditorStyledTextPreview() {
    MemCloudApplicationTheme {
        ImprovedTextEditor(
            content = NoteContent(
                blocks = listOf(
                    ContentBlock.TextBlock(
                        id = "1",
                        text = "This text is bold, italic, and underlined.",
                        styles = listOf(
                            TextStyleRange(13, 17, TextStyle.BOLD),
                            TextStyleRange(19, 25, TextStyle.ITALIC),
                            TextStyleRange(31, 41, TextStyle.UNDERLINE)
                        )
                    )
                )
            ),
            onContentChange = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Todo List")
@Composable
private fun ImprovedTextEditorTodoListPreview() {
    MemCloudApplicationTheme {
        ImprovedTextEditor(
            content = NoteContent(
                blocks = listOf(
                    ContentBlock.TodoBlock("1", "Buy milk", true),
                    ContentBlock.TodoBlock("2", "Buy eggs", true),
                    ContentBlock.TodoBlock("3", "Buy bread", false)
                )
            ),
            onContentChange = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Mixed Content")
@Composable
private fun ImprovedTextEditorMixedContentPreview() {
    MemCloudApplicationTheme {
        ImprovedTextEditor(
            content = NoteContent(
                blocks = listOf(
                    ContentBlock.TextBlock(
                        id = "1",
                        text = "Meeting Notes",
                        styles = listOf(TextStyleRange(0, 13, TextStyle.BOLD))
                    ),
                    ContentBlock.TextBlock(
                        id = "2",
                        text = "Attendees: John, Jane, and Bob"
                    ),
                    ContentBlock.TodoBlock("3", "Discuss Q4 roadmap", true),
                    ContentBlock.TodoBlock("4", "Review budget", false),
                    ContentBlock.TextBlock(
                        id = "5",
                        text = "Action items are important!",
                        styles = listOf(TextStyleRange(16, 25, TextStyle.ITALIC))
                    )
                )
            ),
            onContentChange = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

