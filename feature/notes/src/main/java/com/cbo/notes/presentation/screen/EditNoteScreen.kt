package com.cbo.notes.presentation.screen

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.NotificationAdd
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cbo.notes.R
import com.cbo.notes.domain.model.Category
import com.cbo.notes.domain.model.Note
import com.cbo.notes.domain.model.Tag
import com.cbo.notes.domain.model.NoteTemplate
import com.cbo.notes.presentation.component.CreateTagDialog
import com.cbo.notes.presentation.component.FilterChip
import com.cbo.notes.presentation.component.ReminderChip
import com.cbo.notes.presentation.component.ReminderDialog
import com.cbo.notes.presentation.component.SelectionBottomSheet
import com.cbo.notes.presentation.viewmodel.EditNoteUiState
import com.cbo.notes.presentation.viewmodel.EditNoteViewModel
import com.cbo.notes.presentation.viewmodel.NavigationEvent
import com.cbo.ui.components.AppBasicTextField
import com.cbo.ui.components.AppIconButton
import com.cbo.ui.components.AppOutlinedTextField
import com.cbo.ui.components.AppTitle
import com.cbo.ui.components.ScreenWithTopBarAndInsets
import com.cbo.ui.components.dialogs.AppConfirmationDialog
import com.cbo.ui.components.dialogs.DialogType
import com.cbo.ui.components.richtext.RichTextEditorField
import com.cbo.ui.components.states.AppErrorState
import com.cbo.ui.components.states.AppLoadingScreen
import com.cbo.ui.theme.MemCloudApplicationTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditNoteViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.showReminderDialog()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.notification_permission_required),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val onRequestReminder = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (isGranted) {
                viewModel.showReminderDialog()
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            viewModel.showReminderDialog()
        }
    }

    // Handle navigation events
    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                is NavigationEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    // Delegate to the unified UI composable
    EditNoteScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onTitleChange = viewModel::updateTitle,
        onContentChange = viewModel::updateContent,
        onSave = viewModel::saveNote,
        onCategorySelected = viewModel::selectCategory,
        onTagToggle = viewModel::toggleTag,
        onClearError = viewModel::clearError,
        onDiscardChanges = viewModel::discardChanges,
        onApplySelection = { category, tags ->
            viewModel.selectTags(tags)
            viewModel.selectCategory(category)
        },
        onShowReminderDialog = onRequestReminder,
        onHideReminderDialog = viewModel::hideReminderDialog,
        onSetReminder = viewModel::setReminder,
        onRemoveReminder = viewModel::removeReminder,
        onShowTemplateSelector = viewModel::showTemplateSelector,
        onApplyTemplate = viewModel::applyTemplate,
        onCreateTemplate = viewModel::createTemplate,
        onHideTemplateSelector = viewModel::hideTemplateSelector,
        onInsertLink = viewModel::insertLink,
        modifier = modifier,
    )
}

@Composable
private fun CategorySelection(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AppTitle(
            text = "Category",
            modifier = Modifier.padding(bottom = 8.dp),
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
        ) {
            // "None" option
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onCategorySelected(null) },
                    label = "None",
                )
            }

            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory?.id == category.id,
                    onClick = { onCategorySelected(category) },
                    label = category.name,
                    color = category.color,
                )
            }
        }
    }
}

@Composable
private fun TagSelection(
    tags: List<Tag>,
    selectedTags: List<Tag>,
    tagInputText: String,
    onTagToggle: (Tag) -> Unit,
    onTagInputChange: (String) -> Unit,
    onCreateTagFromInput: () -> Unit,
    onCreateTag: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppTitle(text = "Tags")

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (selectedTags.isNotEmpty()) {
                    Text(
                        text = "${selectedTags.size} selected",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Add tag button - kept for custom colors if needed
                AppIconButton(
                    onClick = onCreateTag,
                    modifier = Modifier.size(32.dp),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create custom tag",
                            modifier = Modifier.size(16.dp),
                        )
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tag input field
        AppOutlinedTextField(
            value = tagInputText,
            onValueChange = { newValue ->
                // Handle space character - create tag when space is typed
                if (newValue.endsWith(" ") && newValue.trim().isNotBlank()) {
                    val tagName = newValue.trim()
                    if (tagName.isNotBlank()) {
                        // Update with the tag name first, then create the tag
                        onTagInputChange(tagName)
                        onCreateTagFromInput()
                    }
                } else {
                    onTagInputChange(newValue)
                }
            },
            placeholder = "Type tag name & press Enter/Space",
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.type == KeyEventType.KeyDown &&
                            (keyEvent.key == Key.Enter || keyEvent.key == Key.NumPadEnter) &&
                            tagInputText.isNotBlank()
                        ) {
                            onCreateTagFromInput()
                            true
                        } else {
                            false
                        }
                    },
            singleLine = true,
            trailingIcon = {
                if (tagInputText.isNotBlank()) {
                    IconButton(onClick = onCreateTagFromInput) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add tag",
                        )
                    }
                }
            },
            maxLines = 1,
            keyboardActions =
                KeyboardActions(
                    onDone = {
                        if (tagInputText.isNotBlank()) {
                            onCreateTagFromInput()
                        }
                    },
                ),
            keyboardOptions =
                KeyboardOptions(
                    imeAction = ImeAction.Done,
                ),
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
        ) {
            items(tags.sortedByDescending { it in selectedTags }) { tag ->
                FilterChip(
                    selected = selectedTags.any { it.id == tag.id },
                    onClick = { onTagToggle(tag) },
                    label = "#${tag.name}",
                    color = tag.color,
                )
            }

            // If no tags, show helpful message
            if (tags.isEmpty()) {
                item {
                    Text(
                        text = "No tags yet. Type in the field above to create your first tag!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

// Preview-specific version of EditNoteScreen that takes UI state directly
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    uiState: EditNoteUiState,
    onNavigateBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onSave: () -> Unit,
    onCategorySelected: (Category?) -> Unit,
    onTagToggle: (Tag) -> Unit,
    onClearError: () -> Unit = {},
    onDiscardChanges: () -> Unit = {},
    onApplySelection: (Category?, List<Tag>) -> Unit = { _, _ -> },
    onShowReminderDialog: () -> Unit = {},
    onHideReminderDialog: () -> Unit = {},
    onSetReminder: (Long) -> Unit = {},
    onRemoveReminder: () -> Unit = {},
    onShowTemplateSelector: () -> Unit = {},
    onApplyTemplate: (NoteTemplate) -> Unit = {},
    onCreateTemplate: (String, String) -> Unit = { _, _ -> },
    onHideTemplateSelector: () -> Unit = {},
    onInsertLink: (Note) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showSelectionSheet by remember { mutableStateOf(false) }

    // Intercept system back to respect unsaved changes
    BackHandler(enabled = true) {
        if (uiState.hasUnsavedChanges) {
            showDiscardDialog = true
        } else {
            onNavigateBack()
        }
    }

    ScreenWithTopBarAndInsets(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    AppBasicTextField(
                        value = uiState.title,
                        onValueChange = onTitleChange,
                        placeholder = "Note title...",
                        textStyle =
                            MaterialTheme.typography.titleLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (uiState.hasUnsavedChanges) {
                                showDiscardDialog = true
                            } else {
                                onNavigateBack()
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    // Reminder button
                    IconButton(onClick = onShowReminderDialog) {
                        Icon(
                            imageVector = if (uiState.reminderTime != null) 
                                Icons.Default.NotificationsActive 
                            else 
                                Icons.Default.NotificationAdd, 
                            contentDescription = "Set Reminder",
                            tint = if (uiState.reminderTime != null) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onShowTemplateSelector) {
                        Icon(imageVector = Icons.Default.Description, contentDescription = "Templates")
                    }
                    IconButton(onClick = { showSelectionSheet = !showSelectionSheet }) {
                        Icon(imageVector = Icons.Default.CollectionsBookmark, contentDescription = "Filter")
                    }
                    TextButton(
                        onClick = onSave,
                        enabled = uiState.title.isNotBlank() && !uiState.isSaving,
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text("Save")
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                AppLoadingScreen(
                    message = "Loading note...",
                    showProgress = true,
                )
            }

            uiState.errorMessage != null -> {
                AppErrorState(
                    error = uiState.errorMessage ?: "Failed to load note",
                    onRetry = { onClearError() },
                )
            }

            else -> {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Show reminder chip if a reminder is set
                    if (uiState.reminderTime != null) {
                        ReminderChip(
                            reminderTime = uiState.reminderTime,
                            onClick = onShowReminderDialog,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    val focusRequestContent = remember { FocusRequester() }

                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        RichTextEditorField(
                            valueMarkdown = uiState.content,
                            onValueChange = onContentChange,
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .focusRequester(remember { focusRequestContent }),
                            placeholder = "Start writing your note...",
                            minHeight = 300,
                        )

                        // Link Suggestions Dropdown
                        if (uiState.showLinkSuggestions && uiState.linkSearchQuery != null) {
                            val filteredNotes = uiState.allNotes.filter {
                                it.title.contains(uiState.linkSearchQuery, ignoreCase = true)
                            }
                            if (filteredNotes.isNotEmpty()) {
                                androidx.compose.material3.DropdownMenu(
                                    expanded = true,
                                    onDismissRequest = { /* Controlled by typing */ },
                                    modifier = Modifier.fillMaxWidth(0.8f).align(Alignment.TopCenter)
                                ) {
                                    filteredNotes.take(5).forEach { note ->
                                        androidx.compose.material3.DropdownMenuItem(
                                            text = { androidx.compose.material3.Text(note.title) },
                                            onClick = { onInsertLink(note) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (uiState.backlinks.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        androidx.compose.material3.Text(
                            text = "Backlinks",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        androidx.compose.foundation.lazy.LazyRow(
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.backlinks.size) { index ->
                                val backlink = uiState.backlinks[index]
                                androidx.compose.material3.AssistChip(
                                    onClick = { /* Could navigate to note, but out of scope for now */ },
                                    label = { androidx.compose.material3.Text(backlink.title) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Link,
                                            contentDescription = "Link",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    /*Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                        if (uiState.availableCategories.isNotEmpty()) {
                            CategorySelection(
                                categories = uiState.availableCategories,
                                selectedCategory = uiState.selectedCategory,
                                onCategorySelected = onCategorySelected,
                            )
                        }

                        TagSelection(
                            tags = uiState.availableTags,
                            selectedTags = uiState.selectedTags,
                            tagInputText = uiState.tagInputText,
                            onTagToggle = onTagToggle,
                            onTagInputChange = { },
                            onCreateTagFromInput = { },
                            onCreateTag = { },
                        )
                    }*/
                }
            }
        }
    }

    if (showDiscardDialog) {
        AppConfirmationDialog(
            type = DialogType.WARNING,
            title = "Discard changes?",
            message = "You have unsaved changes. Are you sure you want to go back?",
            onConfirm = {
                showDiscardDialog = false
                onDiscardChanges()
                onNavigateBack()
            },
            onDismiss = { showDiscardDialog = false },
            confirmText = "Discard",
            dismissText = "Cancel",
        )
    }

    if (showSelectionSheet) {
        SelectionBottomSheet(
            allCategories = uiState.availableCategories,
            allTags = uiState.availableTags,
            selectedCategory = uiState.selectedCategory,
            selectedTags = uiState.selectedTags,
            onApply = { category, tags, -> onApplySelection(category, tags) },
            onClearAll = {},
            onDismiss = { showSelectionSheet = !showSelectionSheet }
        )
    }

    // Reminder Dialog
    if (uiState.showReminderDialog) {
        ReminderDialog(
            existingReminderTime = uiState.reminderTime,
            onConfirm = onSetReminder,
            onRemove = if (uiState.reminderTime != null) onRemoveReminder else null,
            onDismiss = onHideReminderDialog
        )
    }

    if (uiState.showTemplateSelector) {
        TemplateSelectionDialog(
            templates = uiState.availableTemplates,
            onSelect = onApplyTemplate,
            onCreateTemplate = onCreateTemplate,
            onDismiss = onHideTemplateSelector
        )
    }
}

@Composable
fun TemplateSelectionDialog(
    templates: List<com.cbo.notes.domain.model.NoteTemplate>,
    onSelect: (com.cbo.notes.domain.model.NoteTemplate) -> Unit,
    onCreateTemplate: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var showCreateDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    if (showCreateDialog) {
        CreateTemplateDialog(
            onCreate = { name, content ->
                onCreateTemplate(name, content)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    } else {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = onDismiss,
            title = { androidx.compose.material3.Text("Select Template") },
            text = {
                androidx.compose.foundation.lazy.LazyColumn {
                    items(templates.size) { index ->
                        val template = templates[index]
                        androidx.compose.material3.ListItem(
                            modifier = Modifier.clickable {
                                onSelect(template)
                                onDismiss()
                            },
                            headlineContent = { androidx.compose.material3.Text(template.name) }
                        )
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showCreateDialog = true }) {
                    androidx.compose.material3.Text("Create New")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = onDismiss) {
                    androidx.compose.material3.Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CreateTemplateDialog(
    onCreate: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var content by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { androidx.compose.material3.Text("Create Template") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { androidx.compose.material3.Text("Template Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                androidx.compose.material3.OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { androidx.compose.material3.Text("Content (Markdown)") },
                    modifier = Modifier.fillMaxWidth().height(150.dp)
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = { onCreate(name, content) },
                enabled = name.isNotBlank() && content.isNotBlank()
            ) {
                androidx.compose.material3.Text("Create")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                androidx.compose.material3.Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EditNoteScreenCreatePreview() {
    MemCloudApplicationTheme {
        EditNoteScreen(
            uiState =
                EditNoteUiState(
                    isLoading = false,
                    title = "",
                    content = "",
                    selectedCategory = null,
                    selectedTags = emptyList(),
                    availableCategories = sampleEditCategories(),
                    availableTags = sampleEditTags(),
                    hasUnsavedChanges = false,
                    originalNote = null,
                ),
            onNavigateBack = {},
            onTitleChange = {},
            onContentChange = {},
            onSave = {},
            onCategorySelected = {},
            onTagToggle = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun EditNoteScreenEditPreview() {
    MemCloudApplicationTheme {
        EditNoteScreen(
            uiState =
                EditNoteUiState(
                    isLoading = false,
                    title = "Project Meeting Notes",
                    content = "Discussed the new features for Q4. Need to finalize the design by Friday. John will handle the backend integration while Sarah focuses on the UI components.\n\nAction items:\n• Complete wireframes\n• Review technical specs\n• Schedule follow-up meeting",
                    selectedCategory = sampleEditCategories()[0], // Work category
                    selectedTags = listOf(sampleEditTags()[1], sampleEditTags()[2]), // meeting, project
                    availableCategories = sampleEditCategories(),
                    availableTags = sampleEditTags(),
                    hasUnsavedChanges = true,
                    originalNote =
                        Note(
                            id = 1,
                            userId = 1,
                            title = "Project Meeting Notes",
                            content = "Original content...",
                            isPinned = false,
                            isFavorite = false,
                            isArchived = false,
                        ),
                ),
            onNavigateBack = {},
            onTitleChange = {},
            onContentChange = {},
            onSave = {},
            onCategorySelected = {},
            onTagToggle = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun EditNoteScreenLoadingPreview() {
    MemCloudApplicationTheme {
        EditNoteScreen(
            uiState =
                EditNoteUiState(
                    isLoading = true,
                ),
            onNavigateBack = {},
            onTitleChange = {},
            onContentChange = {},
            onSave = {},
            onCategorySelected = {},
            onTagToggle = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun EditNoteScreenSavingPreview() {
    MemCloudApplicationTheme {
        EditNoteScreen(
            uiState =
                EditNoteUiState(
                    isLoading = false,
                    isSaving = true,
                    title = "Quick Note",
                    content = "This is a quick note that I'm currently saving...",
                    selectedCategory = sampleEditCategories()[1], // Personal
                    selectedTags = listOf(sampleEditTags()[4]), // to-do
                    availableCategories = sampleEditCategories(),
                    availableTags = sampleEditTags(),
                    hasUnsavedChanges = false,
                    originalNote = null,
                ),
            onNavigateBack = {},
            onTitleChange = {},
            onContentChange = {},
            onSave = {},
            onCategorySelected = {},
            onTagToggle = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun EditNoteScreenCreateTagPreview() {
    MemCloudApplicationTheme {
        EditNoteScreen(
            uiState =
                EditNoteUiState(
                    isLoading = false,
                    title = "My Shopping List",
                    content = "Need to buy groceries for the weekend party",
                    selectedCategory = sampleEditCategories()[1], // Personal
                    selectedTags = listOf(sampleEditTags()[4]), // to-do
                    availableCategories = sampleEditCategories(),
                    availableTags = sampleEditTags(),
                    hasUnsavedChanges = true,
                    showCreateTagDialog = true,
                    newTagName = "shopping",
                    newTagColor = "#FF6B6B",
                    isCreatingTag = false,
                ),
            onNavigateBack = {},
            onTitleChange = {},
            onContentChange = {},
            onSave = {},
            onCategorySelected = {},
            onTagToggle = {},
        )
    }
}

// Sample data functions
private fun sampleEditCategories(): List<Category> =
    listOf(
        Category(
            id = 1,
            userId = 1,
            name = "Work",
            color = "#FF6B6B",
            description = "Work-related notes",
            notesCount = 5,
        ),
        Category(
            id = 2,
            userId = 1,
            name = "Personal",
            color = "#4ECDC4",
            description = "Personal notes and reminders",
            notesCount = 3,
        ),
        Category(
            id = 3,
            userId = 1,
            name = "Ideas",
            color = "#45B7D1",
            description = "Creative ideas and inspiration",
            notesCount = 2,
        ),
        Category(
            id = 4,
            userId = 1,
            name = "Learning",
            color = "#FFA07A",
            description = "Study notes and learning materials",
            notesCount = 4,
        ),
    )

private fun sampleEditTags(): List<Tag> =
    listOf(
        Tag(id = 1, userId = 1, name = "urgent", color = "#FF6B6B", usageCount = 3),
        Tag(id = 2, userId = 1, name = "meeting", color = "#4ECDC4", usageCount = 2),
        Tag(id = 3, userId = 1, name = "project", color = "#45B7D1", usageCount = 5),
        Tag(id = 4, userId = 1, name = "idea", color = "#FFA07A", usageCount = 4),
        Tag(id = 5, userId = 1, name = "todo", color = "#DDA0DD", usageCount = 6),
        Tag(id = 6, userId = 1, name = "research", color = "#98D8C8", usageCount = 2),
    )
