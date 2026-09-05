package com.cbo.notes.presentation.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cbo.core.domain.model.ViewMode
import com.cbo.ui.components.AppHeadline
import com.cbo.ui.components.AppSearchField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesAppBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onCategoriesClick: () -> Unit,
    viewMode: ViewMode,
    onViewModeChange: (ViewMode) -> Unit,
    onArchiveClick: () -> Unit,
    onTrashClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isSearchActive by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    TopAppBar(
        modifier = modifier,
        navigationIcon = {
            if (isSearchActive) {
                IconButton(
                    onClick = {
                        isSearchActive = false
                        onClearSearch()
                        keyboardController?.hide()
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(id = com.cbo.notes.R.string.close_search),
                    )
                }
            }
        },
        title = {
            if (isSearchActive) {
                AppSearchField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    onClear = onClearSearch,
                    placeholder = stringResource(id = com.cbo.notes.R.string.search_notes_placeholder),
                    modifier =
                        Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                )
            } else {
                AppHeadline(text = stringResource(id = com.cbo.notes.R.string.notes_title))
            }
        },
        actions = {
            // ViewMode
            if (!isSearchActive) {
                IconButton(onClick = {
                    onViewModeChange(
                        when (viewMode) {
                            ViewMode.LIST -> ViewMode.GRID
                            ViewMode.GRID -> ViewMode.COMPACT
                            ViewMode.COMPACT -> ViewMode.LIST
                        },
                    )
                }) {
                    AnimatedContent(
                        targetState = viewMode,
                        transitionSpec = {
                            slideInVertically (
                                initialOffsetY = { -it },
                                animationSpec = tween(300),
                            ) togetherWith
                                    slideOutVertically (
                                        targetOffsetY = { it },
                                        animationSpec = tween(300),
                                    )
                        },
                        label = "fabIconAnim",
                    ) { viewMode ->
                        when (viewMode) {
                            ViewMode.LIST -> Icon(
                                imageVector = Icons.Default.ViewStream,
                                contentDescription = stringResource(com.cbo.notes.R.string.toggle_view_mode)
                            )
                            ViewMode.GRID -> Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = stringResource(com.cbo.notes.R.string.toggle_view_mode)
                            )
                            ViewMode.COMPACT -> Icon(
                                imageVector = Icons.AutoMirrored.Filled.ViewList,
                                contentDescription = stringResource(com.cbo.notes.R.string.toggle_view_mode)
                            )
                        }
                    }
                }
            }

            if (!isSearchActive) {
                IconButton(onClick = { isSearchActive = true }) {
                    Icon(Icons.Default.Search, contentDescription = stringResource(id = com.cbo.notes.R.string.search))
                }
            }

            // More options menu
            var showMenu by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(id = com.cbo.notes.R.string.more_options),
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(id = com.cbo.notes.R.string.manage_categories)) },
                        onClick = {
                            onCategoriesClick()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                            )
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(id = com.cbo.notes.R.string.archive)) },
                        onClick = {
                            onArchiveClick()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Archive,
                                contentDescription = null,
                            )
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(id = com.cbo.notes.R.string.trash)) },
                        onClick = {
                            onTrashClick()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.RestoreFromTrash,
                                contentDescription = null,
                            )
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(id = com.cbo.notes.R.string.settings)) },
                        onClick = {
                            onSettingsClick()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                            )
                        },
                    )
                }
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
            ),
    )

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            // Request focus and show keyboard when entering search mode
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }
}

@Preview(showBackground = true, name = "Notes App Bar")
@Composable
fun PreviewNotesAppBar() {
    MaterialTheme {
        NotesAppBar(
            searchQuery = "",
            onSearchQueryChange = {},
            onClearSearch = {},
            viewMode = ViewMode.COMPACT,
            onViewModeChange = {},
            onCategoriesClick = {},
            onSettingsClick = {},
            onArchiveClick = {},
            onTrashClick = {},
        )
    }
}
