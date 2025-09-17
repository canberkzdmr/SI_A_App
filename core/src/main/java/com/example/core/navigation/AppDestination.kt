package com.example.core.navigation

sealed class AppDestination(val route: String) {
    object Login : AppDestination("login?username={username}") {
        fun createRoute(username: String? = null): String {
            return if (username!= null){
                "login?username=$username"
            } else {
                "login"
            }
        }
    }
    object Register : AppDestination("register")
    object Profile : AppDestination("profile")
    object EditProfile : AppDestination("edit_profile")

    object Splash: AppDestination("splash")
}