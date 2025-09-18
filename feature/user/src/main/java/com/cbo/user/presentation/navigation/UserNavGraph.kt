package com.cbo.user.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cbo.user.presentation.screen.EditProfileScreen
import com.cbo.user.presentation.screen.ProfileScreen
import com.example.core.navigation.AppDestination

fun NavGraphBuilder.userNavGraph(
    onLogOut: () -> Unit,
    onEditProfile: () -> Unit,
    onProfileUpdated: () -> Unit,
    onEditProfileCancelled: () -> Unit,
) {
    composable(AppDestination.Profile.route) {
        ProfileScreen(
            onLogOut = { onLogOut() },
            onEditProfile = { onEditProfile() }
        )
    }

    composable(AppDestination.EditProfile.route) {
        EditProfileScreen(
            onCancelEditProfile = {
                onEditProfileCancelled()
            },
            onSaveSuccess = {
                onProfileUpdated()
            }
        )
    }
}