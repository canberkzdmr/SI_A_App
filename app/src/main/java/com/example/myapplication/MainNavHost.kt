package com.example.myapplication

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.cbo.user.ui.navgraph.userNavGraph
import com.example.core.navigation.AppDestination
import com.example.login.presentation.navigation.loginNavGraph

@Composable
fun MainNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.Login.route,
        modifier = modifier
    ) {
        loginNavGraph(
            onLoginSuccess = {
                navController.navigate(AppDestination.Profile.route) {
                    popUpTo(AppDestination.Login.route) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            },

            onRegisterClick = {
                navController.navigate(AppDestination.Register.route) {

                }
            },

            onRegisterSuccess = { username ->
                navController.navigate(AppDestination.Login.createRoute(username)) {
                    popUpTo(AppDestination.Login.route) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        )

        userNavGraph(

        )
    }
}