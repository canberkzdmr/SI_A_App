package com.cbo.notes.presentation.screen

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells.Fixed
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cbo.core.domain.model.ViewMode
import com.cbo.notes.R
import com.cbo.notes.domain.model.Category
import com.cbo.notes.domain.model.Note
import com.cbo.notes.domain.model.Tag
import com.cbo.notes.presentation.SortOrder
import com.cbo.notes.presentation.component.CompactNoteCard
import com.cbo.notes.presentation.component.FiltersBottomSheet
import com.cbo.notes.presentation.component.ImprovedFilterSection
import com.cbo.notes.presentation.component.NoteCard
import com.cbo.notes.presentation.component.NotesAppBar
import com.cbo.notes.presentation.component.NotesEmptyState
import com.cbo.notes.presentation.component.SortBottomSheet
import com.cbo.notes.presentation.viewmodel.DeleteArchiveMode
import com.cbo.notes.presentation.viewmodel.NotesUiState
import com.cbo.notes.presentation.viewmodel.NotesViewModel
import com.cbo.ui.components.ScreenWithTopBarAndInsets
import com.cbo.ui.components.SecondaryButton
import com.cbo.ui.components.states.AppEmptyState
import com.cbo.ui.components.states.AppErrorState
import com.cbo.ui.components.states.AppLoadingScreen
import com.cbo.ui.theme.MemCloudApplicationTheme

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NotesScreen(
    onNavigateToCreateNote: () -> Unit,
    onNavigateToEditNote: (noteId: Int) -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToDeletedArchived: (Int) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToFilters: () -> Unit = {},
    initialCategoryId: Int? = null,
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler {
        Log.i("NotesScreen", "Back button is disabled for Notes Screen")
    }

    LaunchedEffect(initialCategoryId, uiState.categories) {
        if (initialCategoryId != null && uiState.categories.isNotEmpty()) {
            val category = uiState.categories.firstOrNull { it.id == initialCategoryId }
            category?.let { viewModel.filterByCategories(listOf(it)) }
        }
    }

    ScreenWithTopBarAndInsets(
        modifier = modifier,
        topBar = {
            NotesAppBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::searchNotes,
                onClearSearch = { viewModel.searchNotes("") },
                onCategoriesClick = onNavigateToCategories,
                onArchiveClick = { onNavigateToDeletedArchived(DeleteArchiveMode.ARCHIVE.toTabIndex()) },
                onTrashClick = { onNavigateToDeletedArchived(DeleteArchiveMode.DELETE.toTabIndex()) },
                onSettingsClick = onNavigateToSettings,
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(0.dp))

            // Search, Sort, View Mode and Filters Controls
            var showFiltersSheet by remember { mutableStateOf(false) }
            var sortExpanded by remember { mutableStateOf(false) }

            // Notes content (controls move into scroll via header)
            when {
                uiState.isLoading -> {
                    AppLoadingScreen(
                        message = stringResource(id = com.cbo.notes.R.string.loading_notes),
                        showProgress = true,
                    )
                }

                uiState.filteredNotes.isEmpty() -> {
                    val hasActiveFilters = uiState.selectedCategories.isNotEmpty() || uiState.selectedTags.isNotEmpty() || uiState.filterPinned || uiState.filterFavorites
                    val hasSearchQuery = uiState.searchQuery.isNotEmpty()
                    val hasAnyFilter = hasActiveFilters || hasSearchQuery

                    AppEmptyState(
                        title = when {
                            hasSearchQuery -> stringResource(id = com.cbo.notes.R.string.no_matching_notes)
                            hasActiveFilters -> stringResource(id = com.cbo.notes.R.string.no_matching_notes)
                            else -> stringResource(id = com.cbo.notes.R.string.no_notes_yet)
                        },
                        message = when {
                            hasSearchQuery -> stringResource(id = com.cbo.notes.R.string.try_adjusting_search)
                            hasActiveFilters -> stringResource(id = com.cbo.notes.R.string.try_adjusting_filters)
                            else -> stringResource(id = com.cbo.notes.R.string.start_creating_note)
                        },
                        actionText = stringResource(id = com.cbo.notes.R.string.create_note),
                        onAction = onNavigateToCreateNote,
                        secondaryActionText = when {
                            hasSearchQuery && hasActiveFilters -> stringResource(id = com.cbo.notes.R.string.clear_all_filters)
                            hasSearchQuery -> stringResource(id = com.cbo.notes.R.string.clear_search)
                            hasActiveFilters -> stringResource(id = com.cbo.notes.R.string.clear_filters)
                            else -> null
                        },
                        onSecondaryAction = {
                            viewModel.searchNotes("")
                            viewModel.clearFilters()
                        },
                    )
                }

                uiState.errorMessage != null -> {
                    AppErrorState(
                        error = uiState.errorMessage ?: stringResource(id = com.cbo.notes.R.string.unexpected_error),
                        onRetry = {
                            // Retry loading notes - refresh by clearing filters
                            viewModel.clearFilters()
                        },
                    )
                }

                else -> {
                    NotesContent(
                        notes = uiState.filteredNotes,
                        viewMode = uiState.viewMode,
                        header = {
                            Row(modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SecondaryButton(
                                    text = stringResource(R.string.sort),
                                    onClick = {
                                        sortExpanded = true
                                    },
                                    leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null) },
                                    trailingIcon = {
                                        val rotation by animateFloatAsState(
                                            targetValue = if (sortExpanded) 180f else 0f,
                                            animationSpec = tween(durationMillis = 300),
                                            label = "sortExpandRotation"
                                        )

                                        Icon(
                                            imageVector = Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            modifier = Modifier.rotate(rotation)
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                                SecondaryButton(
                                    text = stringResource(R.string.filters),
                                    onClick = onNavigateToFilters,
                                    leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                            }
                        },
                        onNoteClick = onNavigateToEditNote,
                        onTogglePin = viewModel::toggleNotePin,
                        onToggleFavorite = viewModel::toggleNoteFavorite,
                        onArchive = viewModel::archiveNote,
                        onDelete = viewModel::deleteNote,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            // Filters bottom sheet host
            if (showFiltersSheet) {
                FiltersBottomSheet(
                    allCategories = uiState.categories,
                    allTags = uiState.tags,
                    selectedCategories = uiState.selectedCategories,
                    selectedTags = uiState.selectedTags,
                    onUpdateSelectedCategories = { categories ->
                        viewModel.filterByCategories(categories)
                    },
                    onUpdateSelectedTags = { tags ->
                        viewModel.filterByTags(tags)
                    },
                    onApply = { categories, tags ->
                        viewModel.filterByCategories(categories)
                        viewModel.filterByTags(tags)
                        showFiltersSheet = false
                    },
                    onClearAll = {
                        viewModel.clearFilters()
                        showFiltersSheet = false
                    },
                    onDismiss = { showFiltersSheet = false },
                )
            }

            if (sortExpanded) {
                SortBottomSheet(
                    currentSortOrder = uiState.sortOrder,
                    onSortOrderSelected = { sortOption ->
                        viewModel.changeSortOrder(sortOption)
                        sortExpanded = false
                      },
                    onDismiss = { sortExpanded = false },
                )
            }
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
    onManageFiltersClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Legacy implementation removed. Use ImprovedFilterSection instead.
    ImprovedFilterSection(
        categories = categories,
        tags = tags,
        selectedCategory = selectedCategory,
        selectedTags = selectedTags,
        onCategorySelected = onCategorySelected,
        onTagSelected = onTagSelected,
        onClearFilters = onClearFilters,
        onManageFiltersClick = onManageFiltersClick,
        modifier = modifier,
    )
}

@Composable
private fun NotesContent(
    notes: List<Note>,
    viewMode: ViewMode,
    header: (@Composable () -> Unit)? = null,
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
                contentPadding = PaddingValues(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (header != null) {
                    item {
                        header()
                        Spacer(Modifier.height(8.dp))
                    }
                }
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
                columns = Fixed(2),
                modifier = modifier,
                contentPadding = PaddingValues(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalItemSpacing = 8.dp,
            ) {
                if (header != null) {
                    item(span = StaggeredGridItemSpan.FullLine) { header() }
                }
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

        ViewMode.COMPACT -> {
            LazyColumn(
                modifier = modifier,
                contentPadding = PaddingValues(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (header != null) {
                    item {
                        header()
                        Spacer(Modifier.height(8.dp))
                    }
                }
                itemsIndexed(notes) { index, note ->
                    CompactNoteCard(
                        note = note,
                        onClick = { onNoteClick(note.id) },
                        onTogglePin = { onTogglePin(note.id) },
                        onToggleFavorite = { onToggleFavorite(note.id) },
                        onArchive = { onArchive(note.id) },
                        onDelete = { onDelete(note.id) },
                        isFirstItem = index == 0,
                    )
                }
            }
        }
    }
}

@Composable
private fun NotesScreenPreviewHost(initialUiState: NotesUiState) {
    var searchQuery by remember { mutableStateOf(initialUiState.searchQuery) }
    var viewMode by remember { mutableStateOf(initialUiState.viewMode) }
    var sortOrder by remember { mutableStateOf(initialUiState.sortOrder) }
    var selectedCategories by remember { mutableStateOf(initialUiState.selectedCategories) }
    var selectedTags by remember { mutableStateOf(initialUiState.selectedTags) }
    var showFiltersSheet by remember { mutableStateOf(false) }
    var sortExpanded by remember { mutableStateOf(false) }

    val uiState =
        initialUiState.copy(
            searchQuery = searchQuery,
            viewMode = viewMode,
            sortOrder = sortOrder,
            selectedCategories = selectedCategories,
            selectedTags = selectedTags,
        )

    MemCloudApplicationTheme {
        ScreenWithTopBarAndInsets(
            topBar = {
                NotesAppBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onClearSearch = { searchQuery = "" },
                    onArchiveClick = {},
                    onTrashClick = {},
                    onCategoriesClick = {},
                    onSettingsClick = {},
                )
            },
        ) { paddingValues ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Content
                when {
                    uiState.isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    uiState.filteredNotes.isEmpty() -> {
                        NotesEmptyState(
                            hasNotes = uiState.notes.isNotEmpty(),
                            searchQuery = searchQuery,
                            onCreateNote = {},
                            onClearFilters = {},
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    else -> {
                        NotesContent(
                            notes = uiState.filteredNotes,
                            viewMode = viewMode,
                            header = {
                                Row(modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SecondaryButton(
                                        text = stringResource(R.string.sort),
                                        onClick = {
                                            sortExpanded = true
                                        },
                                        leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null) },
                                        trailingIcon = {
                                            val rotation by animateFloatAsState(
                                                targetValue = if (sortExpanded) 180f else 0f,
                                                animationSpec = tween(durationMillis = 300),
                                                label = "sortExpandRotation"
                                            )

                                            Icon(
                                                imageVector = Icons.Default.ExpandMore,
                                                contentDescription = null,
                                                modifier = Modifier.rotate(rotation)
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                    )
                                SecondaryButton(
                                    text = stringResource(R.string.filters),
                                    onClick = { showFiltersSheet = true },
                                    leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                            }
                        },
                        onNoteClick = {},
                            onTogglePin = {},
                            onToggleFavorite = {},
                            onArchive = {},
                            onDelete = {},
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                if (showFiltersSheet) {
                    FiltersBottomSheet(
                        allCategories = uiState.categories,
                        allTags = uiState.tags,
                        selectedCategories = selectedCategories,
                        selectedTags = selectedTags,
                        onUpdateSelectedCategories = { c ->
                            selectedCategories = c
                        },
                        onUpdateSelectedTags = { t ->
                            selectedTags = t
                        },
                        onApply = { c, t ->
                            selectedCategories = c
                            selectedTags = t
                            showFiltersSheet = false
                        },
                        onClearAll = {
                            selectedCategories = emptyList()
                            selectedTags = emptyList()
                            showFiltersSheet = false
                        },
                        onDismiss = { showFiltersSheet = false },
                    )
                }

                if (sortExpanded) {
                    SortBottomSheet(
                        currentSortOrder = uiState.sortOrder,
                        onSortOrderSelected = {
                            sortExpanded = false
                        },
                        onDismiss = { sortExpanded = false }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Notes • List")
@Composable
private fun NotesScreenListPreview() {
    NotesScreenPreviewHost(previewNotesUiState().copy(viewMode = ViewMode.LIST))
}

@Preview(showBackground = true, showSystemUi = true, name = "Notes • Grid")
@Composable
private fun NotesScreenGridPreview() {
    NotesScreenPreviewHost(previewNotesUiState().copy(viewMode = ViewMode.GRID))
}

@Preview(showBackground = true, showSystemUi = true, name = "Notes • Compact")
@Composable
private fun NotesScreenCompactPreview() {
    NotesScreenPreviewHost(previewNotesUiState().copy(viewMode = ViewMode.COMPACT))
}

@Preview(showBackground = true, showSystemUi = true, name = "Notes • Empty")
@Composable
private fun NotesScreenEmptyPreview() {
    NotesScreenPreviewHost(
        previewNotesUiState().copy(
            notes = emptyList(),
            filteredNotes = emptyList(),
        ),
    )
}

@Preview(showBackground = true, showSystemUi = true, name = "Notes • Loading")
@Composable
private fun NotesScreenLoadingPreview() {
    NotesScreenPreviewHost(previewNotesUiState().copy(isLoading = true))
}

@Preview(showBackground = true, showSystemUi = true, name = "Notes • Filters Applied")
@Composable
private fun NotesScreenFiltersAppliedPreview() {
    val base = previewNotesUiState()
    NotesScreenPreviewHost(
        base.copy(
            selectedCategories = base.categories.take(1),
            selectedTags = base.tags.take(3),
        ),
    )
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
        selectedCategories = emptyList(),
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
            tags = listOf(tags[4]), // to-do
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
            tags = listOf(tags[4]), // to-do
            createdAt = System.currentTimeMillis() - 518400000, // 6 days ago
            updatedAt = System.currentTimeMillis() - 86400000, // 1 day ago
            isPinned = false,
            isFavorite = false,
            isArchived = false,
        ),
    )
