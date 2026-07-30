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
            var markdown by remember { mutableStateOf("") }
            Column {
                RichTextEditorField(
                    valueMarkdown = markdown,
                    onValueChange = { markdown = it },
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
            var markdown by remember {
                mutableStateOf(
                    """
                    ### Compose Rich Editor
                    
                    Write **bold**, *italic*, and lists below:
                    
                    - First
                    - Second
                    
                    1. Ordered item one
                    2. Ordered item two
                    
                    > A blockquote example
                    
                    Inline `code` and ~~strikethrough~~ also work.
                    """.trimIndent()
                )
            }
            Column {
                RichTextEditorField(
                    valueMarkdown = markdown,
                    onValueChange = { markdown = it },
                )
            }
        }
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
                        This is **bold** and *italic* text with a [link](https://example.com).
                        
                        1. Item one
                        2. Item two
                        
                        - Bullet A
                        - Bullet B
                        
                        > A wise person once said…
                        
                        Inline `code` snippet.
                    """.trimIndent(),
                )
            }
        }
    }
}