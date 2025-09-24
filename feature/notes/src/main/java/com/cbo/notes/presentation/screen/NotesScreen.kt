package com.cbo.notes.presentation.screen

import android.util.Log
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cbo.notes.domain.model.Category
import com.cbo.notes.domain.model.Note
import com.cbo.notes.domain.model.Tag
import com.cbo.notes.presentation.component.FilterChip
import com.cbo.notes.presentation.component.NoteCard
import com.cbo.notes.presentation.component.NotesAppBar
import com.cbo.notes.presentation.component.NotesEmptyState
import com.cbo.notes.presentation.component.SortBottomSheet
import com.cbo.notes.presentation.SortOrder
import com.cbo.notes.presentation.ViewMode
import com.cbo.notes.presentation.viewmodel.NotesUiState
import com.cbo.notes.presentation.viewmodel.NotesViewModel
import com.cbo.ui.components.AppLabel
import com.cbo.ui.theme.MyApplicationTheme

@Composable
fun NotesScreen(
    onNavigateToCreateNote: () -> Unit,
    onNavigateToEditNote: (noteId: Int) -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSortBottomSheet by remember { mutableStateOf(false) }

    BackHandler {
        Log.i("NotesScreen", "Back button is disabled for Notes Screen")
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            NotesAppBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::searchNotes,
                onClearSearch = { viewModel.searchNotes("") },
                viewMode = uiState.viewMode,
                onViewModeChange = viewModel::changeViewMode,
                onSortClick = { showSortBottomSheet = true },
                onCategoriesClick = onNavigateToCategories,
                onSettingsClick = onNavigateToSettings,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateNote,
                modifier = Modifier.padding(16.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Note",
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            // Filter chips
            if (uiState.categories.isNotEmpty() || uiState.tags.isNotEmpty()) {
                FilterSection(
                    categories = uiState.categories,
                    tags = uiState.tags,
                    selectedCategory = uiState.selectedCategory,
                    selectedTags = uiState.selectedTags,
                    onCategorySelected = viewModel::filterByCategory,
                    onTagSelected = { tag ->
                        val currentTags = uiState.selectedTags.toMutableList()
                        if (currentTags.contains(tag)) {
                            currentTags.remove(tag)
                        } else {
                            currentTags.add(tag)
                        }
                        viewModel.filterByTags(currentTags)
                    },
                    onClearFilters = viewModel::clearFilters,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            // Notes content
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.filteredNotes.isEmpty() -> {
                    NotesEmptyState(
                        hasNotes = uiState.notes.isNotEmpty(),
                        searchQuery = uiState.searchQuery,
                        onCreateNote = onNavigateToCreateNote,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                else -> {
                    NotesContent(
                        notes = uiState.filteredNotes,
                        viewMode = uiState.viewMode,
                        onNoteClick = onNavigateToEditNote,
                        onTogglePin = viewModel::toggleNotePin,
                        onToggleFavorite = viewModel::toggleNoteFavorite,
                        onArchive = viewModel::archiveNote,
                        onDelete = viewModel::deleteNote,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }

        // Sort bottom sheet
        if (showSortBottomSheet) {
            SortBottomSheet(
                currentSortOrder = uiState.sortOrder,
                onSortOrderSelected = { sortOrder ->
                    viewModel.changeSortOrder(sortOrder)
                    showSortBottomSheet = false
                },
                onDismiss = { showSortBottomSheet = false },
            )
        }
    }

    // Handle error messages
    uiState.errorMessage?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Error is already handled by SnackbarManager in ViewModel
        }
    }
}

@Composable
private fun FilterSection(
    categories: List<Category>,
    tags: List<Tag>,
    selectedCategory: Category?,
    selectedTags: List<Tag>,
    onCategorySelected: (Category?) -> Unit,
    onTagSelected: (Tag) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppLabel(text = "Filters")

            if (selectedCategory != null || selectedTags.isNotEmpty()) {
                TextButton(
                    onClick = onClearFilters,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text("Clear All", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (categories.isNotEmpty()) {
                item {
                    AppLabel(text = "Categories")
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(categories) { category ->
                            FilterChip(
                                selected = selectedCategory?.id == category.id,
                                onClick = {
                                    onCategorySelected(if (selectedCategory?.id == category.id) null else category)
                                },
                                label = category.name,
                                color = category.color,
                            )
                        }
                    }
                }
            }

            if (tags.isNotEmpty()) {
                item {
                    AppLabel(text = "Tags")
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(tags) { tag ->
                            FilterChip(
                                selected = selectedTags.any { it.id == tag.id },
                                onClick = { onTagSelected(tag) },
                                label = "#${tag.name}",
                                color = tag.color,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotesContent(
    notes: List<Note>,
    viewMode: ViewMode,
    onNoteClick: (Int) -> Unit,
    onTogglePin: (Int) -> Unit,
    onToggleFavorite: (Int) -> Unit,
    onArchive: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (viewMode) {
        ViewMode.LIST -> {
            LazyColumn(
                modifier = modifier,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(notes) { note ->
                    NoteCard(
                        note = note,
                        onClick = { onNoteClick(note.id) },
                        onTogglePin = { onTogglePin(note.id) },
                        onToggleFavorite = { onToggleFavorite(note.id) },
                        onArchive = { onArchive(note.id) },
                        onDelete = { onDelete(note.id) },
                    )
                }
            }
        }

        ViewMode.GRID -> {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = modifier,
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalItemSpacing = 8.dp,
            ) {
                items(notes) { note ->
                    NoteCard(
                        note = note,
                        onClick = { onNoteClick(note.id) },
                        onTogglePin = { onTogglePin(note.id) },
                        onToggleFavorite = { onToggleFavorite(note.id) },
                        onArchive = { onArchive(note.id) },
                        onDelete = { onDelete(note.id) },
                        isCompact = true,
                    )
                }
            }
        }
    }
}

// Preview-specific version of NotesScreen that takes UI state directly
@Composable
private fun NotesScreenContent(
    uiState: NotesUiState,
    onNavigateToCreateNote: () -> Unit,
    onNavigateToEditNote: (noteId: Int) -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onViewModeChange: (ViewMode) -> Unit,
    onSortClick: () -> Unit,
    onCategorySelected: (Category?) -> Unit,
    onTagSelected: (Tag) -> Unit,
    onClearFilters: () -> Unit,
    onTogglePin: (Int) -> Unit,
    onToggleFavorite: (Int) -> Unit,
    onArchive: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSortBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            NotesAppBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onClearSearch = onClearSearch,
                viewMode = uiState.viewMode,
                onViewModeChange = onViewModeChange,
                onSortClick = onSortClick,
                onCategoriesClick = onNavigateToCategories,
                onSettingsClick = onNavigateToSettings,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateNote,
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Note",
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            // Filters Section
            FilterSection(
                categories = uiState.categories,
                tags = uiState.tags,
                selectedCategory = uiState.selectedCategory,
                selectedTags = uiState.selectedTags,
                onCategorySelected = onCategorySelected,
                onTagSelected = onTagSelected,
                onClearFilters = onClearFilters,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            // Loading state
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            // Empty state
            else if (uiState.filteredNotes.isEmpty() && !uiState.isLoading) {
                NotesEmptyState(
                    searchQuery = uiState.searchQuery,
                    onCreateNote = onNavigateToCreateNote,
                    hasNotes = true,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            // Notes content
            else {
                NotesContent(
                    notes = uiState.filteredNotes,
                    viewMode = uiState.viewMode,
                    onNoteClick = onNavigateToEditNote,
                    onTogglePin = onTogglePin,
                    onToggleFavorite = onToggleFavorite,
                    onArchive = onArchive,
                    onDelete = onDelete,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    // Sort Bottom Sheet
    if (showSortBottomSheet) {
        SortBottomSheet(
            currentSortOrder = uiState.sortOrder,
            onSortOrderSelected = { /* Handle sort order change */ },
            onDismiss = { showSortBottomSheet = false },
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NotesScreenPreview() {
    MyApplicationTheme {
        NotesScreenContent(
            uiState = previewNotesUiState(),
            onNavigateToCreateNote = {},
            onNavigateToEditNote = {},
            onNavigateToCategories = {},
            onSearchQueryChange = {},
            onClearSearch = {},
            onViewModeChange = {},
            onSortClick = {},
            onCategorySelected = {},
            onTagSelected = {},
            onClearFilters = {},
            onTogglePin = {},
            onToggleFavorite = {},
            onArchive = {},
            onNavigateToSettings = {},
            onDelete = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NotesScreenGridPreview() {
    MyApplicationTheme {
        NotesScreenContent(
            uiState = previewNotesUiState().copy(viewMode = ViewMode.GRID),
            onNavigateToCreateNote = {},
            onNavigateToEditNote = {},
            onNavigateToCategories = {},
            onSearchQueryChange = {},
            onClearSearch = {},
            onViewModeChange = {},
            onSortClick = {},
            onCategorySelected = {},
            onTagSelected = {},
            onClearFilters = {},
            onTogglePin = {},
            onToggleFavorite = {},
            onArchive = {},
            onNavigateToSettings = {},
            onDelete = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NotesScreenEmptyPreview() {
    MyApplicationTheme {
        NotesScreenContent(
            uiState =
                NotesUiState(
                    isLoading = false,
                    filteredNotes = emptyList(),
                    categories = sampleCategories(),
                    tags = sampleTags(),
                ),
            onNavigateToCreateNote = {},
            onNavigateToEditNote = {},
            onNavigateToCategories = {},
            onSearchQueryChange = {},
            onClearSearch = {},
            onViewModeChange = {},
            onSortClick = {},
            onCategorySelected = {},
            onTagSelected = {},
            onClearFilters = {},
            onTogglePin = {},
            onToggleFavorite = {},
            onArchive = {},
            onNavigateToSettings = {},
            onDelete = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NotesScreenLoadingPreview() {
    MyApplicationTheme {
        NotesScreenContent(
            uiState = NotesUiState(isLoading = true),
            onNavigateToCreateNote = {},
            onNavigateToEditNote = {},
            onNavigateToCategories = {},
            onSearchQueryChange = {},
            onClearSearch = {},
            onViewModeChange = {},
            onSortClick = {},
            onCategorySelected = {},
            onTagSelected = {},
            onClearFilters = {},
            onTogglePin = {},
            onToggleFavorite = {},
            onArchive = {},
            onNavigateToSettings = {},
            onDelete = {},
        )
    }
}

// Sample data for previews
private fun previewNotesUiState(): NotesUiState {
    val categories = sampleCategories()
    val tags = sampleTags()
    val notes = sampleNotes(categories, tags)

    return NotesUiState(
        isLoading = false,
        notes = notes,
        filteredNotes = notes,
        categories = categories,
        tags = tags,
        searchQuery = "",
        selectedCategory = null,
        selectedTags = emptyList(),
        viewMode = ViewMode.LIST,
        sortOrder = SortOrder.UPDATED_DESC,
    )
}

private fun sampleCategories(): List<Category> =
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

private fun sampleTags(): List<Tag> =
    listOf(
        Tag(id = 1, userId = 1, name = "urgent", color = "#FF6B6B", usageCount = 3),
        Tag(id = 2, userId = 1, name = "meeting", color = "#4ECDC4", usageCount = 2),
        Tag(id = 3, userId = 1, name = "project", color = "#45B7D1", usageCount = 5),
        Tag(id = 4, userId = 1, name = "idea", color = "#FFA07A", usageCount = 4),
        Tag(id = 5, userId = 1, name = "todo", color = "#DDA0DD", usageCount = 6),
        Tag(id = 6, userId = 1, name = "research", color = "#98D8C8", usageCount = 2),
    )

private fun sampleNotes(
    categories: List<Category>,
    tags: List<Tag>,
): List<Note> =
    listOf(
        Note(
            id = 1,
            userId = 1,
            title = "Project Meeting Notes",
            content = "Discussed the new features for Q4. Need to finalize the design by Friday. John will handle the backend integration while Sarah focuses on the UI components.",
            category = categories[0], // Work
            tags = listOf(tags[1], tags[2]), // meeting, project
            createdAt = System.currentTimeMillis() - 86400000, // 1 day ago
            updatedAt = System.currentTimeMillis() - 3600000, // 1 hour ago
            isPinned = true,
            isFavorite = false,
            isArchived = false,
        ),
        Note(
            id = 2,
            userId = 1,
            title = "Grocery List",
            content = "• Milk\n• Bread\n• Eggs\n• Apples\n• Chicken\n• Rice\n• Yogurt\n• Bananas",
            category = categories[1], // Personal
            tags = listOf(tags[4]), // todo
            createdAt = System.currentTimeMillis() - 172800000, // 2 days ago
            updatedAt = System.currentTimeMillis() - 1800000, // 30 minutes ago
            isPinned = false,
            isFavorite = true,
            isArchived = false,
        ),
        Note(
            id = 3,
            userId = 1,
            title = "App Idea: Smart Garden",
            content = "An IoT-based smart garden monitoring app that tracks soil moisture, temperature, and light levels. Users can get notifications when plants need water or care.",
            category = categories[2], // Ideas
            tags = listOf(tags[3], tags[2]), // idea, project
            createdAt = System.currentTimeMillis() - 259200000, // 3 days ago
            updatedAt = System.currentTimeMillis() - 259200000, // 3 days ago
            isPinned = false,
            isFavorite = false,
            isArchived = false,
        ),
        Note(
            id = 4,
            userId = 1,
            title = "Kotlin Coroutines Study",
            content = "Key concepts:\n• Suspend functions\n• Dispatchers (Main, IO, Default)\n• ViewModelScope\n• Flow vs LiveData\n• Exception handling in coroutines",
            category = categories[3], // Learning
            tags = listOf(tags[5]), // research
            createdAt = System.currentTimeMillis() - 345600000, // 4 days ago
            updatedAt = System.currentTimeMillis() - 7200000, // 2 hours ago
            isPinned = false,
            isFavorite = true,
            isArchived = false,
        ),
        Note(
            id = 5,
            userId = 1,
            title = "Urgent: Fix Production Bug",
            content = "Critical issue in user authentication module. Users can't log in. Priority: HIGH\n\nSteps to reproduce:\n1. Open login screen\n2. Enter valid credentials\n3. Tap login\n4. Error occurs",
            category = categories[0], // Work
            tags = listOf(tags[0], tags[2]), // urgent, project
            createdAt = System.currentTimeMillis() - 432000000, // 5 days ago
            updatedAt = System.currentTimeMillis() - 900000, // 15 minutes ago
            isPinned = true,
            isFavorite = false,
            isArchived = false,
        ),
        Note(
            id = 6,
            userId = 1,
            title = "Weekend Plans",
            content = "Saturday:\n• Visit the farmers market\n• Movie night with friends\n\nSunday:\n• Hiking at Green Mountain\n• Meal prep for the week",
            category = categories[1], // Personal
            tags = listOf(tags[4]), // todo
            createdAt = System.currentTimeMillis() - 518400000, // 6 days ago
            updatedAt = System.currentTimeMillis() - 86400000, // 1 day ago
            isPinned = false,
            isFavorite = false,
            isArchived = false,
        ),
    )
