package com.cbo.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * Bottom navigation destinations for the main app screens
 */
sealed class BottomNavDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Notes : BottomNavDestination(
        route = "main_notes",
        title = "Notes",
        icon = Icons.AutoMirrored.Filled.Note
    )
    
    object Categories : BottomNavDestination(
        route = "main_categories", 
        title = "Categories",
        icon = Icons.Default.Category
    )
    
    object Profile : BottomNavDestination(
        route = "main_profile",
        title = "Profile", 
        icon = Icons.Default.Person
    )

    object Tags : BottomNavDestination(
        route = "main_tags",
        title = "Tags",
        icon = Icons.Default.Tag
    )
}

/**
 * Main app bottom navigation bar component
 */
@Composable
fun AppBottomNavigation(
    navController: NavController,
    destinations: List<BottomNavDestination> = listOf(
        BottomNavDestination.Notes,
        BottomNavDestination.Categories,
        BottomNavDestination.Tags,
        BottomNavDestination.Profile,
    )
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination

    NavigationBar {
        destinations.forEach { destination ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.title
                    )
                },
                label = {
                    Text(destination.title)
                },
                selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}