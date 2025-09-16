package com.example.splash

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.core.navigation.AppDestination

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