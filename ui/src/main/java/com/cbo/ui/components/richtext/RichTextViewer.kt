package com.cbo.ui.components.richtext

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText

@Composable
fun RichTextViewer(
    html: String,
    modifier: Modifier = Modifier,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    maxLines: Int = 5,
) {
    val state = remember { RichTextState() }

    LaunchedEffect(html) {
        state.setHtml(html)
    }

    RichText(
        state = state,
        overflow = overflow,
        maxLines = maxLines,
        modifier = modifier.fillMaxWidth(),
    )
}


