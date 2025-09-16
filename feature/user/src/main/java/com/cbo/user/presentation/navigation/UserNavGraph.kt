package com.cbo.user.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cbo.user.presentation.screen.ProfileScreen
import com.cbo.user.presentation.screen.ProfileScreenContent
import com.example.core.navigation.AppDestination

fun NavGraphBuilder.userNavGraph(
    onLogOut: () -> Unit,
) {
    composable(AppDestination.Profile.route) {
        ProfileScreen(
            onLogOut = { onLogOut() }
        )
    }
}