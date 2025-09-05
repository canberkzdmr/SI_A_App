package com.cbo.user.ui.navgraph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cbo.user.ui.ProfileScreen
import com.example.core.navigation.AppDestination

fun NavGraphBuilder.userNavGraph(
) {
    composable(AppDestination.Profile.route) {
        ProfileScreen()
    }
}