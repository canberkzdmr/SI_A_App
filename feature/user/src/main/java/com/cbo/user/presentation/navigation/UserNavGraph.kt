package com.cbo.user.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cbo.user.presentation.screen.EditProfileScreen
import com.cbo.user.presentation.screen.ProfileScreen
import com.cbo.user.presentation.screen.ChangePasswordScreen
import com.cbo.core.navigation.AppDestination
import com.cbo.statistics.presentation.screen.StatisticsScreen
import com.cbo.user.presentation.screen.ChangeLanguageScreen

fun NavGraphBuilder.userNavGraph(
    onLogOut: () -> Unit,
    onEditProfile: () -> Unit,
    onProfileUpdated: () -> Unit,
    onEditProfileCancelled: () -> Unit,
    onChangePassword: () -> Unit,
    onChangeLanguage: () -> Unit,
    onNavigateBack: () -> Unit,
    onPasswordChanged: () -> Unit,
    onChangePasswordCancelled: () -> Unit,
    onDeleteUserClicked: () -> Unit,
    onNotesClicked: () -> Unit = {},
    onCategoriesClicked: () -> Unit = {},
    onTagsClicked: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {},
) {
    composable(AppDestination.Profile.route) {
        ProfileScreen(
            onLogOut = { onLogOut() },
            onEditProfile = { onEditProfile() },
            onChangePassword = { onChangePassword() },
            onChangeLanguage = { onChangeLanguage() },
            onDeleteAccount = { onDeleteUserClicked() },
            onNotesClicked = { onNotesClicked() },
            onCategoriesClicked = { onCategoriesClicked() },
            onTagsClicked = { onTagsClicked() },
            onStatisticsClicked = { onNavigateToStatistics() },
        )
    }

    composable(AppDestination.Statistics.route) {
        StatisticsScreen(onNavigateBack = { onNavigateBack() })
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

    composable(AppDestination.ChangeLanguage.route) {
        ChangeLanguageScreen(
            onNavigateBack = { onNavigateBack() }
        )
    }
}