package com.cbo.splash

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cbo.core.navigation.AppDestination

fun NavGraphBuilder.splashNavGraph (
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit,
) {
    composable(AppDestination.Splash.route) {
        SplashScreen(
            onNavigateToMain = { onNavigateToMain() },
            onNavigateToLogin = { onNavigateToLogin() },
        )
    }
}