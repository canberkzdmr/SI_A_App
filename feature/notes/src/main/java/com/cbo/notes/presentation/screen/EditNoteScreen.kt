package com.cbo.notes.presentation.screen

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.cbo.notes.domain.model.Category
import com.cbo.notes.domain.model.Note
import com.cbo.notes.domain.model.Tag
import com.cbo.notes.presentation.component.CreateTagDialog
import com.cbo.notes.presentation.component.FilterChip
import com.cbo.notes.presentation.component.RichTextEditor
import com.cbo.notes.presentation.viewmodel.EditNoteUiState
import com.cbo.notes.presentation.viewmodel.EditNoteViewModel
import com.cbo.notes.presentation.viewmodel.NavigationEvent
import com.cbo.ui.components.AppIconButton
import com.cbo.ui.components.AppOutlinedTextField
import com.cbo.ui.components.AppTitle
import com.cbo.ui.theme.MemCloudApplicationTheme
import com.mohamedrejeb.richeditor.model.rememberRichTextState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditNoteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDiscardDialog by remember { mutableStateOf(false) }
    
    // Rich text state for content
    val richTextState = rememberRichTextState()
    
    // Initialize rich text state with content when UI state changes
    LaunchedEffect(uiState.contentHtml) {
        if (richTextState.toHtml() != uiState.contentHtml) {
            richTextState.setHtml(uiState.contentHtml)
        }
    }
    
    // Update ViewModel when rich text changes
    LaunchedEffect(richTextState.annotatedString) {
        val html = richTextState.toHtml()
        if (html != uiState.contentHtml) {
            viewModel.updateContentHtml(html)
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

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { 
                    // Editable title in TopAppBar
                    BasicTextField(
                        value = uiState.title,
                        onValueChange = viewModel::updateTitle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp),
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            if (uiState.title.isEmpty()) {
                                Text(
                                    text = "Note Title",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                            innerTextField()
                        }
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
                // Rich Text Content Editor
                RichTextEditor(
                    state = richTextState,
                    modifier = Modifier.fillMaxWidth(),
                    label = "Content",
                    placeholder = "Start writing your note with rich formatting...",
                    minHeight = 300
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
                    tagInputText = uiState.tagInputText,
                    onTagToggle = viewModel::toggleTag,
                    onTagInputChange = viewModel::updateTagInputText,
                    onCreateTagFromInput = viewModel::createTagFromInput,
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
            isEdit = false,
            onTagNameChange = viewModel::updateNewTagName,
            onColorChange = viewModel::updateNewTagColor,
            onConfirm = viewModel::createTag,
            onDismiss = viewModel::hideCreateTagDialog,
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
        AppTitle(
            text = "Category",
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
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppTitle(text = "Tags")
            
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
                
                // Add tag button - kept for custom colors if needed
                AppIconButton(
                    onClick = onCreateTag,
                    modifier = Modifier.size(32.dp),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create custom tag",
                            modifier = Modifier.size(16.dp)
                        )
                    }
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
            placeholder = "Type tag name and press Enter or Space...",
            modifier = Modifier
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
                            contentDescription = "Add tag"
                        )
                    }
                }
            },
            keyboardActions = KeyboardActions(
                onDone = {
                    if (tagInputText.isNotBlank()) {
                        onCreateTagFromInput()
                    }
                }
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            )
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(tags.sortedByDescending { it in selectedTags }) { tag ->
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
                        text = "No tags yet. Type in the field above to create your first tag!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
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

    val richTextState = rememberRichTextState()
    
    // Initialize rich text state with content when UI state changes
    LaunchedEffect(uiState.contentHtml) {
        if (richTextState.toHtml() != uiState.contentHtml) {
            richTextState.setHtml(uiState.contentHtml)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { 
                    // Editable title in TopAppBar
                    BasicTextField(
                        value = uiState.title,
                        onValueChange = onTitleChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp),
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            if (uiState.title.isEmpty()) {
                                Text(
                                    text = "Note Title",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                            innerTextField()
                        }
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
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Rich Text Content Editor
                RichTextEditor(
                    state = richTextState,
                    modifier = Modifier.fillMaxWidth(),
                    label = "Content",
                    placeholder = "Start writing your note with rich formatting...",
                    minHeight = 300
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                CategorySelection(
                    selectedCategory = uiState.selectedCategory,
                    categories = uiState.availableCategories,
                    onCategorySelected = onCategorySelected
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                TagSelection(
                    selectedTags = uiState.selectedTags,
                    tags = uiState.availableTags,
                    tagInputText = uiState.tagInputText,
                    onTagToggle = onTagToggle,
                    onTagInputChange = { /* Handle tag input change in preview */ },
                    onCreateTagFromInput = { /* Handle create tag from input in preview */ },
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

@Preview(showBackground = true, showSystemUi = true, name = "Create Note - Empty")
@Composable
private fun EditNoteScreenCreatePreview() {
    MemCloudApplicationTheme {
        EditNoteScreenContent(
            uiState = EditNoteUiState(
                isLoading = false,
                title = "",
                content = "",
                contentHtml = "",
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

@Preview(showBackground = true, showSystemUi = true, name = "Edit Note - Rich Text")
@Composable
private fun EditNoteScreenEditPreview() {
    MemCloudApplicationTheme {
        EditNoteScreenContent(
            uiState = EditNoteUiState(
                isLoading = false,
                title = "Project Meeting Notes",
                content = "<p><strong>Discussed the new features for Q4.</strong> Need to finalize the design by <em>Friday</em>.</p><p>John will handle the backend integration while Sarah focuses on the UI components.</p><h1>Action Items:</h1><ul><li>Complete wireframes</li><li>Review technical specs</li><li>Schedule follow-up meeting</li></ul>",
                contentHtml = "<p><strong>Discussed the new features for Q4.</strong> Need to finalize the design by <em>Friday</em>.</p><p>John will handle the backend integration while Sarah focuses on the UI components.</p><h1>Action Items:</h1><ul><li>Complete wireframes</li><li>Review technical specs</li><li>Schedule follow-up meeting</li></ul>",
                selectedCategory = sampleEditCategories()[0], // Work category
                selectedTags = listOf(sampleEditTags()[1], sampleEditTags()[2]), // meeting, project
                availableCategories = sampleEditCategories(),
                availableTags = sampleEditTags(),
                hasUnsavedChanges = true,
                originalNote = Note(
                    id = 1,
                    userId = 1,
                    title = "Project Meeting Notes",
                    content = "<p><strong>Discussed the new features for Q4.</strong> Need to finalize the design by <em>Friday</em>.</p>",
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

@Preview(showBackground = true, showSystemUi = true, name = "Loading State")
@Composable
private fun EditNoteScreenLoadingPreview() {
    MemCloudApplicationTheme {
        EditNoteScreenContent(
            uiState = EditNoteUiState(
                isLoading = true,
                contentHtml = ""
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

@Preview(showBackground = true, showSystemUi = true, name = "Saving State")
@Composable
private fun EditNoteScreenSavingPreview() {
    MemCloudApplicationTheme {
        EditNoteScreenContent(
            uiState = EditNoteUiState(
                isLoading = false,
                isSaving = true,
                title = "Quick Note",
                content = "<p>This is a <strong>quick note</strong> that I'm currently saving...</p>",
                contentHtml = "<p>This is a <strong>quick note</strong> that I'm currently saving...</p>",
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

@Preview(showBackground = true, showSystemUi = true, name = "Create Tag Dialog")
@Composable
private fun EditNoteScreenCreateTagPreview() {
    MemCloudApplicationTheme {
        EditNoteScreenContent(
            uiState = EditNoteUiState(
                isLoading = false,
                title = "My Shopping List",
                content = "<p>Need to buy groceries for the weekend party</p>",
                contentHtml = "<p>Need to buy groceries for the weekend party</p>",
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

@Preview(showBackground = true, showSystemUi = true, name = "Rich Text - Code & Lists")
@Composable
private fun EditNoteScreenRichTextPreview() {
    MemCloudApplicationTheme {
        EditNoteScreenContent(
            uiState = EditNoteUiState(
                isLoading = false,
                title = "Development Notes",
                content = "<h1>Kotlin Tips</h1><p>Here are some <em>important</em> things to remember:</p><ol><li><strong>Use data classes</strong> for models</li><li>Leverage <code>sealed classes</code> for state</li><li>Apply <u>coroutines</u> for async operations</li></ol><h2>Code Example</h2><p><code>fun greet() = println(\"Hello\")</code></p>",
                contentHtml = "<h1>Kotlin Tips</h1><p>Here are some <em>important</em> things to remember:</p><ol><li><strong>Use data classes</strong> for models</li><li>Leverage <code>sealed classes</code> for state</li><li>Apply <u>coroutines</u> for async operations</li></ol><h2>Code Example</h2><p><code>fun greet() = println(\"Hello\")</code></p>",
                selectedCategory = sampleEditCategories()[3], // Learning
                selectedTags = listOf(sampleEditTags()[0], sampleEditTags()[3]), // urgent, idea
                availableCategories = sampleEditCategories(),
                availableTags = sampleEditTags(),
                hasUnsavedChanges = true,
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

@Preview(showBackground = true, showSystemUi = true, name = "Rich Text - Formatting Styles")
@Composable
private fun EditNoteScreenFormattingPreview() {
    MemCloudApplicationTheme {
        EditNoteScreenContent(
            uiState = EditNoteUiState(
                isLoading = false,
                title = "Formatting Examples",
                content = "<h1>Text Formatting Demo</h1><p>This text has <strong>bold</strong>, <em>italic</em>, <u>underline</u>, and <s>strikethrough</s> formatting.</p><h2>Mixed Formatting</h2><p><strong><em>Bold and italic</em></strong> combined together.</p><ul><li>Bullet point one</li><li>Bullet point two with <strong>bold text</strong></li><li>Bullet point three with <em>italic text</em></li></ul>",
                contentHtml = "<h1>Text Formatting Demo</h1><p>This text has <strong>bold</strong>, <em>italic</em>, <u>underline</u>, and <s>strikethrough</s> formatting.</p><h2>Mixed Formatting</h2><p><strong><em>Bold and italic</em></strong> combined together.</p><ul><li>Bullet point one</li><li>Bullet point two with <strong>bold text</strong></li><li>Bullet point three with <em>italic text</em></li></ul>",
                selectedCategory = sampleEditCategories()[2], // Ideas
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
