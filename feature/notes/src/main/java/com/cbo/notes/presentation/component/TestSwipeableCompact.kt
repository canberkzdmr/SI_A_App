package com.cbo.notes.presentation.component

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.cbo.notes.domain.model.Category
import com.cbo.notes.domain.model.Note
import com.cbo.notes.domain.model.Tag
import com.cbo.ui.components.AppTitleMedium
import com.cbo.ui.components.cards.AppCardHorizontal
import com.cbo.ui.components.cards.CardSize
import com.cbo.ui.components.cards.CardVariant
import com.cbo.ui.components.cards.IndicatorPosition
import com.cbo.ui.toHexString
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class SwipeState {
    CLOSED,
    REVEALED
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalFoundationApi::class)
@Composable
fun SwipeableNoteCard(
    note: Note,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    // Add other actions if needed, but we'll focus on Pin/Favorite for the swipe
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val actionButtonSize = 70.dp
    val actionAreaWidth = actionButtonSize * 2
    val actionAreaWidthPx = with(density) { actionAreaWidth.toPx() }

    // 2. Configure the AnchoredDraggableState
    val state = remember {
        AnchoredDraggableState(
            initialValue = SwipeState.CLOSED,
            positionalThreshold = { distance: Float -> distance * 0.5f },
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            animationSpec = tween(durationMillis = 300),
        ).apply {
            updateAnchors(
                DraggableAnchors {
                    SwipeState.CLOSED at 0f
                    SwipeState.REVEALED at -actionAreaWidthPx // Swipe Left (negative offset)
                }
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // --- BACKGROUND (Swipe Actions) ---
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(actionAreaWidth)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pin Action
            SwipeActionItem(
                label = if (note.isPinned) "Unpin" else "Pin",
                icon = Icons.Filled.PushPin,
                color = if (note.isPinned) Color.Gray else Color(0xFFFFA000) // Unpin uses a different color
            ) {
                onTogglePin()
                // 💥 Key: Auto-close the card after action
                scope.launch { state.animateTo(SwipeState.CLOSED) }
            }

            // Favorite Action
            SwipeActionItem(
                label = if (note.isFavorite) "Unfav" else "Fav",
                icon = Icons.Filled.Favorite,
                color = if (note.isFavorite) Color.Gray else Color(0xFFD32F2F)
            ) {
                onToggleFavorite()
                // 💥 Key: Auto-close the card after action
                scope.launch { state.animateTo(SwipeState.CLOSED) }
            }
        }

        // --- FOREGROUND (Your NoteCardC Content) ---
        // The foreground is wrapped to handle the movement and click.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset {
                    IntOffset(
                        x = state.requireOffset().roundToInt(),
                        y = 0
                    )
                }
                .anchoredDraggable(
                    state = state,
                    orientation = Orientation.Horizontal
                )
                .clickable(onClick = onClick) // Original onClick for the card
        ) {
            // 3. Place your original NoteCardC content here
            // Note: We remove the internal Pin/Favorite buttons as they are now swipe actions
            NoteCardContent(
                note = note,
                // We pass a dummy onClick for the internal content's onClick to prevent interference
                // and keep the outer Box's onClick as the primary click handler.
                onClick = onClick
            )
        }
    }
}

// Helper Composable for the swipe actions
@Composable
private fun RowScope.SwipeActionItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .weight(1f)
            .background(color)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = label, tint = Color.White)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White)
        }
    }
}

// Helper to keep the original card logic separate, excluding the Pin/Favorite/More buttons
@Composable
private fun NoteCardContent(
    note: Note,
    onClick: () -> Unit, // Keeping this for structural completeness
) {
    val categoryColor =
        note.category?.let { category ->
            category.color?.let { color ->
                // Assuming toColorInt() is a function that converts hex string to Int
                // Color(color.toColorInt())
                Color.Red // Placeholder since I don't have your full utils
            } ?: run {
                MaterialTheme.colorScheme.primary
            }
        } ?: run { MaterialTheme.colorScheme.primary }

    AppCardHorizontal( // Use your custom AppCardHorizontal here
        variant = CardVariant.OUTLINED, // Replace with your actual CardVariant
        size = CardSize.SMALL, // Replace with your actual CardSize
        indicatorColor = categoryColor,
        indicatorPosition = IndicatorPosition.START, // Replace with your actual IndicatorPosition
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .clickable(onClick = onClick), // Make the content clickable
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppTitleMedium( // Use your custom AppTitleMedium here
                modifier = Modifier.weight(1f),
                text = note.title,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )

            // ❌ Removed the Pin, Favorite, and MoreVert IconButtons
            // as Pin/Favorite are now swipe actions, and MoreVert can be added back
            // inside the content or as another swipe action if desired.
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SwipeableNoteCardPreview() {
    MaterialTheme {
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
                updatedAt = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000, // 3 days ago
            )
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Default (Closed) Note
            item {
                SwipeableNoteCard(
                    note = note,
                    onClick = { println("Card Clicked") },
                    onTogglePin = { println("Pin Toggle") },
                    onToggleFavorite = { println("Favorite Toggle") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 2. Note that is Pinned (Action will be "Unpin")
            item {
                SwipeableNoteCard(
                    note = note,
                    onClick = { println("Card Clicked") },
                    onTogglePin = { println("Unpin Toggle") },
                    onToggleFavorite = { println("Favorite Toggle") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 3. Visual Demonstration of the Revealed State (Manual Offset)
            item {
                Text(
                    text = "--- Swipe Left to Reveal ---",
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.titleSmall
                )
                // This simulates the card being swiped open for the preview
                // NOTE: This uses manual offset and is for visual demonstration only,
                // not for production use.
                DemoRevealedCard()
            }
        }
    }
}

// Helper to manually show the revealed state in the preview
@Composable
fun DemoRevealedCard() {
    val primaryColorHex = MaterialTheme.colorScheme.primary.toHexString()
    val note = Note(
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
        updatedAt = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000, // 3 days ago
    )
    val density = LocalDensity.current
    val actionButtonSize = 70.dp
    val actionAreaWidth = actionButtonSize * 2
    val actionAreaWidthPx = with(density) { actionAreaWidth.toPx() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // BACKGROUND ACTIONS (Visible)
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(actionAreaWidth)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val dummyClick = { }
            SwipeActionItem(label = "Pin", icon = Icons.Filled.PushPin, color = Color(0xFFFFA000), onClick = dummyClick)
            SwipeActionItem(label = "Fav", icon = Icons.Filled.Favorite, color = Color(0xFFD32F2F), onClick = dummyClick)
        }

        // FOREGROUND CARD (Offset to the left)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                // Manual offset to reveal the background actions
                .offset { IntOffset(x = -actionAreaWidthPx.roundToInt(), y = 0) }
            // Remove anchors/draggables so it doesn't try to move in the preview
        ) {
            NoteCardContent(note = note, onClick = {})
        }
    }
}