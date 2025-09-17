package com.cbo.user.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.cbo.user.presentation.screen.EditUserProfileScreen
import com.cbo.user.presentation.screen.ProfileScreen
import com.example.core.navigation.AppDestination

fun NavGraphBuilder.userNavGraph(
    onLogOut: () -> Unit,
    onEditProfile: () -> Unit,
    onProfileUpdated: () -> Unit,
) {
    composable(AppDestination.Profile.route) {
        ProfileScreen(
            onLogOut = { onLogOut() },
            onEditProfile = { onEditProfile() }
        )
    }

    composable(AppDestination.EditProfile.route) {
        EditUserProfileScreen(
            onSaveSuccess = {
                onProfileUpdated()
            }
        )
    }
}