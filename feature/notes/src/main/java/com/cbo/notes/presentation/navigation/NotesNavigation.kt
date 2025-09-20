package com.cbo.notes.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.cbo.notes.presentation.screen.CategoriesScreen
import com.cbo.notes.presentation.screen.EditNoteScreen
import com.cbo.notes.presentation.screen.NotesScreen

const val NOTES_ROUTE = "notes"
const val CREATE_NOTE_ROUTE = "create_note"
const val EDIT_NOTE_ROUTE = "edit_note"
const val CATEGORIES_ROUTE = "categories"

fun NavController.navigateToNotes(navOptions: NavOptions? = null) {
    this.navigate(NOTES_ROUTE, navOptions)
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

fun NavGraphBuilder.notesGraph(
    onNavigateBack: () -> Unit,
    onNavigateToCreateNote: () -> Unit,
    onNavigateToEditNote: (noteId: Int) -> Unit,
    onNavigateToCategories: () -> Unit
) {
    composable(route = NOTES_ROUTE) {
        NotesScreen(
            onNavigateToCreateNote = onNavigateToCreateNote,
            onNavigateToEditNote = onNavigateToEditNote,
            onNavigateToCategories = onNavigateToCategories
        )
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
}
