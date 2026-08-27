package com.cbo.notes.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.cbo.notes.presentation.component.FilterDetailScreen
import com.cbo.notes.presentation.component.FilterListScreen
import com.cbo.notes.presentation.component.FilterType
import com.cbo.notes.presentation.screen.CategoriesScreen
import com.cbo.notes.presentation.screen.DeletedArchivedNotesScreen
import com.cbo.notes.presentation.screen.EditNoteScreen
import com.cbo.notes.presentation.screen.NotesScreen
import com.cbo.notes.presentation.screen.TagsScreen
import com.cbo.notes.presentation.screen.toTabIndex
import com.cbo.notes.presentation.viewmodel.DeleteArchiveMode
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import com.cbo.notes.presentation.viewmodel.NotesViewModel

const val NOTES_ROUTE = "notes"
const val CREATE_NOTE_ROUTE = "create_note"
const val EDIT_NOTE_ROUTE = "edit_note"
const val CATEGORIES_ROUTE = "categories"
const val TAGS_ROUTE = "tags"
const val DELETED_ARCHIVED_ROUTE = "deleted_archived"
const val FILTER_LIST_ROUTE = "filter_list"
const val FILTER_DETAIL_ROUTE = "filter_detail"
const val CALENDAR_ROUTE = "calendar"
const val MAP_ROUTE = "map"

fun NavController.navigateToNotes(navOptions: NavOptions? = null) {
    this.navigate(NOTES_ROUTE, navOptions)
}

fun NavController.navigateToNotes(categoryId: Int, navOptions: NavOptions? = null) {
    this.navigate("$NOTES_ROUTE?categoryId=$categoryId", navOptions)
}

fun NavController.navigateToCreateNote(navOptions: NavOptions? = null) {
    this.navigate(CREATE_NOTE_ROUTE, navOptions)
}

fun NavController.navigateToEditNote(noteId: Int, navOptions: NavOptions? = null) {
    this.navigate("$EDIT_NOTE_ROUTE/$noteId", navOptions)
}

fun NavController.navigateToCategories(navOptions: NavOptions? = null) {
    this.navigate(CATEGORIES_ROUTE, navOptions)
}

fun NavController.navigateToTags(navOptions: NavOptions? = null) {
    this.navigate(TAGS_ROUTE, navOptions)
}

fun NavController.navigateToDeletedArchived(tabId: Int, navOptions: NavOptions? = null) {
    this.navigate("$DELETED_ARCHIVED_ROUTE/$tabId", navOptions)
}

fun NavController.navigateToFilterList(navOptions: NavOptions? = null) {
    this.navigate(FILTER_LIST_ROUTE, navOptions)
}

fun NavController.navigateToFilterDetail(filterType: String, navOptions: NavOptions? = null) {
    this.navigate("$FILTER_DETAIL_ROUTE/$filterType", navOptions)
}

fun NavGraphBuilder.notesGraph(
    navController: NavController,
    onNavigateBack: () -> Unit,
    onNavigateToCreateNote: () -> Unit,
    onNavigateToEditNote: (noteId: Int) -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToTags: () -> Unit,
    onNavigateToDeletedArchived: (tabId: Int) -> Unit,
    onOpenNotesForCategory: (Int) -> Unit,
) {
    navigation(
        route = "notes_flow",
        startDestination = "$NOTES_ROUTE?categoryId={categoryId}"
    ) {
        composable(
            route = "$NOTES_ROUTE?categoryId={categoryId}",
            arguments = listOf(
                navArgument("categoryId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val categoryArg = backStackEntry.arguments?.getInt("categoryId") ?: -1
            val initialCategoryId = if (categoryArg != -1) categoryArg else null
            NotesScreen(
                onNavigateToCreateNote = onNavigateToCreateNote,
                onNavigateToEditNote = onNavigateToEditNote,
                onNavigateToCategories = onNavigateToCategories,
                onNavigateToDeletedArchived = onNavigateToDeletedArchived,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToFilters = {
                    navController.navigateToFilterList()
                },
                initialCategoryId = initialCategoryId,
            )
        }

        composable(FILTER_LIST_ROUTE) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("notes_flow")
            }
            val notesViewModel: NotesViewModel = hiltViewModel(parentEntry)
            val uiState by notesViewModel.uiState.collectAsStateWithLifecycle()

            FilterListScreen(
                selectedCategories = uiState.selectedCategories,
                selectedTags = uiState.selectedTags,
                filterPinned = uiState.filterPinned,
                filterFavorites = uiState.filterFavorites,
                onNavigateBack = { navController.popBackStack() },
                onFilterTypeClick = { filterType ->
                    navController.navigateToFilterDetail(filterType.name)
                },
                onPinnedToggle = { notesViewModel.toggleFilterPinned() },
                onFavoritesToggle = { notesViewModel.toggleFilterFavorites() },
                onClearAllFilters = {
                    notesViewModel.clearFilters()
                }
            )
        }

        composable(
            route = "$FILTER_DETAIL_ROUTE/{filterType}",
            arguments = listOf(
                navArgument("filterType") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val filterTypeArg = backStackEntry.arguments?.getString("filterType") ?: FilterType.CATEGORY.name
            val filterType = FilterType.valueOf(filterTypeArg)

            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("notes_flow")
            }
            val notesViewModel: NotesViewModel = hiltViewModel(parentEntry)
            val uiState by notesViewModel.uiState.collectAsStateWithLifecycle()

            FilterDetailScreen(
                filterType = filterType,
                categories = uiState.categories,
                tags = uiState.tags,
                selectedCategories = uiState.selectedCategories,
                selectedTags = uiState.selectedTags,
                onCategoryToggled = { category ->
                    notesViewModel.toggleCategory(category)
                },
                onTagToggled = { tag ->
                    notesViewModel.toggleTag(tag)
                },
                onApply = {
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }

    composable(route = CREATE_NOTE_ROUTE) {
        EditNoteScreen(
            onNavigateBack = onNavigateBack
        )
    }

    composable(
        route = "$EDIT_NOTE_ROUTE/{noteId}",
        arguments = listOf(
            navArgument("noteId") {
                type = NavType.IntType
                defaultValue = 0
            }
        )
    ) {
        EditNoteScreen(
            onNavigateBack = onNavigateBack
        )
    }

    composable(route = CATEGORIES_ROUTE) {
        CategoriesScreen(
            onNavigateBack = onNavigateBack,
            onOpenNotesForCategory = onOpenNotesForCategory,
        )
    }

    composable(route = TAGS_ROUTE) {
        TagsScreen(
            onNavigateBack = onNavigateBack
        )
    }

    composable(
        route = "$DELETED_ARCHIVED_ROUTE/{tabId}",
        arguments = listOf(
            navArgument("tabId") {
                type = NavType.IntType
                defaultValue = DeleteArchiveMode.DELETE.toTabIndex()
            }
        )
    ) {
        DeletedArchivedNotesScreen(
            onNavigateBack = onNavigateBack
        )
    }
}

// Sealed class for type-safe navigation
sealed class NotesDestination(val route: String) {
    object Notes : NotesDestination(NOTES_ROUTE)
    object CreateNote : NotesDestination(CREATE_NOTE_ROUTE)
    data class EditNote(val noteId: Int) : NotesDestination("$EDIT_NOTE_ROUTE/$noteId")
    object Categories : NotesDestination(CATEGORIES_ROUTE)
    data class DeletedArchived(val tabId: Int) : NotesDestination("$DELETED_ARCHIVED_ROUTE/$tabId")
    object FilterList : NotesDestination(FILTER_LIST_ROUTE)
    data class FilterDetail(val filterType: String) : NotesDestination("$FILTER_DETAIL_ROUTE/$filterType")
}
