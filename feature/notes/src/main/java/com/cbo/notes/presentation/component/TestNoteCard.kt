package com.cbo.notes.presentation.component

import android.R
import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.cbo.notes.domain.model.Category
import com.cbo.notes.domain.model.Note
import com.cbo.notes.domain.model.Tag
import com.cbo.ui.components.AppTitle
import com.cbo.ui.components.cards.AppCardHorizontal
import com.cbo.ui.components.cards.CardVariant
import com.cbo.ui.theme.MemCloudApplicationTheme

@Composable
fun NoteCardC(
    note: Note,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val categoryColor = note.category?.let { category ->
        category.color?.let { color ->
            Color(color.toColorInt())
        } ?: run {
            MaterialTheme.colorScheme.onPrimary
        }
    } ?: run { MaterialTheme.colorScheme.onPrimary }

    AppCardHorizontal(
        variant = CardVariant.OUTLINED
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            VerticalDivider(thickness = 6.dp,  color = categoryColor, modifier = Modifier.padding(end = 8.dp))

            AppTitle(text = note.title)

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onTogglePin) {
                if (note.isPinned) {
                    Icon(imageVector = Icons.Default.PushPin, contentDescription = null)
                } else {
                    Icon(imageVector = Icons.Outlined.PushPin, contentDescription = null)
                }
            }

            IconButton(onClick = onTogglePin) {
                if (note.isFavorite) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null)
                } else {
                    Icon(imageVector = Icons.Outlined.Star, contentDescription = null)
                }
            }
        }
    }
}


@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
fun NoteCardC_Preview() {
    val note =
        Note(
            id = 3,
            title = "Meeting Summary",
            content = "Discussed project roadmap and upcoming milestones. Next meeting scheduled for Friday.",
            category =
                Category(
                    id = 3,
                    name = "Meetings",
                    userId = 0,
                    color = "#FF9800",
                ),
            tags =
                listOf(
                    Tag(id = 5, name = "team", color = "#FFECB3", userId = 0),
                    Tag(id = 6, name = "weekly", color = "#E1BEE7", userId = 0),
                    Tag(id = 7, name = "planning", color = "#BBDEFB", userId = 0),
                ),
            isPinned = false,
            isFavorite = false,
            userId = 0,
            updatedAt = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000, // 3 days ago
        )
    MemCloudApplicationTheme {
        Column(modifier = Modifier.padding(8.dp)) {
            NoteCardC(
                note = note,
                onClick = {},
                onTogglePin = {},
                onToggleFavorite = {},
                onArchive = {},
                onDelete = {},
            )
        }
    }
}