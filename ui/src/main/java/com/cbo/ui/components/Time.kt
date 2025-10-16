package com.cbo.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun RelativeTimeText(
    epochMillis: Long,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Text(
        text = formatRelativeTime(epochMillis),
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = modifier,
    )
}

fun formatRelativeTime(epochMillis: Long, now: Long = System.currentTimeMillis()): String {
    val diff = (now - epochMillis).coerceAtLeast(0)
    val minute = 60_000L
    val hour = 60 * minute
    val day = 24 * hour
    val week = 7 * day
    return when {
        diff < minute -> "just now"
        diff < hour -> "${diff / minute} min ago"
        diff < day -> "${diff / hour} h ago"
        diff < week -> "${diff / day} d ago"
        else -> "${diff / week} w ago"
    }
}


