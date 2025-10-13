package com.cbo.notes.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.cbo.core.domain.model.ViewMode
import com.cbo.ui.components.AppHeadline
import com.cbo.ui.components.AppOutlinedTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesAppBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    viewMode: ViewMode,
    onViewModeChange: (ViewMode) -> Unit,
    onSortClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSearchActive by remember { mutableStateOf(false) }

    TopAppBar(
        modifier = modifier,
        title = {
            if (isSearchActive) {
                SearchTextField(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onClearQuery = {
                        onClearSearch()
                        isSearchActive = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                AppHeadline(text = "Notes")
            }
        },
        actions = {
            if (!isSearchActive) {
                // Search button
                IconButton(onClick = { isSearchActive = true }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                }

                // View mode toggle
                IconButton(
                    onClick = { 
                        onViewModeChange(
                            when (viewMode) {
                                ViewMode.LIST -> ViewMode.GRID
                                ViewMode.GRID -> ViewMode.COMPACT
                                ViewMode.COMPACT -> ViewMode.LIST
                            }
                        )
                    }
                ) {
                    Icon(
                        imageVector =
                            when (viewMode) {
                                ViewMode.LIST -> Icons.Default.ViewStream
                                ViewMode.GRID -> Icons.Default.GridView
                                ViewMode.COMPACT -> Icons.AutoMirrored.Filled.ViewList
                            }
                        ,
                        contentDescription = "Toggle view mode"
                    )
                }

                // Sort button
                IconButton(onClick = onSortClick) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Sort"
                    )
                }

                // More options menu
                var showMenu by remember { mutableStateOf(false) }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Manage Categories") },
                            onClick = {
                                onCategoriesClick()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Archive") },
                            onClick = {
                                // Handle archive navigation
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Archive,
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            onClick = {
                                onSettingsClick()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppOutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = "Search notes...",
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearQuery) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search"
                    )
                }
            }
        },
        singleLine = true
    )
}

@Preview
@Composable
fun PreviewSearchTextField() {
    MaterialTheme {
        Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
            SearchTextField(
                "",
                {},
                {},
            )
        }
    }
}

@Preview(showBackground = true, name = "Notes App Bar - Default (List View)")
@Composable
fun PreviewNotesAppBar_ListView() {
    MaterialTheme {
        NotesAppBar(
            searchQuery = "",
            onSearchQueryChange = {},
            onClearSearch = {},
            viewMode = ViewMode.LIST,
            onViewModeChange = {},
            onSortClick = {},
            onCategoriesClick = {},
            onSettingsClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Notes App Bar - Grid View")
@Composable
fun PreviewNotesAppBar_GridView() {
    MaterialTheme {
        NotesAppBar(
            searchQuery = "",
            onSearchQueryChange = {},
            onClearSearch = {},
            viewMode = ViewMode.GRID,
            onViewModeChange = {},
            onSortClick = {},
            onCategoriesClick = {},
            onSettingsClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Notes App Bar - Search Active")
@Composable
fun PreviewNotesAppBar_SearchActive() {
    MaterialTheme {
        var query by remember { mutableStateOf("Meeting notes") }

        // Simulate search mode by passing non-empty query
        NotesAppBar(
            searchQuery = query,
            onSearchQueryChange = { query = it },
            onClearSearch = { query = "" },
            viewMode = ViewMode.LIST,
            onViewModeChange = {},
            onSortClick = {},
            onCategoriesClick = {},
            onSettingsClick = {}
        )
    }
}

