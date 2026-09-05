package com.cbo.notes.presentation.screen

import android.content.res.Configuration
import com.cbo.core.logger.AppLogger
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import com.cbo.ui.components.ScreenWithTopBarAndInsets
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cbo.notes.domain.model.Tag
import com.cbo.notes.presentation.component.CreateTagDialog
import com.cbo.notes.presentation.component.FilterChip
import com.cbo.notes.presentation.viewmodel.TagsUiState
import com.cbo.notes.presentation.viewmodel.TagsViewModel
import com.cbo.notes.presentation.viewmodel.ViewMode
import com.cbo.ui.components.AppAlertDialog
import com.cbo.ui.components.AppTitle
import com.cbo.ui.components.cards.HeaderCard
import com.cbo.ui.components.cards.CardVariant
import com.cbo.ui.theme.MemCloudApplicationTheme
import androidx.compose.ui.res.stringResource

@Composable
fun TagsScreen(
    onNavigateBack: () -> Unit,
    onOpenNotesForTag: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: TagsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TagsContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onChangeViewMode = viewModel::changeViewMode,
        onShowCreateTagDialog = viewModel::showCreateTagDialog,
        onShowEditTagDialog = viewModel::showEditTagDialog,
        onUpdateSelectedTags = { tag ->
            AppLogger.d("Selected tag -> $tag")
            val currentTags = uiState.selectedTags.toMutableList()
            if (currentTags.contains(tag)) {
                currentTags.remove(tag)
            } else {
                currentTags.add(tag)
            }
            viewModel.updateSelectedTags(currentTags)
        },
        onOpenNotesForTag = onOpenNotesForTag,
        onSaveTag = viewModel::saveTag,
        onUpdateTag = viewModel::updateTag,
        onHideCreateTagDialog = viewModel::hideCreateTagDialog,
        onDeleteSelectedTags = viewModel::deleteSelectedTags,
        onResetStateToEdit = viewModel::resetStateToEdit,
        onTagNameChange = viewModel::updateTagName, // 👈 add this
        onColorChange = viewModel::updateTagColor,  // 👈 add this
        modifier = modifier
    )
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TagsContent(
    uiState: TagsUiState,
    onNavigateBack: () -> Unit,
    onChangeViewMode: (ViewMode) -> Unit,
    onShowCreateTagDialog: () -> Unit,
    onShowEditTagDialog: (Tag) -> Unit,
    onUpdateSelectedTags: (Tag) -> Unit,
    onOpenNotesForTag: (Int) -> Unit = {},
    onSaveTag: () -> Unit,
    onUpdateTag: () -> Unit,
    onHideCreateTagDialog: () -> Unit,
    onDeleteSelectedTags: () -> Unit,
    onResetStateToEdit: () -> Unit,
    onTagNameChange: (String) -> Unit,
    onColorChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler {
        if (uiState.viewMode == ViewMode.DELETE) {
            onChangeViewMode(ViewMode.EDIT)
        } else {
            onNavigateBack()
        }
    }

    ScreenWithTopBarAndInsets(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { AppTitle(stringResource(id = com.cbo.notes.R.string.tags_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = com.cbo.notes.R.string.back))
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            onChangeViewMode(
                                if (uiState.viewMode == ViewMode.EDIT) ViewMode.DELETE else ViewMode.EDIT
                            )
                        }
                    ) {
                        when (uiState.viewMode) {
                            ViewMode.EDIT -> Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(id = com.cbo.notes.R.string.delete_tags_mode),
                                tint = MaterialTheme.colorScheme.error
                            )
                            ViewMode.DELETE -> Icon(
                                Icons.Default.Cancel,
                                contentDescription = stringResource(id = com.cbo.notes.R.string.cancel_delete_mode),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    IconButton(onClick = onShowCreateTagDialog) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(id = com.cbo.notes.R.string.add_tag_cd))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (uiState.viewMode == ViewMode.EDIT) {
                        onShowCreateTagDialog()
                    } else {
                        if (uiState.selectedTags.isNotEmpty()) onDeleteSelectedTags()
                        else onResetStateToEdit()
                    }
                },
                containerColor = animateColorAsState(
                    targetValue = if (uiState.viewMode == ViewMode.EDIT)
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
                    animationSpec = tween(300)
                ).value
            ) {
                AnimatedContent(
                    targetState = uiState.viewMode,
                    transitionSpec = {
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(300),
                        ) togetherWith
                                slideOutHorizontally(
                                    targetOffsetX = { -it },
                                    animationSpec = tween(300),
                                )
                    },
                    label = "fabIconAnim",
                ) { viewMode ->
                    val iconColor by animateColorAsState(
                        targetValue = if (viewMode == ViewMode.EDIT)
                            MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onError,
                        animationSpec = tween(300)
                    )
                    Icon(
                        imageVector = if (viewMode == ViewMode.EDIT)
                            Icons.Default.Add
                        else Icons.Default.Delete,
                        contentDescription = null,
                        tint = iconColor
                    )
                }
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            uiState.tags.isEmpty() -> EmptyTagsState(
                onCreateTag = onShowCreateTagDialog,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )

            else -> {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    HeaderCard(
                        modifier = Modifier
                            .padding(paddingValues)
                            .padding(16.dp),
                        variant = CardVariant.DEFAULT,
                        icon = Icons.Default.Tag,
                        title = stringResource(id = com.cbo.notes.R.string.manage_your_tags),
                        content = stringResource(id = com.cbo.notes.R.string.tags_help_text)
                    )

                    Spacer(Modifier.height(16.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                    ) {
                        uiState.tags.forEach { tag ->
                            JiggleTag(
                                tag = tag,
                                viewMode = uiState.viewMode,
                                onClick = { if (uiState.viewMode == ViewMode.EDIT) onShowEditTagDialog(tag) else onOpenNotesForTag(tag.id) },
                                selectedTags = uiState.selectedTags,
                                onTagSelected = {
                                    onUpdateSelectedTags(tag)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.showCreateDialog) {
        CreateTagDialog(
            tagName = uiState.dialogTagName,
            selectedColor = uiState.dialogTagColor,
            isEdit = uiState.editingTag != null,
            isCreating = uiState.isCreating,
            onConfirm = { if (uiState.editingTag == null) onSaveTag() else onUpdateTag() },
            onDismiss = onHideCreateTagDialog,
            onTagNameChange = onTagNameChange,
            onColorChange = onColorChange
        )
    }

    if (uiState.showDeleteTagDialog) {
        val count = uiState.selectedTags.size
        AppAlertDialog(
            title = stringResource(id = com.cbo.notes.R.string.delete_tags_title),
            message = if (count > 1)
                stringResource(id = com.cbo.notes.R.string.delete_tags_message_multiple, count)
            else stringResource(id = com.cbo.notes.R.string.delete_tags_message_single),
            onConfirm = onDeleteSelectedTags,
            onDismiss = onResetStateToEdit
        )
    }
}

@Composable
fun TagItem(
    tag: Tag,
    selectedTags: List<Tag>,
    onTagToggle: (Tag) -> Unit,
) {
    FilterChip(
        modifier = Modifier,
        selected = selectedTags.any { it.id == tag.id },
        onClick = { onTagToggle(tag) },
        leadingIcon = { Icon(Icons.Default.Tag, contentDescription = "tag") },
        label = "${tag.name} (${tag.usageCount})",
        color = tag.color,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun JiggleTag(
    tag: Tag,
    viewMode: ViewMode,
    onClick: () -> Unit,
    selectedTags: List<Tag>,
    onTagSelected: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "jiggle")
    val haptic = LocalHapticFeedback.current

    // Small rotation back and forth
    val rotation by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "rotation",
    )

    val animatedModifier =
        when (viewMode) {
            ViewMode.EDIT -> {
                Modifier
            }

            ViewMode.DELETE -> {
                Modifier.graphicsLayer {
                    rotationZ = rotation
                }
            }
        }

    Box {
        FilterChip(
            modifier = animatedModifier,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                if (viewMode == ViewMode.EDIT) {
                    onClick()
                } else {
                    onTagSelected()
                }
                AppLogger.d("Clicked tag: ${tag.name}(id:${tag.id})")
            },
            isDeleteMode = viewMode == ViewMode.DELETE,
            label = "#${tag.name} (${tag.usageCount})",
            selected = if (viewMode == ViewMode.EDIT) true else selectedTags.any { it.id == tag.id },
            color = tag.color,
        )
    }
}

@Composable
private fun EmptyTagsState(
    onCreateTag: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Tag,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = com.cbo.notes.R.string.no_tags_yet),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(id = com.cbo.notes.R.string.create_tags_filter),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onCreateTag,
            modifier = Modifier.fillMaxWidth(0.6f),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(id = com.cbo.notes.R.string.create_new_tag))
        }
    }
}

@Preview(showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun TagsContentPreview_Empty() {
    MemCloudApplicationTheme {
        TagsContent(
            uiState = TagsUiState(tags = emptyList(), viewMode = ViewMode.EDIT),
            onNavigateBack = {},
            onChangeViewMode = {},
            onShowCreateTagDialog = {},
            onShowEditTagDialog = {},
            onUpdateSelectedTags = {},
            onSaveTag = {},
            onUpdateTag = {},
            onHideCreateTagDialog = {},
            onDeleteSelectedTags = {},
            onResetStateToEdit = {},
            onTagNameChange = {},
            onColorChange = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TagsContentPreview_WithData() {
    MemCloudApplicationTheme {
        TagsContent(
            uiState = TagsUiState(tags = sampleEditTags(), viewMode = ViewMode.EDIT),
            onNavigateBack = {},
            onChangeViewMode = {},
            onShowCreateTagDialog = {},
            onShowEditTagDialog = {},
            onUpdateSelectedTags = {},
            onSaveTag = {},
            onUpdateTag = {},
            onHideCreateTagDialog = {},
            onDeleteSelectedTags = {},
            onResetStateToEdit = {},
            onTagNameChange = {},
            onColorChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TagsContentPreview_DeleteMode() {
    val sampleTags = listOf(
        Tag(1, userId = 1, "Work", "#FF9800"),
        Tag(2, userId = 1,"Personal", "#4CAF50")
    )
    MemCloudApplicationTheme {
        TagsContent(
            uiState = TagsUiState(tags = sampleTags, viewMode = ViewMode.DELETE, selectedTags = listOf(sampleTags[0])),
            onNavigateBack = {},
            onChangeViewMode = {},
            onShowCreateTagDialog = {},
            onShowEditTagDialog = {},
            onUpdateSelectedTags = {},
            onSaveTag = {},
            onUpdateTag = {},
            onHideCreateTagDialog = {},
            onDeleteSelectedTags = {},
            onResetStateToEdit = {},
            onTagNameChange = {},
            onColorChange = {}
        )
    }
}

private fun sampleEditTags(): List<Tag> =
    listOf(
        Tag(id = 1, userId = 1, name = "urgent", color = "#FF6B6B", usageCount = 3),
        Tag(id = 2, userId = 1, name = "meeting", color = "#4ECDC4", usageCount = 2),
        Tag(id = 3, userId = 1, name = "project", color = "#45B7D1", usageCount = 5),
        Tag(id = 4, userId = 1, name = "idea", color = "#FFA07A", usageCount = 4),
        Tag(id = 5, userId = 1, name = "todo", color = "#DDA0DD", usageCount = 6),
        Tag(id = 6, userId = 1, name = "research", color = "#98D8C8", usageCount = 2),
        Tag(id = 7, userId = 1, name = "work", color = "#FFD93D", usageCount = 7),
        Tag(id = 8, userId = 1, name = "personal", color = "#6A0572", usageCount = 3),
        Tag(id = 9, userId = 1, name = "shopping", color = "#FFB347", usageCount = 5),
        Tag(id = 10, userId = 1, name = "travel", color = "#0096C7", usageCount = 4),
        Tag(id = 11, userId = 1, name = "fitness", color = "#06D6A0", usageCount = 2),
        Tag(id = 12, userId = 1, name = "reading", color = "#8D99AE", usageCount = 6),
        Tag(id = 13, userId = 1, name = "learning", color = "#118AB2", usageCount = 8),
        Tag(id = 14, userId = 1, name = "finance", color = "#EF476F", usageCount = 3),
        Tag(id = 15, userId = 1, name = "deadline", color = "#FF9F1C", usageCount = 5),
        Tag(id = 16, userId = 1, name = "family", color = "#9C89B8", usageCount = 2),
        Tag(id = 17, userId = 1, name = "study", color = "#5E60CE", usageCount = 7),
        Tag(id = 18, userId = 1, name = "event", color = "#38A3A5", usageCount = 4),
        Tag(id = 19, userId = 1, name = "shopping list", color = "#FFB6B9", usageCount = 3),
        Tag(id = 20, userId = 1, name = "important", color = "#FF595E", usageCount = 9),
        Tag(id = 21, userId = 1, name = "notes", color = "#6D597A", usageCount = 2),
        Tag(id = 22, userId = 1, name = "ideas", color = "#FFCA3A", usageCount = 5),
        Tag(id = 23, userId = 1, name = "tasks", color = "#8AC926", usageCount = 7),
        Tag(id = 24, userId = 1, name = "reminder", color = "#1982C4", usageCount = 3),
        Tag(id = 25, userId = 1, name = "holiday", color = "#6A4C93", usageCount = 4),
        Tag(id = 26, userId = 1, name = "groceries", color = "#F72585", usageCount = 6),
        Tag(id = 27, userId = 1, name = "workshop", color = "#3A86FF", usageCount = 2),
        Tag(id = 28, userId = 1, name = "deadline soon", color = "#8338EC", usageCount = 8),
        Tag(id = 29, userId = 1, name = "presentation", color = "#FB5607", usageCount = 3),
        Tag(id = 30, userId = 1, name = "conference", color = "#FF006E", usageCount = 4),
        Tag(id = 31, userId = 1, name = "bookmarks", color = "#FFD166", usageCount = 5),
        Tag(id = 32, userId = 1, name = "planning", color = "#06D6A0", usageCount = 7),
        Tag(id = 33, userId = 1, name = "design", color = "#118AB2", usageCount = 6),
        Tag(id = 34, userId = 1, name = "coding", color = "#073B4C", usageCount = 9),
        Tag(id = 35, userId = 1, name = "bugs", color = "#FF595E", usageCount = 4),
        Tag(id = 36, userId = 1, name = "fixes", color = "#8D99AE", usageCount = 2),
        Tag(id = 37, userId = 1, name = "release", color = "#4ECDC4", usageCount = 5),
        Tag(id = 38, userId = 1, name = "feature", color = "#45B7D1", usageCount = 7),
        Tag(id = 39, userId = 1, name = "review", color = "#FFA07A", usageCount = 3),
        Tag(id = 40, userId = 1, name = "approval", color = "#FFD93D", usageCount = 4),
        Tag(id = 41, userId = 1, name = "meeting notes", color = "#6A0572", usageCount = 2),
        Tag(id = 42, userId = 1, name = "strategy", color = "#0096C7", usageCount = 5),
        Tag(id = 43, userId = 1, name = "marketing", color = "#06D6A0", usageCount = 6),
        Tag(id = 44, userId = 1, name = "social", color = "#8D99AE", usageCount = 3),
        Tag(id = 45, userId = 1, name = "content", color = "#118AB2", usageCount = 8),
        Tag(id = 46, userId = 1, name = "writing", color = "#EF476F", usageCount = 5),
        Tag(id = 47, userId = 1, name = "draft", color = "#FF9F1C", usageCount = 2),
        Tag(id = 48, userId = 1, name = "published", color = "#9C89B8", usageCount = 4),
        Tag(id = 49, userId = 1, name = "archived", color = "#5E60CE", usageCount = 3),
        Tag(id = 50, userId = 1, name = "favorites", color = "#38A3A5", usageCount = 9),
        Tag(id = 51, userId = 1, name = "team", color = "#FFB6B9", usageCount = 2),
        Tag(id = 52, userId = 1, name = "collab", color = "#FF595E", usageCount = 5),
        Tag(id = 53, userId = 1, name = "deadline next", color = "#6D597A", usageCount = 4),
        Tag(id = 54, userId = 1, name = "brainstorm", color = "#FFCA3A", usageCount = 7),
        Tag(id = 55, userId = 1, name = "weekly", color = "#8AC926", usageCount = 6),
        Tag(id = 56, userId = 1, name = "monthly", color = "#1982C4", usageCount = 3),
        Tag(id = 57, userId = 1, name = "yearly", color = "#6A4C93", usageCount = 2),
        Tag(id = 58, userId = 1, name = "journal", color = "#F72585", usageCount = 9),
        Tag(id = 59, userId = 1, name = "habits", color = "#3A86FF", usageCount = 5),
        Tag(id = 60, userId = 1, name = "budget", color = "#8338EC", usageCount = 4),
        Tag(id = 61, userId = 1, name = "invoice", color = "#FB5607", usageCount = 6),
        Tag(id = 62, userId = 1, name = "client", color = "#FF006E", usageCount = 8),
        Tag(id = 63, userId = 1, name = "supplier", color = "#FFD166", usageCount = 3),
        Tag(id = 64, userId = 1, name = "contacts", color = "#06D6A0", usageCount = 2),
        Tag(id = 65, userId = 1, name = "health", color = "#118AB2", usageCount = 5),
        Tag(id = 66, userId = 1, name = "doctor", color = "#073B4C", usageCount = 7),
        Tag(id = 67, userId = 1, name = "meds", color = "#FF595E", usageCount = 3),
        Tag(id = 68, userId = 1, name = "chores", color = "#8D99AE", usageCount = 4),
        Tag(id = 69, userId = 1, name = "home", color = "#4ECDC4", usageCount = 2),
        Tag(id = 70, userId = 1, name = "garden", color = "#45B7D1", usageCount = 6),
        Tag(id = 71, userId = 1, name = "pets", color = "#FFA07A", usageCount = 5),
        Tag(id = 72, userId = 1, name = "kids", color = "#FFD93D", usageCount = 8),
        Tag(id = 73, userId = 1, name = "school", color = "#6A0572", usageCount = 4),
        Tag(id = 74, userId = 1, name = "university", color = "#0096C7", usageCount = 7),
        Tag(id = 75, userId = 1, name = "course", color = "#06D6A0", usageCount = 6),
        Tag(id = 76, userId = 1, name = "exam", color = "#8D99AE", usageCount = 3),
        Tag(id = 77, userId = 1, name = "grades", color = "#118AB2", usageCount = 9),
        Tag(id = 78, userId = 1, name = "certificate", color = "#EF476F", usageCount = 4),
        Tag(id = 79, userId = 1, name = "online", color = "#FF9F1C", usageCount = 5),
        Tag(id = 80, userId = 1, name = "offline", color = "#9C89B8", usageCount = 2),
        Tag(id = 81, userId = 1, name = "movies", color = "#5E60CE", usageCount = 7),
        Tag(id = 82, userId = 1, name = "music", color = "#38A3A5", usageCount = 4),
        Tag(id = 83, userId = 1, name = "series", color = "#FFB6B9", usageCount = 6),
        Tag(id = 84, userId = 1, name = "games", color = "#FF595E", usageCount = 8),
        Tag(id = 85, userId = 1, name = "tech", color = "#6D597A", usageCount = 5),
        Tag(id = 86, userId = 1, name = "gadgets", color = "#FFCA3A", usageCount = 3),
        Tag(id = 87, userId = 1, name = "apps", color = "#8AC926", usageCount = 2),
        Tag(id = 88, userId = 1, name = "android", color = "#1982C4", usageCount = 4),
        Tag(id = 89, userId = 1, name = "ios", color = "#6A4C93", usageCount = 7),
        Tag(id = 90, userId = 1, name = "web", color = "#F72585", usageCount = 6),
        Tag(id = 91, userId = 1, name = "backend", color = "#3A86FF", usageCount = 9),
        Tag(id = 92, userId = 1, name = "frontend", color = "#8338EC", usageCount = 4),
        Tag(id = 93, userId = 1, name = "cloud", color = "#FB5607", usageCount = 3),
        Tag(id = 94, userId = 1, name = "ai", color = "#FF006E", usageCount = 8),
        Tag(id = 95, userId = 1, name = "ml", color = "#FFD166", usageCount = 5),
        Tag(id = 96, userId = 1, name = "data", color = "#06D6A0", usageCount = 7),
        Tag(id = 97, userId = 1, name = "security", color = "#118AB2", usageCount = 6),
        Tag(id = 98, userId = 1, name = "privacy", color = "#073B4C", usageCount = 2),
        Tag(id = 99, userId = 1, name = "policy", color = "#FF595E", usageCount = 3),
        Tag(id = 100, userId = 1, name = "terms", color = "#8D99AE", usageCount = 5),
    )
