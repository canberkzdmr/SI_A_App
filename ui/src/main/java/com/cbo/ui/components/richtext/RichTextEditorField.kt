package com.cbo.ui.components.richtext

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.FormatAlignLeft
import androidx.compose.material.icons.automirrored.outlined.FormatAlignRight
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.FormatAlignCenter
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.FormatStrikethrough
import androidx.compose.material.icons.outlined.FormatUnderlined
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cbo.ui.components.dialogs.AppFormDialog
import com.cbo.ui.components.dialogs.FormField
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RichTextEditorField(
    valueHtml: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Start writing your note...",
    minHeight: Int = 200
) {
    val richTextState = rememberRichTextState()

    // Sync editor when external value changes (e.g., editing existing note)
    LaunchedEffect(valueHtml) {
        // Only update if different to avoid cursor jumps
        if (valueHtml != richTextState.toHtml()) {
            richTextState.setHtml(valueHtml)
        }
    }

    Column(modifier = modifier) {
        EditorToolbar(richTextState)
        HorizontalDivider()
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight.dp),
            tonalElevation = 1.dp,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 12.dp),
            ) {
                if (richTextState.toHtml().isBlank()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                RichTextEditor(
                    state = richTextState,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    // Report changes upstream by observing changes
    LaunchedEffect(richTextState) {
        snapshotFlow { richTextState.annotatedString }
            .map { richTextState.toHtml() }
            .distinctUntilChanged()
            .collectLatest { html -> onValueChange(html) }
    }
}

@Composable
private fun EditorToolbar(state: RichTextState) {

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // Bold
            RichTextStyleButton(
                onClick = { state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) },
                isSelected = state.currentSpanStyle.fontWeight == FontWeight.Bold,
                icon = Icons.Outlined.FormatBold
            )

            // Italic
            RichTextStyleButton(
                onClick = {
                    state.toggleSpanStyle(
                        SpanStyle(
                            fontStyle = Italic
                        )
                    )
                },
                isSelected = state.currentSpanStyle.fontStyle == Italic,
                icon = Icons.Outlined.FormatItalic
            )

            // Underline
            RichTextStyleButton(
                onClick = {
                    state.toggleSpanStyle(
                        SpanStyle(
                            textDecoration = TextDecoration.Underline
                        )
                    )
                },
                isSelected = state.currentSpanStyle.textDecoration?.contains(TextDecoration.Underline) == true,
                icon = Icons.Outlined.FormatUnderlined
            )

            // Strikethrough
            RichTextStyleButton(
                onClick = {
                    state.toggleSpanStyle(
                        SpanStyle(
                            textDecoration = TextDecoration.LineThrough
                        )
                    )
                },
                isSelected = state.currentSpanStyle.textDecoration?.contains(TextDecoration.LineThrough) == true,
                icon = Icons.Outlined.FormatStrikethrough
            )

            // Format Size
            RichTextStyleButton(
                onClick = {
                    state.toggleSpanStyle(
                        SpanStyle(
                            fontSize = 28.sp
                        )
                    )
                },
                isSelected = state.currentSpanStyle.fontSize == 28.sp,
                icon = Icons.Outlined.FormatSize
            )

            // Red
            RichTextStyleButton(
                onClick = {
                    state.toggleSpanStyle(
                        SpanStyle(
                            color = Color.Red
                        )
                    )
                },
                isSelected = state.currentSpanStyle.color == MaterialTheme.colorScheme.error,
                icon = Icons.Filled.Circle,
                tint = MaterialTheme.colorScheme.error
            )

            // Yellow
            RichTextStyleButton(
                onClick = {
                    state.toggleSpanStyle(
                        SpanStyle(
                            background = Color.Yellow
                        )
                    )
                },
                isSelected = state.currentSpanStyle.background == Color.Yellow,
                icon = Icons.Outlined.Circle,
                tint = Color.Yellow
            )
        }

        HorizontalDivider(modifier = Modifier.fillMaxWidth())

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            RichTextStyleButton(
                onClick = {
                    state.addParagraphStyle(
                        ParagraphStyle(
                            textAlign = TextAlign.Left,
                        )
                    )
                },
                isSelected = state.currentParagraphStyle.textAlign == TextAlign.Left,
                icon = Icons.AutoMirrored.Outlined.FormatAlignLeft
            )

            RichTextStyleButton(
                onClick = {
                    state.addParagraphStyle(
                        ParagraphStyle(
                            textAlign = TextAlign.Center
                        )
                    )
                },
                isSelected = state.currentParagraphStyle.textAlign == TextAlign.Center,
                icon = Icons.Outlined.FormatAlignCenter
            )

            RichTextStyleButton(
                onClick = {
                    state.addParagraphStyle(
                        ParagraphStyle(
                            textAlign = TextAlign.Right
                        )
                    )
                },
                isSelected = state.currentParagraphStyle.textAlign == TextAlign.Right,
                icon = Icons.AutoMirrored.Outlined.FormatAlignRight
            )

            Spacer(modifier = Modifier.weight(1f))

            VerticalDivider(modifier = Modifier.fillMaxHeight())
            // Code block insertion prompt
            var codeDialogOpen by remember {
                mutableStateOf(false)
            }

            // Unordered List (Bullet/Dot)
            RichTextStyleButton(
                onClick = {
                    state.toggleUnorderedList()
                },
                isSelected = state.isUnorderedList,
                icon = Icons.AutoMirrored.Outlined.FormatListBulleted,
            )

            // Ordered List 1. 2. 3.
            RichTextStyleButton(
                onClick = {
                    state.toggleOrderedList()
                },
                isSelected = state.isOrderedList,
                icon = Icons.Outlined.FormatListNumbered,
            )

            Spacer(modifier = Modifier.weight(1f))

            VerticalDivider(modifier = Modifier.fillMaxHeight())

            RichTextStyleButton(
                onClick = {
                    state.toggleCodeSpan()
                },
                isSelected = state.isCodeSpan,
                icon = Icons.Outlined.Code,
            )

            // Simple link insertion prompt
            var linkDialogOpen by remember { mutableStateOf(false) }

            RichTextStyleButton(
                onClick = {
                    linkDialogOpen = true
                },
                isSelected = state.isLink,
                icon = Icons.Outlined.Link,
            )

            if (linkDialogOpen) {
                SimpleLinkDialog(
                    onConfirm = { text, url ->
                        state.addLink(text = text, url = url)
                        linkDialogOpen = false
                    },
                    onDismiss = { linkDialogOpen = false },
                )
            }
        }
    }
}

@Composable
private fun SimpleLinkDialog(
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }

    val fields =
        listOf(
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
