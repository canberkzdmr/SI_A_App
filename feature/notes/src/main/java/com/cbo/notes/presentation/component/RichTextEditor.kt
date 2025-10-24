package com.cbo.notes.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.model.RichTextState
import androidx.compose.ui.tooling.preview.Preview
import com.cbo.ui.theme.MemCloudApplicationTheme

/**
 * Rich Text Editor component with markdown-style formatting toolbar
 */
@Composable
fun RichTextEditor(
    state: RichTextState,
    modifier: Modifier = Modifier,
    placeholder: String = "Start writing...",
    label: String? = null,
    minHeight: Int = 200
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Label
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Formatting Toolbar
        FormattingToolbar(state = state)

        // Rich Text Editor Field
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        ) {
            Box(
                modifier = Modifier
                    .padding()
                    .fillMaxWidth()
            ) {
                RichTextEditor(
                    state = state,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    placeholder = {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                )
            }
        }
    }
}

/**
 * Formatting toolbar with markdown-style controls
 */
@Composable
private fun FormattingToolbar(
    state: RichTextState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Text Style Controls
            FormatButton(
                icon = Icons.Default.FormatBold,
                tooltip = "Bold",
                isSelected = state.currentSpanStyle.fontWeight == FontWeight.Bold,
                onClick = { state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) }
            )

            FormatButton(
                icon = Icons.Default.FormatItalic,
                tooltip = "Italic",
                isSelected = state.currentSpanStyle.fontStyle == FontStyle.Italic,
                onClick = { state.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic)) }
            )

            FormatButton(
                icon = Icons.Default.FormatUnderlined,
                tooltip = "Underline",
                isSelected = state.currentSpanStyle.textDecoration?.contains(TextDecoration.Underline) == true,
                onClick = { state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline)) }
            )

            FormatButton(
                icon = Icons.Default.FormatStrikethrough,
                tooltip = "Strikethrough",
                isSelected = state.currentSpanStyle.textDecoration?.contains(TextDecoration.LineThrough) == true,
                onClick = { state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) }
            )

            VerticalDivider(
                modifier = Modifier
                    .height(24.dp)
                    .padding(horizontal = 4.dp)
            )

            // Heading Controls
            FormatButton(
                icon = Icons.Default.Title,
                tooltip = "Heading 1",
                isSelected = state.currentSpanStyle.fontSize == 32.sp,
                onClick = {
                    state.toggleSpanStyle(
                        SpanStyle(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            )

            FormatButton(
                icon = Icons.Default.TextFields,
                tooltip = "Heading 2",
                isSelected = state.currentSpanStyle.fontSize == 24.sp,
                onClick = {
                    state.toggleSpanStyle(
                        SpanStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            )

            VerticalDivider(
                modifier = Modifier
                    .height(24.dp)
                    .padding(horizontal = 4.dp)
            )

            // List Controls
            FormatButton(
                icon = Icons.Default.FormatListBulleted,
                tooltip = "Bullet List",
                isSelected = state.isUnorderedList,
                onClick = { state.toggleUnorderedList() }
            )

            FormatButton(
                icon = Icons.Default.FormatListNumbered,
                tooltip = "Numbered List",
                isSelected = state.isOrderedList,
                onClick = { state.toggleOrderedList() }
            )

            VerticalDivider(
                modifier = Modifier
                    .height(24.dp)
                    .padding(horizontal = 4.dp)
            )

            // Code Block
            FormatButton(
                icon = Icons.Default.Code,
                tooltip = "Code Block",
                isSelected = state.isCodeSpan,
                onClick = { state.toggleCodeSpan() }
            )

            VerticalDivider(
                modifier = Modifier
                    .height(24.dp)
                    .padding(horizontal = 4.dp)
            )

            // Alignment Controls
            FormatButton(
                icon = Icons.Default.FormatAlignLeft,
                tooltip = "Align Left",
                isSelected = state.currentParagraphStyle.textAlign == TextAlign.Left,
                onClick = {
                    state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Left))
                }
            )

            FormatButton(
                icon = Icons.Default.FormatAlignCenter,
                tooltip = "Align Center",
                isSelected = state.currentParagraphStyle.textAlign == TextAlign.Center,
                onClick = {
                    state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Center))
                }
            )

            FormatButton(
                icon = Icons.Default.FormatAlignRight,
                tooltip = "Align Right",
                isSelected = state.currentParagraphStyle.textAlign == TextAlign.Right,
                onClick = {
                    state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Right))
                }
            )

            VerticalDivider(
                modifier = Modifier
                    .height(24.dp)
                    .padding(horizontal = 4.dp)
            )

            // Clear Formatting
            FormatButton(
                icon = Icons.Default.FormatClear,
                tooltip = "Clear Formatting",
                isSelected = false,
                onClick = {
                    state.removeSpanStyle(state.currentSpanStyle)
                    state.removeParagraphStyle(state.currentParagraphStyle)
                }
            )
        }
    }
}

/**
 * Individual format button in the toolbar
 */
@Composable
private fun FormatButton(
    icon: ImageVector,
    tooltip: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(36.dp)
            .then(
                if (isSelected) {
                    Modifier.background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    )
                } else {
                    Modifier
                }
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tooltip,
            modifier = Modifier.size(20.dp),
            tint = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

/**
 * Vertical divider for toolbar sections
 */
@Composable
private fun VerticalDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

// Previews
@Preview(showBackground = true, name = "Rich Text Editor - Empty")
@Composable
private fun RichTextEditorEmptyPreview() {
    MemCloudApplicationTheme {
        val state = rememberRichTextState()
        Column(modifier = Modifier.padding(16.dp)) {
            RichTextEditor(
                state = state,
                modifier = Modifier.fillMaxWidth(),
                label = "Content",
                placeholder = "Start writing your note with rich formatting...",
                minHeight = 300
            )
        }
    }
}

@Preview(showBackground = true, name = "Rich Text Editor - With Content")
@Composable
private fun RichTextEditorWithContentPreview() {
    MemCloudApplicationTheme {
        val state = rememberRichTextState()
        LaunchedEffect(Unit) {
            state.setHtml("<h1>Meeting Notes</h1><p>This is a <strong>bold</strong> statement and <em>italic text</em>.</p><ul><li>First item</li><li>Second item</li></ul>")
        }
        Column(modifier = Modifier.padding(16.dp)) {
            RichTextEditor(
                state = state,
                modifier = Modifier.fillMaxWidth(),
                label = "Content",
                placeholder = "Start writing...",
                minHeight = 300
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, name = "Formatting Toolbar")
@Composable
private fun FormattingToolbarPreview() {
    MemCloudApplicationTheme {
        val state = rememberRichTextState()
        Surface(
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            FormattingToolbar(state = state)
        }
    }
}

