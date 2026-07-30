package com.cbo.ui.components.richtext

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.FormatStrikethrough
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.cbo.ui.components.dialogs.AppFormDialog
import com.cbo.ui.components.dialogs.FormField

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RichTextEditorField(
    valueMarkdown: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Start writing your note...",
    minHeight: Int = 200,
) {
    // TextFieldValue is used to track cursor position for Markdown insertion
    var textFieldValue by remember { mutableStateOf(TextFieldValue(valueMarkdown)) }

    // Sync editor when external value changes (e.g., editing existing note)
    LaunchedEffect(valueMarkdown) {
        if (valueMarkdown != textFieldValue.text) {
            textFieldValue = TextFieldValue(
                text = valueMarkdown,
                selection = TextRange(valueMarkdown.length)
            )
        }
    }

    Column(modifier = modifier) {
        EditorToolbar(
            textFieldValue = textFieldValue,
            onValueChange = { newValue ->
                textFieldValue = newValue
                onValueChange(newValue.text)
            }
        )
        HorizontalDivider()
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .heightIn(min = minHeight.dp),
            tonalElevation = 1.dp,
        ) {
            Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                if (textFieldValue.text.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        textFieldValue = newValue
                        onValueChange(newValue.text)
                    },
                    modifier = Modifier.fillMaxSize(),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Toolbar
// ---------------------------------------------------------------------------

@Composable
private fun EditorToolbar(
    textFieldValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
) {
    /** Wraps the current selection (or inserts at cursor) with [prefix] and [suffix]. */
    fun wrap(prefix: String, suffix: String = prefix) {
        val sel = textFieldValue.selection
        val text = textFieldValue.text
        val newText: String
        val newSelection: TextRange

        if (!sel.collapsed) {
            val selected = text.substring(sel.start, sel.end)
            newText = text.substring(0, sel.start) + prefix + selected + suffix +
                    text.substring(sel.end)
            newSelection = TextRange(sel.start, sel.end + prefix.length + suffix.length)
        } else {
            newText = text.substring(0, sel.start) + prefix + suffix + text.substring(sel.start)
            newSelection = TextRange(sel.start + prefix.length)
        }

        onValueChange(TextFieldValue(text = newText, selection = newSelection))
    }

    /** Prepends [prefix] to every selected line (or the line at cursor). */
    fun prependLine(prefix: String) {
        val sel = textFieldValue.selection
        val text = textFieldValue.text

        // Determine the line range
        val lineStart = text.lastIndexOf('\n', sel.start - 1).let { if (it < 0) 0 else it + 1 }
        val lineEnd = text.indexOf('\n', sel.end).let { if (it < 0) text.length else it }
        val lineText = text.substring(lineStart, lineEnd)

        // Toggle: remove prefix if already present, otherwise add it
        val newLineText = if (lineText.startsWith(prefix)) {
            lineText.removePrefix(prefix)
        } else {
            prefix + lineText
        }

        val delta = newLineText.length - lineText.length
        val newText = text.substring(0, lineStart) + newLineText + text.substring(lineEnd)
        val newCursor = (sel.start + delta).coerceIn(0, newText.length)
        onValueChange(TextFieldValue(text = newText, selection = TextRange(newCursor)))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // ---- Row 1: Inline styles ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            RichTextStyleButton(
                onClick = { wrap("**") },
                icon = Icons.Outlined.FormatBold,
                isSelected = false,
            )
            RichTextStyleButton(
                onClick = { wrap("*") },
                icon = Icons.Outlined.FormatItalic,
                isSelected = false,
            )
            RichTextStyleButton(
                onClick = { wrap("~~") },
                icon = Icons.Outlined.FormatStrikethrough,
                isSelected = false,
            )
            RichTextStyleButton(
                onClick = { wrap("`") },
                icon = Icons.Outlined.Code,
                isSelected = false,
            )

            Spacer(modifier = Modifier.weight(1f))
            VerticalDivider(modifier = Modifier.fillMaxHeight())

            // Heading (H3)
            RichTextStyleButton(
                onClick = { prependLine("### ") },
                icon = Icons.Outlined.Title,
                isSelected = false,
            )
        }

        HorizontalDivider(modifier = Modifier.fillMaxWidth())

        // ---- Row 2: Block styles ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // Unordered list
            RichTextStyleButton(
                onClick = { prependLine("- ") },
                icon = Icons.AutoMirrored.Outlined.FormatListBulleted,
                isSelected = false,
            )

            // Ordered list — simple: always prefix with "1. " (user adjusts numbering manually)
            RichTextStyleButton(
                onClick = { prependLine("1. ") },
                icon = Icons.Outlined.FormatListNumbered,
                isSelected = false,
            )

            // Blockquote
            RichTextStyleButton(
                onClick = { prependLine("> ") },
                icon = Icons.Outlined.FormatBold, // placeholder icon
                isSelected = false,
            )

            Spacer(modifier = Modifier.weight(1f))
            VerticalDivider(modifier = Modifier.fillMaxHeight())

            // Link dialog
            var linkDialogOpen by remember { mutableStateOf(false) }
            RichTextStyleButton(
                onClick = { linkDialogOpen = true },
                icon = Icons.Outlined.Link,
                isSelected = false,
            )
            if (linkDialogOpen) {
                SimpleLinkDialog(
                    onConfirm = { text, url ->
                        val sel = textFieldValue.selection
                        val fullText = textFieldValue.text
                        val mdLink = "[$text]($url)"
                        val newText = fullText.substring(0, sel.start) + mdLink +
                                fullText.substring(if (sel.collapsed) sel.start else sel.end)
                        onValueChange(
                            TextFieldValue(
                                text = newText,
                                selection = TextRange(sel.start + mdLink.length)
                            )
                        )
                        linkDialogOpen = false
                    },
                    onDismiss = { linkDialogOpen = false },
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Link dialog
// ---------------------------------------------------------------------------

@Composable
private fun SimpleLinkDialog(
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }

    val fields = listOf(
        FormField(
            key = "text",
            label = "Text",
            placeholder = "Enter link text",
            value = text,
            onValueChange = { text = it },
            isError = text.isBlank(),
            errorMessage = if (text.isBlank()) "Text cannot be empty" else null,
        ),
        FormField(
            key = "url",
            label = "URL",
            placeholder = "Enter link URL",
            value = url,
            onValueChange = { url = it },
            isError = url.isBlank(),
            errorMessage = if (url.isBlank()) "URL cannot be empty" else null,
        ),
    )

    AppFormDialog(
        title = "Insert Link",
        fields = fields,
        onConfirm = { formData ->
            val textValue = formData.fields["text"].orEmpty()
            val urlValue = formData.fields["url"].orEmpty()
            if (textValue.isNotBlank() && urlValue.isNotBlank()) {
                onConfirm(textValue, urlValue)
            }
        },
        onDismiss = onDismiss,
        confirmText = "Add",
        cancelText = "Cancel",
    )
}
