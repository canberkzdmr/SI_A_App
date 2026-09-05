package com.cbo.notes.presentation.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import com.cbo.ui.theme.Dimens
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cbo.core.domain.FieldValidationRules
import com.cbo.notes.domain.model.Category
import com.cbo.notes.domain.model.Tag
import com.cbo.notes.presentation.viewmodel.CategoriesUiState
import com.cbo.notes.presentation.viewmodel.CategoriesViewModel
import com.cbo.ui.components.AppAlertDialog
import com.cbo.ui.components.AppBody
import com.cbo.ui.components.AppInfoDialog
import com.cbo.ui.components.AppOutlinedTextField
import com.cbo.ui.components.AppTitle
import com.cbo.ui.components.AppTitleMedium
import com.cbo.ui.components.ColorPicker
import com.cbo.ui.components.RelativeTimeText
import com.cbo.ui.components.ScreenWithTopBarAndInsets
import com.cbo.ui.components.StatChip
import com.cbo.ui.components.cards.CardVariant
import com.cbo.ui.components.cards.HeaderCard
import com.cbo.ui.components.states.AppEmptyState
import com.cbo.ui.components.states.AppErrorState
import com.cbo.ui.components.states.AppLoadingScreen
import com.cbo.ui.theme.MemCloudApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CategoriesScreen(
    onNavigateBack: () -> Unit,
    onOpenNotesForCategory: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CategoriesContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onClickDialog = viewModel::showInfoDialog,
        onCreateCategory = viewModel::showCreateCategoryDialog,
        onEditCategory = viewModel::showEditCategoryDialog,
        onOpenNotesForCategory = onOpenNotesForCategory,
        onDeleteCategory = viewModel::deleteCategory,
        updateDialogTitle = viewModel::updateDialogTitle,
        updateDialogDescription = viewModel::updateDialogDescription,
        updateDialogColor = viewModel::updateDialogColor,
        saveCategory = viewModel::saveCategory,
        hideDialog = viewModel::hideDialog,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesContent(
    uiState: CategoriesUiState,
    onNavigateBack: () -> Unit,
    onClickDialog: () -> Unit,
    onCreateCategory: () -> Unit,
    onEditCategory: (Category) -> Unit,
    onOpenNotesForCategory: (Int) -> Unit,
    onDeleteCategory: (Category) -> Unit,
    updateDialogTitle: (String) -> Unit,
    updateDialogDescription: (String) -> Unit,
    updateDialogColor: (String?) -> Unit,
    saveCategory: () -> Unit,
    hideDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenWithTopBarAndInsets(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { AppTitle(stringResource(id = com.cbo.notes.R.string.manage_categories_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = com.cbo.notes.R.string.back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onClickDialog) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(id = com.cbo.notes.R.string.info),
                        )
                    }
                    IconButton(onClick = onCreateCategory) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(id = com.cbo.notes.R.string.add_category),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                AppLoadingScreen(
                    message = stringResource(id = com.cbo.notes.R.string.loading_categories),
                    showProgress = true,
                )
            }

            uiState.errorMessage != null -> {
                AppErrorState(
                    error = uiState.errorMessage ?: stringResource(id = com.cbo.notes.R.string.failed_to_load_categories),
                    onRetry = { /* Retry loading categories */ },
                )
            }

            uiState.categories.isEmpty() -> {
                AppEmptyState(
                    title = stringResource(id = com.cbo.notes.R.string.no_categories_yet),
                    message = stringResource(id = com.cbo.notes.R.string.create_first_category),
                    actionText = stringResource(id = com.cbo.notes.R.string.create_category),
                    onAction = onCreateCategory,
                )
            }

            else -> {
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        HeaderCard(
                            title = stringResource(id = com.cbo.notes.R.string.organize_notes_categories),
                            icon = Icons.Default.Category,
                            content = stringResource(id = com.cbo.notes.R.string.swipe_hint),
                            variant = CardVariant.DEFAULT,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    itemsIndexed(uiState.categories) { index, category ->
                        CategoryItem(
                            category = category,
                            onEdit = { onEditCategory(category) },
                            onOpen = { onOpenNotesForCategory(category.id) },
                            onDelete = { onDeleteCategory(category) },
                            isFirstItem = index == 0,
                            lastUpdatedMillis = uiState.lastUpdatedByCategory[category.id],
                            previewTags = uiState.topTagsByCategory[category.id].orEmpty(),
                        )
                    }
                }
            }
        }
    }

    if (uiState.showCreateDialog) {
        CategoryDialog(
            title = if (uiState.editingCategory != null) stringResource(id = com.cbo.notes.R.string.edit_category) else stringResource(id = com.cbo.notes.R.string.create_category),
            name = uiState.dialogTitle,
            description = uiState.dialogDescription,
            color = uiState.dialogColor,
            isCreating = uiState.isCreating,
            validationErrorMessage = uiState.dialogValidationErrorMessage,
            onNameChange = updateDialogTitle,
            onDescriptionChange = updateDialogDescription,
            onColorChange = updateDialogColor,
            onSave = saveCategory,
            onDismiss = hideDialog,
        )
    }

    if (uiState.showInfoDialog) {
        AppInfoDialog(
            title = stringResource(id = com.cbo.notes.R.string.info),
            onDismiss = hideDialog,
            message = stringResource(id = com.cbo.notes.R.string.info_tip),
        )
    }
}

@Composable
private fun EmptyCategoriesState(
    onCreateCategory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Category,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = com.cbo.notes.R.string.no_categories_yet),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(id = com.cbo.notes.R.string.create_first_category),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onCreateCategory,
            modifier = Modifier.fillMaxWidth(0.6f),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(id = com.cbo.notes.R.string.create_category))
        }
    }
}

@Composable
private fun SwipeBackgroundEdit(modifier: Modifier = Modifier) {
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
            imageVector = Icons.Default.Edit,
            contentDescription = stringResource(id = com.cbo.notes.R.string.edit),
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(28.dp),
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
            contentDescription = stringResource(id = com.cbo.notes.R.string.delete),
            tint = MaterialTheme.colorScheme.onError,
            modifier = Modifier.size(28.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryItem(
    category: Category,
    onEdit: () -> Unit,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    isFirstItem: Boolean = false,
    lastUpdatedMillis: Long? = null,
    previewTags: List<Tag> = emptyList(),
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current

    val swipeToDismissBoxState =
        rememberSwipeToDismissBoxState(
            confirmValueChange = { value ->
                when (value) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        onEdit()
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
        val progress = swipeToDismissBoxState.progress
        if (!hasTriggeredHaptic && progress >= 0.5f) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            hasTriggeredHaptic = true
        }
        if (progress < 0.5f && hasTriggeredHaptic) {
            // reset if user swipes back
            hasTriggeredHaptic = false
        }
    }

    // Show swipe hint only for the first item
    val showHint = remember { mutableStateOf(isFirstItem) }
    val offsetX by animateDpAsState(
        targetValue = if (showHint.value) (-60).dp else 0.dp,
        animationSpec = tween(durationMillis = 200, easing = LinearEasing, delayMillis = 100),
    )
    val hintBackgroundVisible = isFirstItem && (showHint.value || offsetX < 0.dp)

    LaunchedEffect(isFirstItem) {
        if (isFirstItem) {
            delay(300)
            showHint.value = false
        }
    }

    if (showDeleteDialog) {
        AppAlertDialog(
            title = stringResource(id = com.cbo.notes.R.string.delete_category_title, category.name),
            message = stringResource(id = com.cbo.notes.R.string.delete_category_message, category.name),
            confirmText = stringResource(id = com.cbo.notes.R.string.delete),
            dismissText = stringResource(id = com.cbo.notes.R.string.cancel),
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            },
            onDismiss = {
                showDeleteDialog = false
                coroutineScope.launch {
                    swipeToDismissBoxState.snapTo(SwipeToDismissBoxValue.Settled)
                }
            },
        )
    }

    Box(
        modifier =
            modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth(),
    ) {
        if (hintBackgroundVisible) {
            SwipeBackgroundDelete(Modifier.matchParentSize().clip(RoundedCornerShape(12.dp)))
        }
        SwipeToDismissBox(
            state = swipeToDismissBoxState,
            enableDismissFromStartToEnd = true,
            enableDismissFromEndToStart = true,
            backgroundContent = {
                when (swipeToDismissBoxState.dismissDirection) {
                    SwipeToDismissBoxValue.StartToEnd -> SwipeBackgroundEdit()
                    SwipeToDismissBoxValue.EndToStart -> SwipeBackgroundDelete()
                    else -> {}
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Card(
                onClick = onOpen,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .offset(x = offsetX),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Color indicator
                        val backgroundColor =
                            category.color?.let { Color(it.toColorInt()) }
                                ?: MaterialTheme.colorScheme.primaryContainer

                        Box(
                            modifier =
                                Modifier
                                    .size(24.dp)
                                    .background(backgroundColor, RoundedCornerShape(6.dp)),
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            AppTitleMedium(
                                text = category.name,
                                fontWeight = FontWeight.SemiBold,
                            )

                            category.description?.takeIf { it.isNotBlank() }?.let { desc ->
                                AppBody(
                                    text = desc,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                StatChip(text = stringResource(id = com.cbo.notes.R.string.notes_count_format, category.notesCount))
                                val lastUpdated = lastUpdatedMillis
                                if (lastUpdated != null && lastUpdated > 0L) {
                                    StatChip(text = stringResource(id = com.cbo.notes.R.string.updated_prefix))
                                    RelativeTimeText(epochMillis = lastUpdated)
                                } else {
                                    RelativeTimeText(epochMillis = category.createdAt)
                                }
                            }

                            if (previewTags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    previewTags.forEach { tag ->
                                        StatChip(text = "#${tag.name}")
                                    }
                                }
                            }
                        }
                    }

                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(id = com.cbo.notes.R.string.more_actions),
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(id = com.cbo.notes.R.string.edit)) },
                                onClick = {
                                    expanded = false
                                    onEdit()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(id = com.cbo.notes.R.string.delete)) },
                                onClick = {
                                    expanded = false
                                    onDelete()
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryDialog(
    title: String,
    name: String,
    description: String,
    color: String?,
    isCreating: Boolean,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onColorChange: (String?) -> Unit,
    validationErrorMessage: String = "",
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val descriptionFocusRequester = remember { FocusRequester() }

                // Category name
                AppOutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = stringResource(id = com.cbo.notes.R.string.category_name),
                    placeholder = stringResource(id = com.cbo.notes.R.string.enter_category_name),
                    modifier = Modifier.fillMaxWidth(),
                    isValid = name.length <= FieldValidationRules.MAX_CATEGORY_NAME_LENGTH,
                    validationErrorMessage = validationErrorMessage,
                    singleLine = true,
                    isError =
                        name.isBlank() || name.length > FieldValidationRules.MAX_CATEGORY_NAME_LENGTH || validationErrorMessage.isNotEmpty(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { descriptionFocusRequester.requestFocus() }),
                )

                // Description (optional)
                val keyboardController = LocalSoftwareKeyboardController.current
                AppOutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = stringResource(id = com.cbo.notes.R.string.description_optional),
                    placeholder = stringResource(id = com.cbo.notes.R.string.enter_description),
                    maxLines = 3,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .focusRequester(remember { descriptionFocusRequester }),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions =
                        KeyboardActions(onDone = {
                            keyboardController?.hide()
                            onSave()
                        }),
                )

                // Color selection
                ColorPicker(
                    selectedColor = color,
                    onColorChange = onColorChange,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = !isCreating && name.isNotBlank() && name.length <= FieldValidationRules.MAX_CATEGORY_NAME_LENGTH,
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(id = com.cbo.notes.R.string.save))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = com.cbo.notes.R.string.cancel))
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
fun CategoriesContentPreview_WithData() {
    MemCloudApplicationTheme {
        CategoriesContent(
            uiState =
                CategoriesUiState(
                    categories = sampleCategoriesForPreview(),
                ),
            onNavigateBack = {},
            onClickDialog = {},
            onCreateCategory = {},
            onEditCategory = {},
            onOpenNotesForCategory = {},
            updateDialogTitle = {},
            updateDialogDescription = {},
            updateDialogColor = {},
            saveCategory = {},
            hideDialog = {},
            onDeleteCategory = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CategoriesContentPreview_Empty() {
    MemCloudApplicationTheme {
        CategoriesContent(
            uiState =
                CategoriesUiState(
                    isLoading = false,
                    categories = emptyList(),
                    showCreateDialog = false,
                ),
            onNavigateBack = {},
            onClickDialog = {},
            onCreateCategory = {},
            onEditCategory = {},
            onOpenNotesForCategory = {},
            updateDialogTitle = {},
            updateDialogDescription = {},
            updateDialogColor = {},
            saveCategory = {},
            hideDialog = {},
            onDeleteCategory = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CategoriesContentPreview_Loading() {
    MemCloudApplicationTheme {
        CategoriesContent(
            uiState = CategoriesUiState(isLoading = true),
            onNavigateBack = {},
            onClickDialog = {},
            onCreateCategory = {},
            onEditCategory = {},
            onOpenNotesForCategory = {},
            updateDialogTitle = {},
            updateDialogDescription = {},
            updateDialogColor = {},
            saveCategory = {},
            hideDialog = {},
            onDeleteCategory = {},
        )
    }
}

// Sample data for previews
private fun sampleCategoriesForPreview(): List<Category> =
    listOf(
        Category(
            id = 1,
            userId = 1,
            name = "Work",
            color = "#FF6B6B",
            description = "Work-related notes and tasks",
            notesCount = 12,
        ),
        Category(
            id = 2,
            userId = 1,
            name = "Personal",
            color = "#4ECDC4",
            description = "Personal notes and reminders",
            notesCount = 7,
        ),
        Category(
            id = 3,
            userId = 1,
            name = "Ideas",
            color = "#45B7D1",
            description = "Creative ideas and inspiration",
            notesCount = 3,
        ),
        Category(
            id = 4,
            userId = 1,
            name = "Learning",
            color = "#FFA07A",
            description = "Study notes and learning materials",
            notesCount = 15,
        ),
        Category(
            id = 5,
            userId = 1,
            name = "Travel",
            color = "#DDA0DD",
            description = "Travel plans and memories",
            notesCount = 2,
        ),
        Category(
            id = 6,
            userId = 1,
            name = "Health",
            color = "#98D8C8",
            description = null,
            notesCount = 4,
        ),
    )
