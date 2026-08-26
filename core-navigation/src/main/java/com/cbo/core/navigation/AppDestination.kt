package com.cbo.core.navigation

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
    object Tags : AppDestination("tags")
    object EditProfile : AppDestination("edit_profile")
    object ChangePassword : AppDestination("change_password")
    object ChangeLanguage : AppDestination("change_language")
    object Splash: AppDestination("splash")
    object Main: AppDestination("main")
    object Statistics: AppDestination("statistics")
}
