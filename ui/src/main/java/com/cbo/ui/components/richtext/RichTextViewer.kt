package com.cbo.ui.components.richtext

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.halilibo.richtext.commonmark.Markdown
import com.halilibo.richtext.ui.material3.RichText

/**
 * Displays Markdown-formatted text using [RichText] from
 * halilozercan/compose-richtext (Material3 themed).
 *
 * @param markdown Raw Markdown string to render.
 */
@Composable
fun RichTextViewer(
    markdown: String,
    modifier: Modifier = Modifier,
) {
    RichText(modifier = modifier) {
        Markdown(content = markdown)
    }
}
