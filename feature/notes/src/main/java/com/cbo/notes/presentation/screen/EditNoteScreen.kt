package com.cbo.notes.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cbo.notes.domain.model.Category
import com.cbo.notes.domain.model.Note
import com.cbo.notes.domain.model.Tag
import com.cbo.notes.presentation.component.FilterChip
import com.cbo.notes.presentation.viewmodel.EditNoteViewModel
import com.cbo.notes.presentation.viewmodel.NavigationEvent
import com.cbo.notes.presentation.viewmodel.EditNoteUiState
import com.cbo.ui.theme.MyApplicationTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditNoteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDiscardDialog by remember { mutableStateOf(false) }

    // Handle navigation events
    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                is NavigationEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (uiState.originalNote != null) "Edit Note" else "Create Note") 
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (uiState.hasUnsavedChanges) {
                                showDiscardDialog = true
                            } else {
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = viewModel::saveNote,
                        enabled = !uiState.isSaving && uiState.title.isNotBlank()
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title field
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = viewModel::updateTitle,
                    label = { Text("Title") },
                    placeholder = { Text("Enter note title...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.title.isBlank()
                )

                // Content field
                OutlinedTextField(
                    value = uiState.content,
                    onValueChange = viewModel::updateContent,
                    label = { Text("Content") },
                    placeholder = { Text("Start writing your note...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 200.dp),
                    minLines = 8
                )

                // Category selection
                if (uiState.availableCategories.isNotEmpty()) {
                    CategorySelection(
                        categories = uiState.availableCategories,
                        selectedCategory = uiState.selectedCategory,
                        onCategorySelected = viewModel::selectCategory
                    )
                }

                // Tag selection
                TagSelection(
                    tags = uiState.availableTags,
                    selectedTags = uiState.selectedTags,
                    onTagToggle = viewModel::toggleTag,
                    onCreateTag = viewModel::showCreateTagDialog
                )
            }
        }
    }

    // Discard changes dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard changes?") },
            text = { Text("You have unsaved changes. Are you sure you want to go back?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDiscardDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Handle error messages
    uiState.errorMessage?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            viewModel.clearError()
        }
    }

    // Tag creation dialog
    if (uiState.showCreateTagDialog) {
        CreateTagDialog(
            tagName = uiState.newTagName,
            selectedColor = uiState.newTagColor,
            isCreating = uiState.isCreatingTag,
            onTagNameChange = viewModel::updateNewTagName,
            onColorChange = viewModel::updateNewTagColor,
            onConfirm = viewModel::createTag,
            onDismiss = viewModel::hideCreateTagDialog
        )
    }
}

@Composable
private fun CategorySelection(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Category",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            // "None" option
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onCategorySelected(null) },
                    label = "None"
                )
            }

            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory?.id == category.id,
                    onClick = { onCategorySelected(category) },
                    label = category.name,
                    color = category.color
                )
            }
        }
    }
}

@Composable
private fun TagSelection(
    tags: List<Tag>,
    selectedTags: List<Tag>,
    onTagToggle: (Tag) -> Unit,
    onCreateTag: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tags",
                style = MaterialTheme.typography.titleMedium
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedTags.isNotEmpty()) {
                    Text(
                        text = "${selectedTags.size} selected",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Add tag button
                FilledTonalIconButton(
                    onClick = onCreateTag,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create new tag",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(tags) { tag ->
                FilterChip(
                    selected = selectedTags.any { it.id == tag.id },
                    onClick = { onTagToggle(tag) },
                    label = "#${tag.name}",
                    color = tag.color
                )
            }
            
            // If no tags, show helpful message
            if (tags.isEmpty()) {
                item {
                    Text(
                        text = "No tags yet. Tap + to create your first tag!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateTagDialog(
    tagName: String,
    selectedColor: String?,
    isCreating: Boolean,
    onTagNameChange: (String) -> Unit,
    onColorChange: (String?) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = listOf(
        "#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A", "#DDA0DD",
        "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E9", "#F8C471"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Tag") },
        text = {
            Column {
                OutlinedTextField(
                    value = tagName,
                    onValueChange = onTagNameChange,
                    label = { Text("Tag name") },
                    placeholder = { Text("Enter tag name...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = tagName.isBlank()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Color (optional)",
                    style = MaterialTheme.typography.labelLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(colors) { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    Color(android.graphics.Color.parseColor(color)),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onColorChange(color) }
                                .then(
                                    if (selectedColor == color) {
                                        Modifier.border(
                                            3.dp,
                                            MaterialTheme.colorScheme.primary,
                                            RoundedCornerShape(8.dp)
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                        ) {
                            if (selectedColor == color) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(16.dp)
                                )
                            }
                        }
                    }
                    
                    // Add "No color" option
                    item {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onColorChange(null) }
                                .then(
                                    if (selectedColor == null) {
                                        Modifier.border(
                                            3.dp,
                                            MaterialTheme.colorScheme.primary,
                                            RoundedCornerShape(8.dp)
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                        ) {
                            if (selectedColor == null) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(16.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "No color",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = tagName.isNotBlank() && !isCreating
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Create")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Preview-specific version of EditNoteScreen that takes UI state directly
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditNoteScreenContent(
    uiState: EditNoteUiState,
    onNavigateBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onSave: () -> Unit,
    onCategorySelected: (Category?) -> Unit,
    onTagToggle: (Tag) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDiscardDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (uiState.originalNote != null) "Edit Note" else "Create Note") 
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (uiState.hasUnsavedChanges) {
                                showDiscardDialog = true
                            } else {
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = onSave,
                        enabled = uiState.title.isNotBlank() && !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Title input
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = onTitleChange,
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Content input
                OutlinedTextField(
                    value = uiState.content,
                    onValueChange = onContentChange,
                    label = { Text("Content") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    maxLines = Int.MAX_VALUE
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Category selection
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                CategorySelection(
                    selectedCategory = uiState.selectedCategory,
                    categories = uiState.availableCategories,
                    onCategorySelected = onCategorySelected
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Tag selection
                Text(
                    text = "Tags",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                TagSelection(
                    selectedTags = uiState.selectedTags,
                    tags = uiState.availableTags,
                    onTagToggle = onTagToggle,
                    onCreateTag = { /* Handle create tag in preview */ }
                )
            }
        }
    }

    // Discard dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard changes?") },
            text = { Text("You have unsaved changes. Are you sure you want to discard them?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDiscardDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun EditNoteScreenCreatePreview() {
    MyApplicationTheme {
        EditNoteScreenContent(
            uiState = EditNoteUiState(
                isLoading = false,
                title = "",
                content = "",
                selectedCategory = null,
                selectedTags = emptyList(),
                availableCategories = sampleEditCategories(),
                availableTags = sampleEditTags(),
                hasUnsavedChanges = false,
                originalNote = null
            ),
            onNavigateBack = {},
            onTitleChange = {},
            onContentChange = {},
            onSave = {},
            onCategorySelected = {},
            onTagToggle = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun EditNoteScreenEditPreview() {
    MyApplicationTheme {
        EditNoteScreenContent(
            uiState = EditNoteUiState(
                isLoading = false,
                title = "Project Meeting Notes",
                content = "Discussed the new features for Q4. Need to finalize the design by Friday. John will handle the backend integration while Sarah focuses on the UI components.\n\nAction items:\n• Complete wireframes\n• Review technical specs\n• Schedule follow-up meeting",
                selectedCategory = sampleEditCategories()[0], // Work category
                selectedTags = listOf(sampleEditTags()[1], sampleEditTags()[2]), // meeting, project
                availableCategories = sampleEditCategories(),
                availableTags = sampleEditTags(),
                hasUnsavedChanges = true,
                originalNote = Note(
                    id = 1,
                    userId = 1,
                    title = "Project Meeting Notes",
                    content = "Original content...",
                    isPinned = false,
                    isFavorite = false,
                    isArchived = false
                )
            ),
            onNavigateBack = {},
            onTitleChange = {},
            onContentChange = {},
            onSave = {},
            onCategorySelected = {},
            onTagToggle = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun EditNoteScreenLoadingPreview() {
    MyApplicationTheme {
        EditNoteScreenContent(
            uiState = EditNoteUiState(
                isLoading = true
            ),
            onNavigateBack = {},
            onTitleChange = {},
            onContentChange = {},
            onSave = {},
            onCategorySelected = {},
            onTagToggle = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun EditNoteScreenSavingPreview() {
    MyApplicationTheme {
        EditNoteScreenContent(
            uiState = EditNoteUiState(
                isLoading = false,
                isSaving = true,
                title = "Quick Note",
                content = "This is a quick note that I'm currently saving...",
                selectedCategory = sampleEditCategories()[1], // Personal
                selectedTags = listOf(sampleEditTags()[4]), // todo
                availableCategories = sampleEditCategories(),
                availableTags = sampleEditTags(),
                hasUnsavedChanges = false,
                originalNote = null
            ),
            onNavigateBack = {},
            onTitleChange = {},
            onContentChange = {},
            onSave = {},
            onCategorySelected = {},
            onTagToggle = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun EditNoteScreenCreateTagPreview() {
    MyApplicationTheme {
        EditNoteScreenContent(
            uiState = EditNoteUiState(
                isLoading = false,
                title = "My Shopping List",
                content = "Need to buy groceries for the weekend party",
                selectedCategory = sampleEditCategories()[1], // Personal
                selectedTags = listOf(sampleEditTags()[4]), // todo
                availableCategories = sampleEditCategories(),
                availableTags = sampleEditTags(),
                hasUnsavedChanges = true,
                showCreateTagDialog = true,
                newTagName = "shopping",
                newTagColor = "#FF6B6B",
                isCreatingTag = false
            ),
            onNavigateBack = {},
            onTitleChange = {},
            onContentChange = {},
            onSave = {},
            onCategorySelected = {},
            onTagToggle = {}
        )
    }
}

// Sample data functions
private fun sampleEditCategories(): List<Category> = listOf(
    Category(
        id = 1,
        userId = 1,
        name = "Work",
        color = "#FF6B6B",
        description = "Work-related notes",
        notesCount = 5
    ),
    Category(
        id = 2,
        userId = 1,
        name = "Personal",
        color = "#4ECDC4",
        description = "Personal notes and reminders",
        notesCount = 3
    ),
    Category(
        id = 3,
        userId = 1,
        name = "Ideas",
        color = "#45B7D1",
        description = "Creative ideas and inspiration",
        notesCount = 2
    ),
    Category(
        id = 4,
        userId = 1,
        name = "Learning",
        color = "#FFA07A",
        description = "Study notes and learning materials",
        notesCount = 4
    )
)

private fun sampleEditTags(): List<Tag> = listOf(
    Tag(id = 1, userId = 1, name = "urgent", color = "#FF6B6B", usageCount = 3),
    Tag(id = 2, userId = 1, name = "meeting", color = "#4ECDC4", usageCount = 2),
    Tag(id = 3, userId = 1, name = "project", color = "#45B7D1", usageCount = 5),
    Tag(id = 4, userId = 1, name = "idea", color = "#FFA07A", usageCount = 4),
    Tag(id = 5, userId = 1, name = "todo", color = "#DDA0DD", usageCount = 6),
    Tag(id = 6, userId = 1, name = "research", color = "#98D8C8", usageCount = 2)
)
