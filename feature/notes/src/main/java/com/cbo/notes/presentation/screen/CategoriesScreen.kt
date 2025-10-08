package com.cbo.notes.presentation.screen

import android.content.res.Configuration
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cbo.notes.domain.model.Category
import com.cbo.notes.presentation.viewmodel.CategoriesUiState
import com.cbo.notes.presentation.viewmodel.CategoriesViewModel
import com.cbo.ui.theme.MemCloudApplicationTheme
import androidx.core.graphics.toColorInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Manage Categories") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::showCreateCategoryDialog) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add category",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.categories.isEmpty() -> {
                EmptyCategoriesState(
                    onCreateCategory = viewModel::showCreateCategoryDialog,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
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
                    items(uiState.categories) { category ->
                        CategoryItem(
                            category = category,
                            onEdit = { viewModel.showEditCategoryDialog(category) },
                            onDelete = { viewModel.deleteCategory(category) },
                        )
                    }
                }
            }
        }
    }

    // Create/Edit category dialog
    if (uiState.showCreateDialog) {
        CategoryDialog(
            title = if (uiState.editingCategory != null) "Edit Category" else "Create Category",
            name = uiState.dialogTitle,
            description = uiState.dialogDescription,
            color = uiState.dialogColor,
            isCreating = uiState.isCreating,
            onNameChange = viewModel::updateDialogTitle,
            onDescriptionChange = viewModel::updateDialogDescription,
            onColorChange = viewModel::updateDialogColor,
            onSave = viewModel::saveCategory,
            onDismiss = viewModel::hideDialog,
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
            text = "No categories yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Create categories to organize your notes better",
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
            Text("Create Category")
        }
    }
}

@Composable
private fun CategoryItem(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                            .size(16.dp)
                            .background(backgroundColor, RoundedCornerShape(8.dp)),
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    category.description?.let { desc ->
                        if (desc.isNotBlank()) {
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Text(
                        text = "${category.notesCount} notes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit category",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete category",
                        tint = MaterialTheme.colorScheme.error,
                    )
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
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val descriptionFocusRequester = remember { FocusRequester() }

                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Category Name") },
                    placeholder = { Text("Enter category name...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = name.isBlank(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { descriptionFocusRequester.requestFocus() }),
                )

                val keyboardController = LocalSoftwareKeyboardController.current
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Description (optional)") },
                    placeholder = { Text("Enter description...") },
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

                // Color selection could be added here
                // For now, we'll keep it simple and allow users to set colors later
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = !isCreating && name.isNotBlank(),
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

// Preview-specific version of CategoriesScreen that takes UI state directly
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoriesScreenContent(
    uiState: CategoriesUiState,
    onNavigateBack: () -> Unit,
    onCreateCategory: () -> Unit,
    onEditCategory: (Category) -> Unit,
    onDeleteCategory: (Category) -> Unit,
    onSaveCategory: () -> Unit,
    onDismissDialog: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onColorChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Manage Categories") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onCreateCategory) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add category",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.categories.isEmpty()) {
            // Empty state
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "No categories yet",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Create categories to organize your notes better",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(24.dp))

                FilledTonalButton(
                    onClick = onCreateCategory,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create Category")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding =
                    PaddingValues(
                        top = paddingValues.calculateTopPadding() + 16.dp,
                        bottom = paddingValues.calculateBottomPadding() + 16.dp,
                        start = 16.dp,
                        end = 16.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(uiState.categories) { category ->
                    CategoryListItem(
                        category = category,
                        onEdit = { onEditCategory(category) },
                        onDelete = { onDeleteCategory(category) },
                    )
                }
            }
        }
    }

    // Category Dialog
    if (uiState.showCreateDialog) {
        CategoryDialog(
            title = if (uiState.editingCategory == null) "Create Category" else "Edit Category",
            name = uiState.dialogTitle,
            description = uiState.dialogDescription,
            color = uiState.dialogColor,
            isCreating = uiState.isCreating,
            onNameChange = onTitleChange,
            onDescriptionChange = onDescriptionChange,
            onColorChange = onColorChange,
            onSave = onSaveCategory,
            onDismiss = onDismissDialog,
        )
    }
}

@Composable
private fun CategoryListItem(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor =
        category.color?.let { Color(android.graphics.Color.parseColor(it)) }
            ?: MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Color indicator
            Box(
                modifier =
                    Modifier
                        .size(24.dp)
                        .background(backgroundColor, RoundedCornerShape(6.dp)),
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Category info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )

                category.description?.let { desc ->
                    if (desc.isNotBlank()) {
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Text(
                    text = "${category.notesCount} notes",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            // Actions
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit category",
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete category",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
private fun CategoriesScreenPreview() {
    MemCloudApplicationTheme {
        CategoriesScreenContent(
            uiState =
                CategoriesUiState(
                    isLoading = false,
                    categories = sampleCategoriesForPreview(),
                    showCreateDialog = false,
                ),
            onNavigateBack = {},
            onCreateCategory = {},
            onEditCategory = {},
            onDeleteCategory = {},
            onSaveCategory = {},
            onDismissDialog = {},
            onTitleChange = {},
            onDescriptionChange = {},
            onColorChange = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CategoriesScreenEmptyPreview() {
    MemCloudApplicationTheme {
        CategoriesScreenContent(
            uiState =
                CategoriesUiState(
                    isLoading = false,
                    categories = emptyList(),
                    showCreateDialog = false,
                ),
            onNavigateBack = {},
            onCreateCategory = {},
            onEditCategory = {},
            onDeleteCategory = {},
            onSaveCategory = {},
            onDismissDialog = {},
            onTitleChange = {},
            onDescriptionChange = {},
            onColorChange = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CategoriesScreenLoadingPreview() {
    MemCloudApplicationTheme {
        CategoriesScreenContent(
            uiState =
                CategoriesUiState(
                    isLoading = true,
                    categories = emptyList(),
                    showCreateDialog = false,
                ),
            onNavigateBack = {},
            onCreateCategory = {},
            onEditCategory = {},
            onDeleteCategory = {},
            onSaveCategory = {},
            onDismissDialog = {},
            onTitleChange = {},
            onDescriptionChange = {},
            onColorChange = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CategoriesScreenWithDialogPreview() {
    MemCloudApplicationTheme {
        CategoriesScreenContent(
            uiState =
                CategoriesUiState(
                    isLoading = false,
                    categories = sampleCategoriesForPreview(),
                    showCreateDialog = true,
                    dialogTitle = "New Category",
                    dialogDescription = "A category for organizing my notes",
                    dialogColor = "#FF6B6B",
                    editingCategory = null,
                ),
            onNavigateBack = {},
            onCreateCategory = {},
            onEditCategory = {},
            onDeleteCategory = {},
            onSaveCategory = {},
            onDismissDialog = {},
            onTitleChange = {},
            onDescriptionChange = {},
            onColorChange = {},
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
