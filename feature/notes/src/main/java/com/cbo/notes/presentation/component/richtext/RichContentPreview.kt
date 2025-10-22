package com.cbo.notes.presentation.component.richtext

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cbo.notes.domain.model.ContentBlock
import com.cbo.notes.domain.model.NoteContent
import com.cbo.notes.domain.model.TextStyle

/**
 * Displays a preview of rich content in note cards and lists
 */
@Composable
fun RichContentPreview(
    content: NoteContent,
    maxLines: Int = 5,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        content.blocks.take(3).forEach { block ->
            when (block) {
                is ContentBlock.TextBlock -> {
                    if (block.text.isNotBlank()) {
                        Text(
                            text = buildStyledText(block),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = maxLines,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                is ContentBlock.TodoBlock -> {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (block.isChecked) 
                                Icons.Default.CheckBox 
                            else 
                                Icons.Default.CheckBoxOutlineBlank,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (block.isChecked) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = block.text,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (block.isChecked) 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            else 
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                is ContentBlock.ImageBlock -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Image",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = block.description ?: "Image",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }
        }
        
        // Show indicator if there are more blocks
        if (content.blocks.size > 3) {
            Text(
                text = "• • •",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Builds styled text from a TextBlock with formatting
 */
@Composable
private fun buildStyledText(block: ContentBlock.TextBlock) = buildAnnotatedString {
    val text = block.text
    val textLength = text.length

    if (block.styles.isEmpty() || textLength == 0) {
        append(text)
        return@buildAnnotatedString
    }

    // Normalize and sort style ranges
    val normalizedRanges = block.styles
        .mapNotNull { range ->
            val start = range.start.coerceIn(0, textLength)
            val end = range.end.coerceIn(0, textLength)
            if (start < end) range.copy(start = start, end = end) else null
        }
        .sortedBy { it.start }

    var cursor = 0

    normalizedRanges.forEach { styleRange ->
        val start = styleRange.start
        val end = styleRange.end

        // Append unstyled text before this range
        if (cursor < start) {
            append(text.substring(cursor, start))
        }

        // Append styled segment
        withStyle(style = getSpanStyle(styleRange.style)) {
            append(text.substring(start, end))
        }

        // Move cursor
        if (end > cursor) cursor = end
    }

    // Append any remaining unstyled text
    if (cursor < textLength) {
        append(text.substring(cursor, textLength))
    }
}

/**
 * Converts TextStyle to Compose SpanStyle
 */
@Composable
private fun getSpanStyle(style: TextStyle): SpanStyle {
    return when (style) {
        TextStyle.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
        TextStyle.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
        TextStyle.UNDERLINE -> SpanStyle(textDecoration = TextDecoration.Underline)
        TextStyle.STRIKETHROUGH -> SpanStyle(textDecoration = TextDecoration.LineThrough)
    }
}

