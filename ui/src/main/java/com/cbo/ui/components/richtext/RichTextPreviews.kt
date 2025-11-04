package com.cbo.ui.components.richtext

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cbo.ui.theme.MemCloudApplicationTheme

@Preview(name = "RichTextEditor – Empty", showBackground = true)
@Composable
private fun RichTextEditorFieldPreview_Empty() {
    MemCloudApplicationTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            var html by remember { mutableStateOf("") }
            Column {
                RichTextEditorField(
                    valueHtml = html,
                    onValueChange = { html = it },
                )
            }
        }
    }
}

@Preview(name = "RichTextEditor – With Content", showBackground = true)
@Composable
private fun RichTextEditorFieldPreview_WithContent() {
    MemCloudApplicationTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            var html by remember {
                mutableStateOf(
                    """
                    <h3>Compose Rich Editor</h3>
                    <p>Write <b>bold</b>, <i>italic</i>, and lists below:</p>
                    <ul>
                      <li>First</li>
                      <li>Second</li>
                    </ul>
                    """.trimIndent()
                )
            }
            Column {
                RichTextEditorField(
                    valueHtml = html,
                    onValueChange = { html = it },
                )
            }
        }
    }
}

@Preview(name = "RichTextViewer – HTML", showBackground = true)
@Composable
private fun RichTextViewerPreview() {
    MemCloudApplicationTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column {
                RichTextViewer(
                    html = """
                        <p>This is <b>bold</b> and <i>italic</i> text with a link to 
                        <a href=\"https://example.com\">Example</a>.</p>
                        <ol>
                          <li>Item one</li>
                          <li>Item two</li>
                        </ol>
                    """.trimIndent(),
                )
            }
        }
    }
}