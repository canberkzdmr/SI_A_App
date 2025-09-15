package com.example.splash

import android.util.Log
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.core.navigation.AppDestination

fun NavGraphBuilder.splashNavGraph (
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit,
) {
    composable(AppDestination.Splash.route) {
        SplashScreen(
            onNavigate = { destination ->
                Log.d("SplashNavGraph", "Destination: $destination")
                when(destination) {
                    SplashDestination.Login -> onNavigateToLogin()
                    SplashDestination.Main -> onNavigateToMain()
                }
            }
        )
    }
}