package com.cbo.notes.presentation.screen

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cbo.notes.R
import com.cbo.notes.domain.model.Category
import com.cbo.notes.domain.model.Note
import com.cbo.notes.presentation.component.CompactNoteCard
import com.cbo.notes.presentation.viewmodel.DeleteArchiveMode
import com.cbo.notes.presentation.viewmodel.DeletedArchivedNotesUiState
import com.cbo.notes.presentation.viewmodel.DeletedArchivedNotesViewModel
import com.cbo.ui.components.AppTabItem
import com.cbo.ui.components.AppTabRow
import com.cbo.ui.components.AppTitle
import com.cbo.ui.components.ScreenWithTopBarAndInsets
import com.cbo.ui.components.cards.HeaderCard
import com.cbo.ui.components.states.AppEmptyState
import com.cbo.ui.components.states.AppLoadingScreen
import com.cbo.ui.theme.MemCloudApplicationTheme
import kotlinx.coroutines.launch

@Composable
fun DeletedArchivedNotesScreen(
    onNavigateBack: () -> Unit,
    viewModel: DeletedArchivedNotesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onRestoreNote = viewModel::restoreNote,
        onTabSelected = viewModel::switchTab
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ScreenContent(
    modifier: Modifier = Modifier,
    uiState: DeletedArchivedNotesUiState,
    onNavigateBack: () -> Unit,
    onRestoreNote: (Int) -> Unit,
    onTabSelected: (Int) -> Unit,
) {
    val tabs = listOf(
        AppTabItem(title = DeleteArchiveMode.DELETE.toDisplayName()),
        AppTabItem(title = DeleteArchiveMode.ARCHIVE.toDisplayName()),
    )

    val initialPage = uiState.viewMode.toTabIndex()
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { tabs.size }
    )
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        val newMode = pagerState.currentPage.toDeleteArchiveMode()
        if (uiState.viewMode != newMode) {
            onTabSelected(pagerState.currentPage)
        }
    }

    ScreenWithTopBarAndInsets(
        modifier = modifier,
        topBar = {
            TopAppBar(
                // Title updates immediately based on Pager State
                title = { AppTitle(pagerState.currentPage.toDeleteArchiveMode().toDisplayName()) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            AppLoadingScreen(
                message = stringResource(id = R.string.loading_notes),
                showProgress = true,
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
            ) {
                AppTabRow(
                    tabs = tabs,
                    selectedTabIndex = pagerState.currentPage,
                    onTabSelected = { index ->
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                )

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { pageIndex ->

                    val pageMode = pageIndex.toDeleteArchiveMode()

                    val notesForPage = when (pageMode) {
                        DeleteArchiveMode.DELETE -> uiState.filteredDeletedNotes
                        DeleteArchiveMode.ARCHIVE -> uiState.filteredArchivedNotes
                    }

                    if (notesForPage.isEmpty()) {
                        when (pageMode) {
                            DeleteArchiveMode.DELETE -> AppEmptyState(
                                icon = Icons.Default.DeleteSweep,
                                title = stringResource(R.string.no_deleted_notes_title),
                                message = stringResource(R.string.no_deleted_notes_message),
                            )
                            DeleteArchiveMode.ARCHIVE -> AppEmptyState(
                                icon = Icons.Default.Archive,
                                title = stringResource(R.string.no_archived_notes_title),
                                message = stringResource(R.string.no_archived_notes_message),
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom = paddingValues.calculateBottomPadding() + 16.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                when (pageMode) {
                                    DeleteArchiveMode.DELETE -> {
                                        HeaderCard(
                                            icon = Icons.Default.RestoreFromTrash,
                                            title = stringResource(R.string.deleted_notes_header_title),
                                            content = stringResource(R.string.deleted_notes_header_message)
                                        )
                                    }
                                    DeleteArchiveMode.ARCHIVE -> {
                                        HeaderCard(
                                            icon = Icons.Default.Archive,
                                            title = stringResource(R.string.archived_notes_header_title),
                                            content = stringResource(R.string.archived_notes_header_message)
                                        )
                                    }
                                }
                            }

                            items(notesForPage) { note ->
                                CompactNoteCard(
                                    note = note,
                                    onRestore = { onRestoreNote(note.id) },
                                    onClick = { /* Navigate */ },
                                    showPinButton = false,
                                    showMenu = false,
                                    enableSwipe = false,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Extension to get display name
@Composable
fun DeleteArchiveMode.toDisplayName(): String {
    return when (this) {
        DeleteArchiveMode.DELETE -> stringResource(R.string.deleted)
        DeleteArchiveMode.ARCHIVE -> stringResource(R.string.archive)
    }
}

// Extension to get tab index
fun DeleteArchiveMode.toTabIndex(): Int {
    return when (this) {
        DeleteArchiveMode.DELETE -> 0
        DeleteArchiveMode.ARCHIVE -> 1
    }
}

// Extension to convert index back to enum
internal fun Int.toDeleteArchiveMode(): DeleteArchiveMode {
    return when (this) {
        0 -> DeleteArchiveMode.DELETE
        1 -> DeleteArchiveMode.ARCHIVE
        else -> DeleteArchiveMode.DELETE // default
    }
}

@Preview(name = "DeletedArchivedNotes-Light")
@Preview(name = "DeletedArchivedNotes-Light", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DeletedArchivedNotes_Preview() {
    MemCloudApplicationTheme {
        ScreenContent(
            uiState = DeletedArchivedNotesUiState(
                isLoading = false,
                deletedNotes = sampleNotes(sampleCategories()),
                filteredDeletedNotes = sampleNotes(sampleCategories()),
                archivedNotes = sampleNotes(sampleCategories()),
                filteredArchivedNotes = sampleNotes(sampleCategories()),
                viewMode = DeleteArchiveMode.DELETE
            ),
            onNavigateBack = {},
            onRestoreNote = {},
            onTabSelected = {},
        )
    }
}

private fun sampleNotes(
    categories: List<Category>,
): List<Note> =
    listOf(
        Note(
            id = 1,
            userId = 1,
            title = "Project Meeting Notes",
            content = "Discussed the new features for Q4. Need to finalize the design by Friday. John will handle the backend integration while Sarah focuses on the UI components.",
            category = categories[0], // Work
            tags = emptyList(), // meeting, project
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
            category = categories[0], // Personal
            tags = emptyList(), // to-do
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
            category = categories[0], // Ideas
            tags = emptyList(), // idea, project
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
            category = categories[0], // Learning
            tags = emptyList(), // research
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
            tags = emptyList(), // urgent, project
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
            category = categories[0], // Personal
            tags = emptyList(), // to-do
            createdAt = System.currentTimeMillis() - 518400000, // 6 days ago
            updatedAt = System.currentTimeMillis() - 86400000, // 1 day ago
            isPinned = false,
            isFavorite = false,
            isArchived = false,
        ),
    )

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
        )