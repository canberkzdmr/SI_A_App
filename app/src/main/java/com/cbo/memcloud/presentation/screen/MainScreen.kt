package com.cbo.memcloud.presentation.screen

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cbo.notes.presentation.navigation.navigateToCategories
import com.cbo.notes.presentation.navigation.navigateToTags
import com.cbo.notes.presentation.screen.NotesScreen
import com.cbo.notes.presentation.screen.CategoriesScreen
import com.cbo.notes.presentation.screen.TagsScreen
import com.cbo.user.presentation.screen.ProfileScreen
import com.cbo.ui.components.AppBottomNavigation
import com.cbo.ui.components.BottomNavDestination

/**
 * Main screen that hosts the bottom navigation and main app content
 */
@Composable
fun MainScreen(
    onLogOut: () -> Unit,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
    onDeleteAccount: () -> Unit,
    onNavigateToCreateNote: () -> Unit,
    onNavigateToEditNote: (noteId: Int) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            AppBottomNavigation(navController = navController)
        }
    ) { paddingValues ->
        Log.d("MainScreen", "current destination (${navController.currentDestination?.route})")
        NavHost(
            navController = navController,
            startDestination = BottomNavDestination.Notes.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            composable(BottomNavDestination.Notes.route) {
                NotesScreen(
                    onNavigateToCreateNote = onNavigateToCreateNote,
                    onNavigateToEditNote = onNavigateToEditNote,
                    onNavigateToCategories = {
                        navController.navigate(BottomNavDestination.Categories.route)
                    },
                    onNavigateToSettings = {
                        navController.navigate(BottomNavDestination.Profile.route)
                    }
                )
            }

            composable(BottomNavDestination.Categories.route) {
                CategoriesScreen(
                    onNavigateBack = {
                        navController.navigate(BottomNavDestination.Notes.route)
                    }
                )
            }

            composable(BottomNavDestination.Profile.route) {
                ProfileScreen(
                    onLogOut = {
                        Log.d("MainScreen", "PS onLogOut")
                        onLogOut()
                    },
                    onEditProfile = {
                        Log.d("MainScreen", "PS onEditProfile")
                        onEditProfile()
                    },
                    onChangePassword = {
                        Log.d("MainScreen", "PS onChangePassword")
                        onChangePassword()
                    },
                    onDeleteAccount = {
                        Log.d("MainScreen", "PS onDeleteAccount")
                        onDeleteAccount()
                    },
                    onNotesClicked = {
                        Log.d("MainScreen", "PS onNotesClicked")
                        navController.navigate(BottomNavDestination.Notes.route)
                    },
                    onCategoriesClicked = {
                        Log.d("MainScreen", "PS onCategoriesClicked")
                        navController.navigate(BottomNavDestination.Categories.route)
                    },
                    onTagsClicked = {
                        Log.d("MainScreen", "PS onTagsClicked")
                        navController.navigate(BottomNavDestination.Tags.route)
                    }
                )
            }

            composable(BottomNavDestination.Tags.route) {
                TagsScreen(
                    onNavigateBack = { Log.d("MainScreen", "TS onNavigateBack")}
                )
            }
        }
    }
}
