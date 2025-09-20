package com.cbo.user.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cbo.user.presentation.screen.EditProfileScreen
import com.cbo.user.presentation.screen.ProfileScreen
import com.cbo.user.presentation.screen.ChangePasswordScreen
import com.cbo.core.navigation.AppDestination

fun NavGraphBuilder.userNavGraph(
    onLogOut: () -> Unit,
    onEditProfile: () -> Unit,
    onProfileUpdated: () -> Unit,
    onEditProfileCancelled: () -> Unit,
    onChangePassword: () -> Unit,
    onPasswordChanged: () -> Unit,
    onChangePasswordCancelled: () -> Unit,
) {
    composable(AppDestination.Profile.route) {
        ProfileScreen(
            onLogOut = { onLogOut() },
            onEditProfile = { onEditProfile() },
            onChangePassword = { onChangePassword() }
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

    composable(AppDestination.ChangePassword.route) {
        ChangePasswordScreen(
            onNavigateBack = {
                onChangePasswordCancelled()
            },
            onPasswordChanged = {
                onPasswordChanged()
            }
        )
    }
}