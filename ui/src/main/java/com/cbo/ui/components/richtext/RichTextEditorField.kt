package com.cbo.ui.components.richtext

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Link
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
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cbo.ui.components.dialogs.AppFormDialog
import com.cbo.ui.components.dialogs.FormField
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RichTextEditorField(
    valueHtml: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Start writing your note...",
) {
    val richTextState: RichTextState = remember { RichTextState() }

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
            tonalElevation = 1.dp,
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
            ) {
                if (richTextState.toMarkdown().isBlank()) {
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

    // Report changes upstream by observing HTML changes
    LaunchedEffect(richTextState) {
        snapshotFlow { richTextState.toHtml() }
            .collectLatest { html -> onValueChange(html) }
    }
}

@Composable
private fun EditorToolbar(state: RichTextState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(onClick = { state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) }) {
                Icon(Icons.Default.FormatBold, contentDescription = "Bold")
            }
            IconButton(onClick = { state.toggleSpanStyle(SpanStyle(fontStyle = Italic)) }) {
                Icon(Icons.Default.FormatItalic, contentDescription = "Italic")
            }
            IconButton(onClick = { state.toggleUnorderedList() }) {
                Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = "Bullet List")
            }
            IconButton(onClick = { state.toggleOrderedList() }) {
                Icon(Icons.Default.FormatListNumbered, contentDescription = "Numbered List")
            }

            Spacer(modifier = Modifier.weight(1f))

            VerticalDivider(modifier = Modifier.fillMaxHeight())
            // Code block insertion prompt
            var codeDialogOpen by remember {
                mutableStateOf(false)
            }

            // Simple link insertion prompt
            var linkDialogOpen by remember { mutableStateOf(false) }
            IconButton(onClick = { linkDialogOpen = true }) {
                Icon(Icons.Default.Link, contentDescription = "Insert Link")
            }

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
        
        HorizontalDivider(modifier = Modifier.fillMaxWidth(),)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = {state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Start))}) {
                Icon(Icons.AutoMirrored.Filled.FormatAlignLeft, "Align Left")
            }
            IconButton(onClick = {state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Center))}) {
                Icon(Icons.Default.FormatAlignCenter, "Align Center")
            }
            IconButton(onClick = {state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Right))}) {
                Icon(Icons.AutoMirrored.Filled.FormatAlignRight, "Align Right")
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

    val fields = listOf(
        FormField(
            key = "text",
            label = "Text",
            placeholder = "Enter link text",
            value = text,
            onValueChange = { text = it },
            isError = text.isBlank(),
            errorMessage = if (text.isBlank()) "Text cannot be empty" else null
        ),
        FormField(
            key = "url",
            label = "URL",
            placeholder = "Enter link URL",
            value = url,
            onValueChange = { url = it },
            isError = url.isBlank(),
            errorMessage = if (url.isBlank()) "URL cannot be empty" else null
        )
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


