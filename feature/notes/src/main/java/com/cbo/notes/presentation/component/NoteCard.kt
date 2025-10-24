package com.cbo.notes.presentation.component

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cbo.notes.domain.model.Note
import com.cbo.ui.components.AppBody
import com.cbo.ui.components.AppCaption
import com.cbo.ui.components.AppLabel
import com.cbo.ui.components.AppOutlinedCard
import com.cbo.ui.components.AppRegular
import com.cbo.ui.components.AppTitle
import com.cbo.ui.components.AppTitleMedium
import com.cbo.ui.theme.MemCloudApplicationTheme
import com.cbo.notes.presentation.component.RichTextPreview
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.graphics.toColorInt
import com.cbo.notes.domain.model.Category
import com.cbo.notes.domain.model.Tag

@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .padding()
            .fillMaxWidth(),
        contentAlignment = Alignment.TopEnd,
    ) {
        AppOutlinedCard(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = if (note.isPinned) 4.dp else 2.dp,
                ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                // Header row with title and actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppTitleMedium(
                        text = note.title,
                        modifier = Modifier.weight(1f),
                    )

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(if (note.isPinned) "Unpin" else "Pin")
                                },
                                onClick = {
                                    onTogglePin()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (note.isPinned) Icons.Default.PushPin else Icons.Default.PushPin,
                                        contentDescription = null,
                                        tint = if (note.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                },
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(if (note.isFavorite) "Remove from favorites" else "Add to favorites")
                                },
                                onClick = {
                                    onToggleFavorite()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (note.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = null,
                                        tint = if (note.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Archive") },
                                onClick = {
                                    onArchive()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Archive,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = {
                                    onDelete()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                },
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth(),
                    thickness = Dp.Hairline,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Content preview
                if (note.content.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    RichTextPreview(
                        html = note.content,
                        maxLines = if (isCompact) 5 else 7,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Category and tags
                if (note.category != null || note.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Tags
                        note.tags.take(if (isCompact) 2 else 3).forEach { tag ->
                            TagChip(
                                tag = tag,
                                isSmall = isCompact,
                            )
                        }

                        // Show more tags indicator
                        if (note.tags.size > (if (isCompact) 2 else 3)) {
                            AppLabel(
                                text = "+${note.tags.size - (if (isCompact) 2 else 3)}",
                                modifier =
                                    Modifier
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }
                }

                // Footer with indicators and date
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    note.category?.let { category ->
                        CategoryChip(
                            category = category,
                            isSmall = isCompact,
                        )
                    }

                    Spacer(modifier.weight(1f))

                    // Updated date
                    AppCaption(
                        modifier = Modifier.padding(start = 4.dp),
                        text = formatDate(note.updatedAt),
                        maxLines = 1
                    )
                }
            }
        }

        if (note.isPinned) {
            Icon(
                imageVector = Icons.Default.PushPin,
                contentDescription = "Pinned",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier =
                    Modifier
                        .size(24.dp)
                        .rotate(45f) // 45° angle rotation
                        .align(Alignment.TopEnd)
                        .offset(x = 0.dp, y = (-14).dp) // move it slightly outside the card
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            shape = CircleShape,
                        )/*.border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)*/
                        .padding(2.dp),
            )
        }
    }
}

@Composable
fun NoteCardCompact(
    note: Note,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .clickable{
                    onClick()
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Color indicator
            val backgroundColor =
                note.category?.color?.let { Color(it.toColorInt()) }
                    ?: MaterialTheme.colorScheme.primaryContainer

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(backgroundColor, RoundedCornerShape(6.dp)),
            )

            AppTitleMedium(
                text = note.title,
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(Modifier.weight(1f))

            if (note.isFavorite) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Delete category",
                    tint = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            if (note.isPinned) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = "Delete category",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            NoteMenu(
                isPinned = note.isPinned,
                isFavorite = note.isFavorite,
                onTogglePin = onTogglePin,
                onToggleFavorite = onToggleFavorite,
                onArchive = onArchive,
                onDelete = onDelete,
            )
        }
        Spacer(Modifier.padding(horizontal = 4.dp).height(1.dp).fillMaxWidth().background(MaterialTheme.colorScheme.onSurfaceVariant))
    }
}

@Composable
fun NoteMenu(
    isPinned: Boolean,
    isFavorite: Boolean,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { showMenu = true },
            modifier = Modifier.size(24.dp),
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
        ) {
            DropdownMenuItem(
                text = { Text(if (isPinned) "Unpin" else "Pin") },
                onClick = {
                    onTogglePin()
                    showMenu = false
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = null,
                        tint = if (isPinned) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )

            DropdownMenuItem(
                text = { Text(if (isFavorite) "Remove from favorites" else "Add to favorites") },
                onClick = {
                    onToggleFavorite()
                    showMenu = false
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )

            DropdownMenuItem(
                text = { Text("Archive") },
                onClick = {
                    onArchive()
                    showMenu = false
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Archive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )

            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = {
                    onDelete()
                    showMenu = false
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                },
            )
        }
    }
}

@Composable
private fun CategoryChip(
    category: Category,
    isSmall: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val backgroundColor =
        category.color?.let { Color(android.graphics.Color.parseColor(it)) }
            ?: MaterialTheme.colorScheme.primaryContainer

    AssistChip(
        onClick = { },
        label = {
            AppLabel(
                text = category.name,
                style = if (isSmall) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        colors =
            AssistChipDefaults.assistChipColors(
                containerColor = backgroundColor,
                labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
        modifier = modifier,
    )
}

@SuppressLint("UseKtx")
@Composable
private fun TagChip(
    tag: Tag,
    isSmall: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val backgroundColor =
        tag.color?.let { Color(it.toColorInt()) }
            ?: MaterialTheme.colorScheme.secondaryContainer

    AssistChip(
        onClick = { },
        label = {
            AppLabel(
                text = "#${tag.name}",
                style = if (isSmall) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        },
        colors =
            AssistChipDefaults.assistChipColors(
                containerColor = backgroundColor,
                labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
        modifier = modifier,
    )
}

private fun formatDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60000 -> "Just now" // Less than 1 minute
        diff < 3600000 -> "${diff / 60000}m ago" // Less than 1 hour
        diff < 86400000 -> "${diff / 3600000}h ago" // Less than 1 day
        diff < 604800000 -> "${diff / 86400000}d ago" // Less than 1 week
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
    }
}

@Preview
@Composable
fun PreviewNoteCardCompact() {
    val note =
        Note(
            id = 1,
            title = "Grocery List",
            content = "Milk, Eggs, Bread, Coffee, Cheese, Chicken breast, Olive oil",
            category =
                Category(
                    id = 1,
                    name = "Personal",
                    color = "#BBDEFB",
                    userId = 0,
                ),
            userId = 0,
            tags =
                listOf(
                    Tag(
                        id = 1,
                        name = "shopping",
                        color = "#FFCDD2",
                        userId = 0,
                    ),
                    Tag(id = 2, name = "urgent", color = "#FFF9C4", userId = 0),
                ),
            isPinned = false,
            isFavorite = false,
            updatedAt = System.currentTimeMillis() - 2 * 60 * 60 * 1000, // 2 hours ago
        )

    MemCloudApplicationTheme {
        NoteCardCompact(
            note = note,
            onClick = {},
            onTogglePin = {},
            onToggleFavorite = {},
            onArchive = {},
            onDelete = {},
        )
    }
}

@Preview(showBackground = true, name = "Note Card - Regular")
@Composable
fun PreviewNoteCard_Regular() {
    val note =
        Note(
            id = 1,
            title = "Grocery List",
            content = "Milk, Eggs, Bread, Coffee, Cheese, Chicken breast, Olive oil",
            category =
                Category(
                    id = 1,
                    name = "Personal",
                    color = "#BBDEFB",
                    userId = 0,
                ),
            userId = 0,
            tags =
                listOf(
                    Tag(
                        id = 1,
                        name = "shopping",
                        color = "#FFCDD2",
                        userId = 0,
                    ),
                    Tag(id = 2, name = "urgent", color = "#FFF9C4", userId = 0),
                ),
            isPinned = true,
            isFavorite = true,
            updatedAt = System.currentTimeMillis() - 2 * 60 * 60 * 1000, // 2 hours ago
        )

    MemCloudApplicationTheme {
        NoteCard(
            note = note,
            onClick = {},
            onTogglePin = {},
            onToggleFavorite = {},
            onArchive = {},
            onDelete = {},
        )
    }
}

@Preview(showBackground = true, name = "Note Card - Pinned & Favorite")
@Composable
fun PreviewNoteCard_PinnedFavorite() {
    val note =
        Note(
            id = 2,
            title = "Project Ideas",
            content = "1. Habit Tracker App\n2. Expense Splitter\n3. Recipe Organizer with AI search",
            category =
                Category(
                    id = 2,
                    name = "Work",
                    color = "#C8E6C9",
                    userId = 0,
                ),
            tags =
                listOf(
                    Tag(id = 3, name = "android", color = "#B3E5FC", userId = 0),
                    Tag(id = 4, name = "compose", color = "#D1C4E9", userId = 0),
                ),
            userId = 0,
            isPinned = true,
            isFavorite = true,
            updatedAt = System.currentTimeMillis() - 30 * 60 * 1000, // 30 min ago
        )

    MemCloudApplicationTheme {
        NoteCard(
            note = note,
            onClick = {},
            onTogglePin = {},
            onToggleFavorite = {},
            onArchive = {},
            onDelete = {},
        )
    }
}

@Preview(showBackground = true, name = "Note Card - Compact")
@Composable
fun PreviewNoteCard_Compact() {
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
                    color = "#FFE0B2",
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
        NoteCard(
            note = note,
            onClick = {},
            onTogglePin = {},
            onToggleFavorite = {},
            onArchive = {},
            onDelete = {},
            isCompact = true,
        )
    }
}
