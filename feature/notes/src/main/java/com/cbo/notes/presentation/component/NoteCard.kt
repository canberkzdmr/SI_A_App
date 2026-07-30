package com.cbo.notes.presentation.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.cbo.core.common.util.DateUtil
import com.cbo.notes.R
import com.cbo.notes.domain.model.Category
import com.cbo.notes.domain.model.Note
import com.cbo.notes.domain.model.Tag
import com.cbo.ui.components.AppCaption
import com.cbo.ui.components.AppTitle
import com.cbo.ui.components.cards.AppCard
import com.cbo.ui.components.cards.CardSize
import com.cbo.ui.components.cards.CardVariant
import com.cbo.ui.components.cards.IndicatorEffect
import com.cbo.ui.components.cards.IndicatorPosition
import com.cbo.ui.components.cards.IndicatorSize
import com.cbo.ui.components.dialogs.AppDeleteDialog
import com.cbo.ui.components.richtext.RichTextViewer
import com.cbo.ui.theme.MemCloudApplicationTheme
import com.cbo.ui.toHexString

@Composable
fun NoteCard(
    modifier: Modifier = Modifier,
    note: Note,
    onClick: () -> Unit,
    onTogglePin: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    onArchive: () -> Unit = {},
    onDelete: () -> Unit = {},
    onRestore: (() -> Unit)? = null,
    isListView: Boolean = false,
    showPinButton: Boolean = true,
    showMenu: Boolean = true,
) {
    val categoryColor =
        note.category?.let { category ->
            category.color?.let { color ->
                Color(color.toColorInt())
            } ?: run {
                MaterialTheme.colorScheme.primary
            }
        } ?: run { MaterialTheme.colorScheme.primary }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var expandMenu by remember { mutableStateOf(false) }

    NoteCardContent(
        note = note,
        categoryColor = categoryColor,
        onClick = onClick,
        onTogglePin = onTogglePin,
        onToggleFavorite = onToggleFavorite,
        onArchive = onArchive,
        onDeleteClick = { showDeleteDialog = true },
        onRestore = onRestore,
        showPinButton = showPinButton,
        showMenu = showMenu,
        isListView = isListView,
        isMenuExpanded = expandMenu,
        onMenuExpandedChange = { expandMenu = it },
    )

    if (showDeleteDialog) {
        AppDeleteDialog(
            title = stringResource(id = R.string.delete_note_title),
            message = stringResource(id = R.string.delete_note_message),
            onDelete = {
                showDeleteDialog = false
                onDelete()
            },
            onDismiss = {
                showDeleteDialog = false
            }
        )
    }
}

@Composable
fun NoteCardContent(
    note: Note,
    categoryColor: Color,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onArchive: () -> Unit,
    onRestore: (() -> Unit)?,
    showPinButton: Boolean,
    showMenu: Boolean,
    isListView: Boolean = false,
    isMenuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    onDeleteClick: () -> Unit,
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        variant = CardVariant.OUTLINED,
        size = CardSize.SMALL,
        isPinned = note.isPinned,
        indicatorColor = categoryColor,
        indicatorPosition = IndicatorPosition.BOTTOM,
        indicatorEffect = IndicatorEffect.GRADIENT,
        indicatorSize = IndicatorSize.THICK,
        pinnedBorderColor = categoryColor,
        onClick = onClick,
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppTitle(
                    modifier = Modifier.weight(1f),
                    text = note.title,
                    style = MaterialTheme.typography.titleLarge,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )

                NoteOptionsMenu(
                    note = note,
                    isExpanded = isMenuExpanded,
                    onExpandedChange = onMenuExpandedChange,
                    onTogglePin = onTogglePin,
                    onToggleFavorite = onToggleFavorite,
                    onArchive = onArchive,
                    onDelete = onDeleteClick
                )
            }

            // Content preview (render rich text HTML)
            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                RichTextViewer(
                    html = note.content,
                    maxLines = if (isListView) 4 else 1,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            HorizontalDivider(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .fillMaxWidth(0.4f)
                    .height(1.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppCaption(
                    modifier = Modifier.weight(1f),
                    text = if (note.createdAt == note.updatedAt) {
                        stringResource(R.string.created_at) + " " + DateUtil.formatRelativeTime(note.createdAt)
                    } else {
                        stringResource(R.string.updated_at) + " " + DateUtil.formatRelativeTime(note.updatedAt)
                    },
                    maxLines = 1
                )

                // Reminder indicator
                if (note.hasActiveReminder()) {
                    ReminderIndicator(
                        hasReminder = true,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                // Tag indicators as colored dots
                if (note.tags.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        note.tags.take(3).forEach { tag ->
                            TagDot(color = tag.color)
                        }
                        // Show count if there are more tags
                        if (note.tags.size > 3) {
                            AppCaption(
                                text = "+${note.tags.size - 3}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TagDot(
    color: String?,
    modifier: Modifier = Modifier
) {
    val dotColor = color?.let {
        try {
            Color(it.toColorInt())
        } catch (e: Exception) {
            MaterialTheme.colorScheme.secondaryContainer
        }
    } ?: MaterialTheme.colorScheme.secondaryContainer

    Box(
        modifier = modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(dotColor)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = CircleShape
            )
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    device = "id:pixel_5"
)
@Composable
private fun CompactNoteCardPreview() {
    MemCloudApplicationTheme {
        val primaryColorHex = MaterialTheme.colorScheme.error.toHexString()
        val note =
            Note(
                id = 3,
                title = "Meeting Summary QR FA fsfa Ggdgdfgds gsd dfgdsgds dsfg",
                content = "Discussed project roadmap and upcoming milestones. Next meeting scheduled for Friday.",
                category =
                    Category(
                        id = 3,
                        name = "Meetings",
                        userId = 0,
                        color = primaryColorHex,
                    ),
                tags =
                    listOf(
                        Tag(id = 5, name = "team", color = "#FFECB3", userId = 0),
                        Tag(id = 6, name = "weekly", color = "#E1BEE7", userId = 0),
                        Tag(id = 7, name = "planning", color = "#BBDEFB", userId = 0),
                        Tag(id = 7, name = "planning", color = "#BBDEFB", userId = 0),
                        Tag(id = 7, name = "planning", color = "#BBDEFB", userId = 0),
                    ),
                isPinned = false,
                isFavorite = false,
                userId = 0,
                createdAt = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000,
                updatedAt = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000, // 3 days ago
            )
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Normal card
            NoteCard(
                note = note.copy(isPinned = true, isFavorite = true),
                onClick = {},
                onTogglePin = {},
                onToggleFavorite = {},
            )

            // Restore card (for deleted/archived)
            NoteCard(
                note = note.copy(
                    title = "Archived/Deleted Note",
                    content = LoremIpsum(words = 50).values.toList().first().toString()
                ),
                onClick = {},
                onTogglePin = {},
                onToggleFavorite = {},
            )
        }
    }
}