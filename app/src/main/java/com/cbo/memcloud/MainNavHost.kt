package com.cbo.memcloud

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cbo.core.navigation.AppDestination
import com.cbo.login.presentation.navigation.loginNavGraph
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.cbo.notes.presentation.navigation.navigateToCategories
import com.cbo.notes.presentation.navigation.navigateToCategoryNotes
import com.cbo.notes.presentation.navigation.navigateToCreateNote
import com.cbo.notes.presentation.navigation.navigateToDeletedArchived
import com.cbo.notes.presentation.navigation.navigateToNotes
import com.cbo.notes.presentation.navigation.navigateToEditNote
import com.cbo.notes.presentation.navigation.navigateToTagNotes
import com.cbo.notes.presentation.navigation.navigateToTags
import com.cbo.notes.presentation.navigation.notesGraph
import com.cbo.splash.splashNavGraph
import com.cbo.user.presentation.navigation.userNavGraph
import com.cbo.notes.presentation.screen.CalendarScreen
import com.cbo.notes.presentation.screen.MapScreen
import com.cbo.notes.presentation.navigation.NOTES_ROUTE
import com.cbo.notes.presentation.navigation.CATEGORIES_ROUTE
import com.cbo.notes.presentation.navigation.TAGS_ROUTE

@Composable
fun MainNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.Splash.route,
        modifier = modifier
    ) {
        loginNavGraph(
            onLoginSuccess = {
                navController.navigate(NOTES_ROUTE) {
                    popUpTo(navController.graph.id) { inclusive = true }
                    launchSingleTop = true
                }
            },

            onRegisterClick = {
                navController.navigate(AppDestination.Register.route) {
                }
            },

            onRegisterSuccess = { _ ->
                navController.navigate(NOTES_ROUTE) {
                    popUpTo(navController.graph.id) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )

        userNavGraph(
            onLogOut = {
                Log.d("MainNavHost", "(userNavGraph) Navigated to login")
                navController.navigate(AppDestination.Login.createRoute()) {
                    popUpTo(AppDestination.Login.route) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            },
            onEditProfile = {
                navController.navigate(AppDestination.EditProfile.route)
            },
            onProfileUpdated = {
                Log.d("MainNavHost", "(userNavGraph) Profile updated, navigating to Main")
                navController.popBackStack()
            },
            onEditProfileCancelled = {
                Log.d("MainNavHost", "(userNavGraph) Edit profile cancelled, navigating to Main")
                navController.popBackStack()
            },
            onChangePassword = {
                Log.d("MainNavHost", "(userNavGraph) Navigated to Change Password")
                navController.navigate(AppDestination.ChangePassword.route)
            },
            onPasswordChanged = {
                Log.d("MainNavHost", "(userNavGraph) Password changed successfully")
                navController.popBackStack()
            },
            onChangePasswordCancelled = {
                Log.d("MainNavHost", "(userNavGraph) Change password cancelled")
                navController.popBackStack()
            },
            onChangeLanguage = {
                Log.d("MainNavHost", "(userNavGraph) Navigated to Change Language")
                navController.navigate(AppDestination.ChangeLanguage.route)
            },
            onNavigateBack = {
                Log.d("MainNavHost", "(userNavGraph) Change Language back")
                navController.popBackStack()
            },
            onDeleteUserClicked = {
                Log.d("MainNavHost", "(userNavGraph) Delete Account clicked")
            },
            onNotesClicked = {
                Log.d("MainNavHost", "(userNavGraph) Navigated to Notes")
                navController.navigate(NOTES_ROUTE) {
                    popUpTo(NOTES_ROUTE) {
                        this.saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            onCategoriesClicked = {
                Log.d("MainNavHost", "(userNavGraph) Navigated to Categories")
                navController.navigate(CATEGORIES_ROUTE) {
                    launchSingleTop = true
                }
            },
            onTagsClicked = {
                Log.d("MainNavHost", "(userNavGraph) Navigated to Tags")
                navController.navigate(TAGS_ROUTE) {
                    launchSingleTop = true
                }
            },
            onNavigateToStatistics = {
                Log.d("MainNavHost", "(userNavGraph) Navigated to Statistics")
                navController.navigate(AppDestination.Statistics.route)
            }
        )

        splashNavGraph(
            onNavigateToLogin = {
                navController.navigate(AppDestination.Login.createRoute()) {
                    popUpTo(navController.graph.id) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onNavigateToMain = {
                navController.navigate(NOTES_ROUTE) {
                    popUpTo(AppDestination.Login.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )

        composable("calendar") {
            CalendarScreen(
                onNavigateToEditNote = { noteId ->
                    navController.navigate("edit_note/$noteId")
                }
            )
        }

        composable("map") {
            MapScreen(
                onNavigateToEditNote = { noteId ->
                    navController.navigate("edit_note/$noteId")
                }
            )
        }

        notesGraph(
            navController = navController,
            onNavigateBack = {
                navController.popBackStack()
            },
            onNavigateToCreateNote = {
                navController.navigateToCreateNote()
            },
            onNavigateToEditNote = { noteId ->
                navController.navigateToEditNote(noteId)
            },
            onNavigateToCategories = {
                navController.navigateToCategories()
            },
            onNavigateToSettings = {
                navController.navigate(AppDestination.Profile.route)
            },
            onNavigateToTags = {
                navController.navigateToTags()
            },
            onOpenNotesForCategory = { categoryId ->
                navController.navigateToCategoryNotes(categoryId)
            },
            onOpenNotesForTag = { tagId ->
                navController.navigateToTagNotes(tagId)
            },
            onNavigateToDeletedArchived = { tabId ->
                navController.navigateToDeletedArchived(tabId)
            }
        )
    }
}