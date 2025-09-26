package com.cbo.memcloud

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cbo.core.navigation.AppDestination
import com.cbo.login.presentation.navigation.loginNavGraph
import com.cbo.memcloud.presentation.screen.MainScreen
import com.cbo.notes.presentation.navigation.navigateToCategories
import com.cbo.notes.presentation.navigation.navigateToCreateNote
import com.cbo.notes.presentation.navigation.navigateToEditNote
import com.cbo.notes.presentation.navigation.navigateToTags
import com.cbo.notes.presentation.navigation.notesGraph
import com.cbo.splash.splashNavGraph
import com.cbo.user.presentation.navigation.userNavGraph

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
                navController.navigate(AppDestination.Main.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            },

            onRegisterClick = {
                navController.navigate(AppDestination.Register.route) {

                }
            },

            onRegisterSuccess = { username ->
                navController.navigate(AppDestination.Login.createRoute(username)) {
                    popUpTo(AppDestination.Login.route) {
                        inclusive = true
                    }
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
            onDeleteUserClicked = {
                Log.d("MainNavHost", "(userNavGraph) Delete Account clicked")
            },
            onNotesClicked = {
                Log.d("MainNavHost", "(userNavGraph) Navigated to Main")
                navController.navigate(AppDestination.Main.route)
            },
            onCategoriesClicked = {
                Log.d("MainNavHost", "(userNavGraph) Navigated to Categories")
                navController.navigateToCategories()
            },
            onTagsClicked = {
                Log.d("MainNavHost", "(userNavGraph) Navigated to Tags")
                navController.navigateToTags()
            }
        )

        splashNavGraph(
            onNavigateToLogin = {
                navController.navigate(AppDestination.Login.createRoute()) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onNavigateToMain = {
                navController.navigate(AppDestination.Main.route) {
                    popUpTo(AppDestination.Login.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )

        // Main screen with bottom navigation
        composable(AppDestination.Main.route) {
            Log.d("MainNavHost", "Main Screen navigation called")
            MainScreen(
                onLogOut = {
                    navController.navigate(AppDestination.Login.createRoute()) {
                        // clear main graph and go back to login
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onEditProfile = {
                    Log.d("MainNavHost", "(MainScreen) Navigated to Edit Profile")
                    navController.navigate(AppDestination.EditProfile.route)
                },
                onChangePassword = {
                    Log.d("MainNavHost", "(MainScreen) Navigated to Change Password")
                    navController.navigate(AppDestination.ChangePassword.route)
                },
                onDeleteAccount = {
                    Log.d("MainNavHost", "(MainScreen) Delete Account clicked")
                },
                onNavigateToCreateNote = {
                    navController.navigateToCreateNote()
                },
                onNavigateToEditNote = { noteId ->
                    navController.navigateToEditNote(noteId)
                }
            )
        }

        notesGraph(
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
            }
        )
    }
}