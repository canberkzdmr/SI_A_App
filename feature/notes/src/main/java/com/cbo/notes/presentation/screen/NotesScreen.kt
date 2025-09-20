package com.cbo.notes.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cbo.notes.presentation.component.FilterChip
import com.cbo.notes.presentation.component.NoteCard
import com.cbo.notes.presentation.component.NotesAppBar
import com.cbo.notes.presentation.component.NotesEmptyState
import com.cbo.notes.presentation.component.SortBottomSheet
import com.cbo.notes.presentation.viewmodel.NotesViewModel
import com.cbo.notes.presentation.viewmodel.ViewMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onNavigateToCreateNote: () -> Unit,
    onNavigateToEditNote: (noteId: Int) -> Unit,
    onNavigateToCategories: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSortBottomSheet by remember { mutableStateOf(false) }

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
                onCategoriesClick = onNavigateToCategories
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateNote,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Note"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Notes content
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                uiState.filteredNotes.isEmpty() -> {
                    NotesEmptyState(
                        hasNotes = uiState.notes.isNotEmpty(),
                        searchQuery = uiState.searchQuery,
                        onCreateNote = onNavigateToCreateNote,
                        modifier = Modifier.fillMaxSize()
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
                        modifier = Modifier.fillMaxSize()
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
                onDismiss = { showSortBottomSheet = false }
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
    categories: List<com.cbo.notes.domain.model.Category>,
    tags: List<com.cbo.notes.domain.model.Tag>,
    selectedCategory: com.cbo.notes.domain.model.Category?,
    selectedTags: List<com.cbo.notes.domain.model.Tag>,
    onCategorySelected: (com.cbo.notes.domain.model.Category?) -> Unit,
    onTagSelected: (com.cbo.notes.domain.model.Tag) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (selectedCategory != null || selectedTags.isNotEmpty()) {
                TextButton(
                    onClick = onClearFilters,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Clear All", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (categories.isNotEmpty()) {
                item {
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { category ->
                            FilterChip(
                                selected = selectedCategory?.id == category.id,
                                onClick = { 
                                    onCategorySelected(if (selectedCategory?.id == category.id) null else category)
                                },
                                label = category.name,
                                color = category.color
                            )
                        }
                    }
                }
            }

            if (tags.isNotEmpty()) {
                item {
                    Text(
                        text = "Tags",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tags) { tag ->
                            FilterChip(
                                selected = selectedTags.any { it.id == tag.id },
                                onClick = { onTagSelected(tag) },
                                label = "#${tag.name}",
                                color = tag.color
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
    notes: List<com.cbo.notes.domain.model.Note>,
    viewMode: ViewMode,
    onNoteClick: (Int) -> Unit,
    onTogglePin: (Int) -> Unit,
    onToggleFavorite: (Int) -> Unit,
    onArchive: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    when (viewMode) {
        ViewMode.LIST -> {
            LazyColumn(
                modifier = modifier,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notes) { note ->
                    NoteCard(
                        note = note,
                        onClick = { onNoteClick(note.id) },
                        onTogglePin = { onTogglePin(note.id) },
                        onToggleFavorite = { onToggleFavorite(note.id) },
                        onArchive = { onArchive(note.id) },
                        onDelete = { onDelete(note.id) }
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
                verticalItemSpacing = 8.dp
            ) {
                items(notes) { note ->
                    NoteCard(
                        note = note,
                        onClick = { onNoteClick(note.id) },
                        onTogglePin = { onTogglePin(note.id) },
                        onToggleFavorite = { onToggleFavorite(note.id) },
                        onArchive = { onArchive(note.id) },
                        onDelete = { onDelete(note.id) },
                        isCompact = true
                    )
                }
            }
        }
    }
}

