package com.cbo.memcloud

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.cbo.user.presentation.navigation.userNavGraph
import com.cbo.core.navigation.AppDestination
import com.cbo.login.presentation.navigation.loginNavGraph
import com.cbo.notes.presentation.navigation.navigateToCreateNote
import com.cbo.notes.presentation.navigation.navigateToEditNote
import com.cbo.notes.presentation.navigation.navigateToNotes
import com.cbo.notes.presentation.navigation.navigateToCategories
import com.cbo.notes.presentation.navigation.notesGraph
import com.cbo.splash.splashNavGraph
import com.cbo.ui.snackbar.SnackbarManager
import com.cbo.ui.snackbar.SnackbarMessage
import kotlinx.coroutines.coroutineScope

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
                navController.navigate(AppDestination.Profile.route) {
                    popUpTo(AppDestination.Login.route) {
                        inclusive = true
                    }
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
                Log.d("MainNavHost", "(userNavGraph) Navigated to Profile\n\tProfile updated")
                navController.popBackStack()
            },
            onEditProfileCancelled = {
                Log.d("MainNavHost", "(userNavGraph) Navigated to Profile\n\tCancelled")
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
                Log.d("MainNavHost", "(userNavGraph) Navigated to Notes")
                navController.navigateToNotes()
            }
        )

        splashNavGraph(
            onNavigateToLogin = {
                Log.d("MainNavHost", "(SplashNavGraph) Navigated to login")
                navController.navigate(AppDestination.Login.createRoute()) {
                    popUpTo(AppDestination.Login.route) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            },
            onNavigateToMain = {
                navController.navigate(AppDestination.Profile.route) {
                    popUpTo(AppDestination.Login.route) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        )
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
            }
        )
    }
}