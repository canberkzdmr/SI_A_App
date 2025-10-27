package com.cbo.memcloud.presentation.screen

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cbo.notes.presentation.screen.CategoriesScreen
import com.cbo.notes.presentation.screen.NotesScreen
import com.cbo.notes.presentation.screen.TagsScreen
import com.cbo.ui.components.AppBottomNavigation
import com.cbo.ui.components.BottomNavDestination
import com.cbo.ui.components.BottomNavigationOverlay
import com.cbo.ui.components.CenterButton
import com.cbo.user.presentation.screen.ProfileScreen

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
    var isBottomNavExpanded by remember { mutableStateOf(false) }
    var selectedCategoryIdForNotes by remember { mutableStateOf<Int?>(null) }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Navigation content - no Scaffold wrapper to avoid nesting
        Log.d("MainScreen", "current destination (${navController.currentDestination?.route})")
        NavHost(
            navController = navController,
            startDestination = BottomNavDestination.Notes.route,
            modifier = Modifier.fillMaxSize()
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
                    },
                    initialCategoryId = selectedCategoryIdForNotes
                )
            }

            composable(BottomNavDestination.Categories.route) {
                CategoriesScreen(
                    onNavigateBack = {
                        navController.navigate(BottomNavDestination.Notes.route)
                    },
                    onOpenNotesForCategory = { categoryId ->
                        selectedCategoryIdForNotes = categoryId
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
                    onNavigateBack = {
                        navController.navigate(BottomNavDestination.Notes.route)
                    }
                )
            }
        }
        
        // Bottom navigation bar as overlay
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            AppBottomNavigation(
                navController = navController,
                isExpanded = isBottomNavExpanded,
                onExpandedChange = { isBottomNavExpanded = it },
                showCenterButton = false
            )
        }
        
        // Center floating action button rendered as an overlay so it can overflow the bar
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(modifier = Modifier.navigationBarsPadding()) {
                CenterButton(
                    isExpanded = isBottomNavExpanded,
                    onClick = { isBottomNavExpanded = !isBottomNavExpanded }
                )
            }
        }

        // Overlay for expanded bottom navigation
        BottomNavigationOverlay(
            isExpanded = isBottomNavExpanded,
            onDismiss = { isBottomNavExpanded = false },
            onOptionClick = { option ->
                when (option) {
                    "create_note" -> {
                        onNavigateToCreateNote()
                    }
                    "categories" -> {
                        navController.navigate(BottomNavDestination.Categories.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    "tags" -> {
                        navController.navigate(BottomNavDestination.Tags.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    // Add more cases as needed
                }
                isBottomNavExpanded = false
            }
        )
    }
}
