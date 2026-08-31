package com.cbo.notes.presentation.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cbo.core.domain.model.ViewMode
import com.cbo.notes.R
import com.cbo.notes.presentation.component.FiltersBottomSheet
import com.cbo.notes.presentation.component.NotesEmptyState
import com.cbo.notes.presentation.component.SortBottomSheet
import com.cbo.notes.presentation.viewmodel.NotesViewModel
import com.cbo.ui.components.AppHeadline
import com.cbo.ui.components.AppSearchField
import com.cbo.ui.components.ScreenWithTopBarAndInsets
import com.cbo.ui.components.SecondaryButton
import com.cbo.ui.components.states.AppEmptyState
import com.cbo.ui.components.states.AppErrorState
import com.cbo.ui.components.states.AppLoadingScreen

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FilteredNotesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreateNote: () -> Unit,
    onNavigateToEditNote: (noteId: Int) -> Unit,
    onNavigateToFilters: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    val title = when {
        uiState.lockedCategoryId != null -> {
            val category = uiState.categories.find { it.id == uiState.lockedCategoryId }
            category?.name ?: stringResource(R.string.categories)
        }
        uiState.lockedTagId != null -> {
            val tag = uiState.tags.find { it.id == uiState.lockedTagId }
            tag?.name?.let { "#$it" } ?: stringResource(R.string.tags)
        }
        else -> stringResource(R.string.notes_title)
    }

    ScreenWithTopBarAndInsets(
        modifier = modifier,
        topBar = {
            var isSearchActive by remember { mutableStateOf(false) }
            val focusRequester = remember { FocusRequester() }
            val keyboardController = LocalSoftwareKeyboardController.current

            TopAppBar(
                navigationIcon = {
                    if (isSearchActive) {
                        IconButton(onClick = {
                            isSearchActive = false
                            viewModel.searchNotes("")
                            keyboardController?.hide()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close_search))
                        }
                    } else {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    }
                },
                title = {
                    if (isSearchActive) {
                        AppSearchField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::searchNotes,
                            onClear = { viewModel.searchNotes("") },
                            placeholder = stringResource(R.string.search_notes_placeholder),
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                        )
                    } else {
                        AppHeadline(text = title)
                    }
                },
                actions = {
                    if (!isSearchActive) {
                        IconButton(onClick = {
                            viewModel.changeViewMode(
                                when (uiState.viewMode) {
                                    ViewMode.LIST -> ViewMode.GRID
                                    ViewMode.GRID -> ViewMode.COMPACT
                                    ViewMode.COMPACT -> ViewMode.LIST
                                }
                            )
                        }) {
                            AnimatedContent(
                                targetState = uiState.viewMode,
                                transitionSpec = {
                                    slideInVertically(initialOffsetY = { -it }, animationSpec = tween(300)) togetherWith
                                            slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300))
                                },
                                label = "viewModeAnim"
                            ) { mode ->
                                when (mode) {
                                    ViewMode.LIST -> Icon(Icons.Default.ViewStream, contentDescription = null)
                                    ViewMode.GRID -> Icon(Icons.Default.GridView, contentDescription = null)
                                    ViewMode.COMPACT -> Icon(Icons.AutoMirrored.Filled.ViewList, contentDescription = null)
                                }
                            }
                        }

                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )

            LaunchedEffect(isSearchActive) {
                if (isSearchActive) {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(0.dp))
            var sortExpanded by remember { mutableStateOf(false) }

            when {
                uiState.isLoading -> {
                    AppLoadingScreen(
                        message = stringResource(R.string.loading_notes),
                        showProgress = true,
                    )
                }

                uiState.filteredNotes.isEmpty() -> {
                    val hasActiveFilters = uiState.selectedCategories.isNotEmpty() || uiState.selectedTags.isNotEmpty() || uiState.filterPinned || uiState.filterFavorites
                    val hasSearchQuery = uiState.searchQuery.isNotEmpty()

                    AppEmptyState(
                        title = when {
                            hasSearchQuery -> stringResource(R.string.no_matching_notes)
                            hasActiveFilters -> stringResource(R.string.no_matching_notes)
                            else -> stringResource(R.string.no_notes_yet)
                        },
                        message = when {
                            hasSearchQuery -> stringResource(R.string.try_adjusting_search)
                            hasActiveFilters -> stringResource(R.string.try_adjusting_filters)
                            else -> stringResource(R.string.start_creating_note)
                        },
                        actionText = stringResource(R.string.create_note),
                        onAction = onNavigateToCreateNote,
                        secondaryActionText = when {
                            hasSearchQuery && hasActiveFilters -> stringResource(R.string.clear_all_filters)
                            hasSearchQuery -> stringResource(R.string.clear_search)
                            hasActiveFilters -> stringResource(R.string.clear_filters)
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
                        error = uiState.errorMessage ?: stringResource(R.string.unexpected_error),
                        onRetry = { viewModel.clearFilters() },
                    )
                }

                else -> {
                    NotesContent(
                        notes = uiState.filteredNotes,
                        viewMode = uiState.viewMode,
                        header = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                SecondaryButton(
                                    text = stringResource(R.string.sort),
                                    onClick = { sortExpanded = true },
                                    leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null) },
                                    trailingIcon = {
                                        val rotation by animateFloatAsState(
                                            targetValue = if (sortExpanded) 180f else 0f,
                                            animationSpec = tween(durationMillis = 300),
                                            label = "sortExpandRotation"
                                        )
                                        Icon(Icons.Default.ExpandMore, contentDescription = null, modifier = Modifier.rotate(rotation))
                                    },
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                )
                                SecondaryButton(
                                    text = stringResource(R.string.filters),
                                    onClick = onNavigateToFilters,
                                    leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth().weight(1f),
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
            
            if (sortExpanded) {
                SortBottomSheet(
                    currentSortOrder = uiState.sortOrder,
                    onSortOrderSelected = {
                        viewModel.changeSortOrder(it)
                        sortExpanded = false
                    },
                    onDismiss = { sortExpanded = false }
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun FilteredNotesScreenPreview() {
    com.cbo.ui.theme.MemCloudApplicationTheme {
        // ViewModel is mocked by hilt or omitted in this context,
        // so we could define a simpler stateless version if we want 
        // to preview it perfectly, but for now we provide the signature.
    }
}
