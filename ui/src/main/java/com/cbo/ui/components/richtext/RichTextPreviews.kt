package com.cbo.ui.components.richtext

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cbo.ui.theme.MemCloudApplicationTheme

@Preview(name = "RichTextEditor – Empty", showBackground = true)
@Composable
private fun RichTextEditorFieldPreview() {
    MemCloudApplicationTheme {
        RichTextEditorField(
            valueMarkdown = "# Preview Title\n\nThis is a *preview* of the **RichTextEditorField** with some `code`.",
            onValueChange = {},
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        )
    }
}

@Preview(name = "RichTextViewer – Markdown", showBackground = true)
@Composable
private fun RichTextViewerPreview() {
    MemCloudApplicationTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column {
                RichTextViewer(
                    markdown = """
                        This is **bold** and *italic* text with a link to 
                        [Example](https://example.com).
                        1. Item one
                        2. Item two
                    """.trimIndent(),
                )
            }
        }
    }
}