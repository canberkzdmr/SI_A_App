package com.cbo.notes.presentation.component

import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.cbo.core.common.util.DateUtil
import com.cbo.notes.R
import com.cbo.notes.domain.model.Category
import com.cbo.notes.domain.model.Note
import com.cbo.notes.domain.model.Tag
import com.cbo.ui.components.AppCaption
import com.cbo.ui.components.AppTitleMedium
import com.cbo.ui.components.cards.AppCardHorizontal
import com.cbo.ui.components.cards.CardSize
import com.cbo.ui.components.cards.CardVariant
import com.cbo.ui.components.cards.IndicatorPosition
import com.cbo.ui.components.dialogs.AppDeleteDialog
import com.cbo.ui.theme.MemCloudApplicationTheme
import com.cbo.ui.toHexString
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactNoteCard(
    modifier: Modifier = Modifier,
    note: Note,
    onClick: () -> Unit,
    onTogglePin: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    onArchive: () -> Unit = {},
    onDelete: () -> Unit = {},
    onRestore: (() -> Unit)? = null,
    showPinButton: Boolean = true,
    showMenu: Boolean = true,
    enableSwipe: Boolean = true,
    isFirstItem: Boolean = false,
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

    val coroutineScope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current

    // Only setup swipe state if swipe is enabled
    val swipeToDismissBoxState =
        rememberSwipeToDismissBoxState(
            confirmValueChange = { value ->
                if (!enableSwipe) return@rememberSwipeToDismissBoxState false

                when (value) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        onTogglePin()
                        false
                    }
                    SwipeToDismissBoxValue.EndToStart -> {
                        showDeleteDialog = true
                        false
                    }
                    else -> false
                }
            },
            positionalThreshold = { totalDistance -> totalDistance * 0.5f },
        )

    // Haptic feedback on swipe completion fraction
    var hasTriggeredHaptic by remember { mutableStateOf(false) }
    LaunchedEffect(swipeToDismissBoxState.progress) {
        if (!enableSwipe) return@LaunchedEffect

        val progress = swipeToDismissBoxState.progress
        if (!hasTriggeredHaptic && progress >= 0.5f) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            hasTriggeredHaptic = true
        }
        if (progress < 0.5f && hasTriggeredHaptic) {
            hasTriggeredHaptic = false
        }
    }

    var showHint by remember { mutableStateOf(isFirstItem && enableSwipe) }
    val offsetX by animateDpAsState(
        targetValue = if (showHint) (-60.dp) else 0.dp,
        animationSpec = tween(durationMillis = 200, easing = LinearEasing, delayMillis = 100),
        label = "swipe_hint_offset"
    )
    val isHintBackgroundVisible = isFirstItem && enableSwipe && (showHint || offsetX < 0.dp)

    LaunchedEffect(isFirstItem, enableSwipe) {
        if (isFirstItem && enableSwipe) {
            delay(300)
            showHint = false
        }
    }

    Box(
        modifier = modifier.fillMaxWidth(),
    ) {
        if (isHintBackgroundVisible) {
            SwipeBackgroundDelete(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(12.dp))
            )
        }

        // Conditionally wrap content based on enableSwipe
        if (enableSwipe) {
            SwipeToDismissBox(
                state = swipeToDismissBoxState,
                enableDismissFromStartToEnd = false,
                enableDismissFromEndToStart = true,
                backgroundContent = {
                    when (swipeToDismissBoxState.dismissDirection) {
                        SwipeToDismissBoxValue.StartToEnd -> SwipeBackgroundPin()
                        SwipeToDismissBoxValue.EndToStart -> SwipeBackgroundDelete()
                        else -> {}
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                CompactNoteCardContent(
                    note = note,
                    categoryColor = categoryColor,
                    onClick = onClick,
                    onTogglePin = onTogglePin,
                    onToggleFavorite = onToggleFavorite,
                    onArchive = onArchive,
                    onRestore = onRestore,
                    showPinButton = showPinButton,
                    showMenu = showMenu,
                    isMenuExpanded = expandMenu,
                    onMenuExpandedChange = { expandMenu = it },
                    onDeleteClick = { showDeleteDialog = true },
                    offsetX = offsetX
                )
            }
        } else {
            CompactNoteCardContent(
                note = note,
                categoryColor = categoryColor,
                onClick = onClick,
                onTogglePin = onTogglePin,
                onToggleFavorite = onToggleFavorite,
                onArchive = onArchive,
                onRestore = onRestore,
                showPinButton = showPinButton,
                showMenu = showMenu,
                isMenuExpanded = expandMenu,
                onMenuExpandedChange = { expandMenu = it },
                onDeleteClick = { showDeleteDialog = true },
                offsetX = 0.dp
            )
        }
    }

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
                coroutineScope.launch { swipeToDismissBoxState.snapTo(SwipeToDismissBoxValue.Settled) }
            }
        )
    }
}

@Composable
private fun CompactNoteCardContent(
    note: Note,
    categoryColor: Color,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onArchive: () -> Unit,
    onRestore: (() -> Unit)?,
    showPinButton: Boolean,
    showMenu: Boolean,
    isMenuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    onDeleteClick: () -> Unit,
    offsetX: androidx.compose.ui.unit.Dp,
) {
    AppCardHorizontal(
        modifier = Modifier.offset(x = offsetX),
        variant = CardVariant.OUTLINED,
        size = CardSize.SMALL,
        indicatorColor = categoryColor,
        indicatorPosition = IndicatorPosition.START,
        isPinned = note.isPinned,
        pinnedBorderColor = categoryColor,
        onClick = onClick
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                AppTitleMedium(
                    text = note.title,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
                AppCaption(
                    text = if (note.createdAt == note.updatedAt) {
                        stringResource(R.string.created_at) + " " + DateUtil.formatRelativeTime(note.createdAt)
                    } else {
                        stringResource(R.string.updated_at) + " " + DateUtil.formatRelativeTime(note.updatedAt)
                    },
                    maxLines = 1
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onRestore != null) {
                    RestoreButton(onClick = onRestore)
                } else {
                    if (showPinButton) {
                        PinButton(
                            pinColor = categoryColor,
                            isPinned = note.isPinned,
                            onClick = onTogglePin
                        )
                    }

                    if (showMenu) {
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
                }
            }
        }
    }
}

@Composable
private fun RestoreButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(30.dp)
            .padding(4.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Restore,
            contentDescription = stringResource(R.string.restore_note),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun PinButton(
    pinColor: Color? = null,
    isPinned: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(30.dp)
            .padding(4.dp)
    ) {
        Icon(
            imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
            contentDescription = if (isPinned) stringResource(R.string.unpin) else stringResource(R.string.pin),
            tint = if (isPinned) pinColor ?: MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SwipeBackgroundDelete(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.error)
                .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            tint = MaterialTheme.colorScheme.onError,
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
private fun SwipeBackgroundPin(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Icon(
            imageVector = Icons.Default.PushPin,
            contentDescription = "Pin",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(28.dp),
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun CompactNoteCardPreview() {
    MemCloudApplicationTheme {
        val primaryColorHex = MaterialTheme.colorScheme.primary.toHexString()
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
                    ),
                isPinned = false,
                isFavorite = false,
                userId = 0,
                createdAt = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000,
                updatedAt = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000, // 3 days ago
            )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Normal card
            CompactNoteCard(
                note = note,
                onClick = {},
                onTogglePin = {},
                onToggleFavorite = {},
                onArchive = {},
                onDelete = {},
            )

            // Normal card
            CompactNoteCard(
                note = note.copy(isPinned = true, category = note.category?.copy(color = "#FFECB3")),
                onClick = {},
                onTogglePin = {},
                onToggleFavorite = {},
                onArchive = {},
                onDelete = {},
            )

            // Restore card (for deleted/archived)
            CompactNoteCard(
                note = note.copy(title = "Archived/Deleted Note"),
                onClick = {},
                onRestore = {},
                showPinButton = false,
                showMenu = false,
                enableSwipe = false
            )
        }
    }
}