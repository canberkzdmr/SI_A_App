package com.cbo.notes.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cbo.notes.domain.model.Category
import com.cbo.notes.domain.model.Tag
import com.cbo.notes.presentation.component.FilterChip
import com.cbo.notes.presentation.viewmodel.EditNoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditNoteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDiscardDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Any initialization logic if needed
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
                if (uiState.availableTags.isNotEmpty()) {
                    TagSelection(
                        tags = uiState.availableTags,
                        selectedTags = uiState.selectedTags,
                        onTagToggle = viewModel::toggleTag
                    )
                }
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
            
            if (selectedTags.isNotEmpty()) {
                Text(
                    text = "${selectedTags.size} selected",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
        }
    }
}
