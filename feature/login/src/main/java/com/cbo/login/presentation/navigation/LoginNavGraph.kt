package com.cbo.login.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.cbo.core.navigation.AppDestination
import com.cbo.login.presentation.screen.LoginScreen
import com.cbo.login.presentation.screen.LoginScreenContent
import com.cbo.login.presentation.screen.RegisterScreen

fun NavGraphBuilder.loginNavGraph(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onRegisterSuccess: (username: String) -> Unit,
) {
    composable(
        route = "login?username={username}",
        arguments = listOf(
            navArgument("username") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val username = backStackEntry.arguments?.getString("username")
        LoginScreen(
            prefillUsername = username,
            onLoginSuccess = {
                onLoginSuccess()
            },
            onRegisterClick = {
                onRegisterClick()
            }
        )
    }

    composable(AppDestination.Register.route) {
        RegisterScreen(
            onRegisterSuccess = { username ->
                onRegisterSuccess(username)
            },
        )
    }
}