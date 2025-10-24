package com.cbo.notes.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText
import com.cbo.ui.theme.MemCloudApplicationTheme

/**
 * Rich Text Viewer component for displaying formatted text content
 */
@Composable
fun RichTextViewer(
    html: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    val state = rememberRichTextState()
    
    LaunchedEffect(html) {
        state.setHtml(html)
    }

    SelectionContainer {
        RichText(
            state = state,
            modifier = modifier,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            maxLines = maxLines,
            overflow = overflow
        )
    }
}

/**
 * Rich Text Viewer for previews (with plain text fallback for empty HTML)
 */
@Composable
fun RichTextPreview(
    html: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 3,
    overflow: TextOverflow = TextOverflow.Ellipsis
) {
    if (html.isBlank() || html == "<p><br></p>" || html == "<p></p>") {
        // Show nothing if content is empty
        return
    }
    
    val state = rememberRichTextState()
    
    LaunchedEffect(html) {
        state.setHtml(html)
    }

    RichText(
        state = state,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        maxLines = maxLines,
        overflow = overflow
    )
}

// Previews
@Preview(showBackground = true, name = "Rich Text Viewer - Full Display")
@Composable
private fun RichTextViewerPreview() {
    MemCloudApplicationTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            RichTextViewer(
                html = "<h1>Welcome to Rich Text</h1><p>This is a <strong>bold statement</strong> followed by <em>italic text</em>.</p><h2>Features</h2><ul><li>Bullet lists</li><li>Formatted text with <u>underline</u></li><li><s>Strikethrough</s> text</li></ul><p>Code example: <code>println(\"Hello World\")</code></p>",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, name = "Rich Text Preview - Card")
@Composable
private fun RichTextPreviewCardPreview() {
    MemCloudApplicationTheme {
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(8.dp),
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Project Meeting Notes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                RichTextPreview(
                    html = "<p><strong>Discussed the new features for Q4.</strong> Need to finalize the design by <em>Friday</em>.</p><p>John will handle the backend integration while Sarah focuses on the UI components.</p><h2>Action Items:</h2><ul><li>Complete wireframes</li><li>Review technical specs</li><li>Schedule follow-up meeting</li></ul>",
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Rich Text Preview - Compact")
@Composable
private fun RichTextPreviewCompactPreview() {
    MemCloudApplicationTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Compact Preview (3 lines max)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            RichTextPreview(
                html = "<h1>Long Content Example</h1><p>This is a longer piece of content with <strong>bold</strong>, <em>italic</em>, and <u>underlined</u> text that will be truncated after 3 lines to show how the preview handles overflow with ellipsis.</p><ul><li>Item one</li><li>Item two</li><li>Item three</li></ul>",
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
        }
    }
}

@Preview(showBackground = true, name = "Rich Text Viewer - Formatted Lists")
@Composable
private fun RichTextViewerListsPreview() {
    MemCloudApplicationTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            Column {
                Text(
                    text = "List Formatting",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                RichTextViewer(
                    html = "<h2>Unordered List</h2><ul><li>First bullet point</li><li>Second bullet point with <strong>bold</strong></li><li>Third bullet point with <em>italic</em></li></ul><h2>Ordered List</h2><ol><li>First numbered item</li><li>Second numbered item</li><li>Third numbered item</li></ol>",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Rich Text Viewer - Mixed Formatting")
@Composable
private fun RichTextViewerMixedPreview() {
    MemCloudApplicationTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            RichTextViewer(
                html = "<h1>Text Styles</h1><p>Regular text with <strong>bold</strong>, <em>italic</em>, <u>underline</u>, and <s>strikethrough</s>.</p><p><strong><em>Bold and italic combined</em></strong></p><p>Inline code: <code>val x = 42</code></p>",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


