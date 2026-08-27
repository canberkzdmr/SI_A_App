package com.cbo.memcloud

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.cbo.core.navigation.AppDestination
import com.cbo.notes.presentation.navigation.NOTES_ROUTE
import com.cbo.notes.presentation.navigation.CATEGORIES_ROUTE
import com.cbo.notes.presentation.navigation.TAGS_ROUTE
import com.cbo.notes.presentation.navigation.CALENDAR_ROUTE
import com.cbo.notes.presentation.navigation.MAP_ROUTE
import com.cbo.notes.presentation.navigation.CREATE_NOTE_ROUTE
import com.cbo.ui.components.AppBottomNavigation
import com.cbo.ui.components.AppScaffoldWithInsets
import com.cbo.ui.components.BottomNavigationOverlay
import com.cbo.ui.components.CenterButton
import com.cbo.ui.components.EdgeToEdgeWrapper
import com.cbo.ui.snackbar.SnackbarHostProvider

@Composable
fun MemcloudApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    var isBottomNavExpanded by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    // Extract base route if it has arguments (e.g. "notes?categoryId={categoryId}" -> "notes")
    val baseRoute = currentRoute?.substringBefore("?")?.substringBefore("/")

    val showBottomBar = baseRoute in listOf(
        NOTES_ROUTE,
        CALENDAR_ROUTE,
        MAP_ROUTE,
        AppDestination.Profile.route
    )

    SnackbarHostProvider { padding ->
        EdgeToEdgeWrapper(modifier = modifier) {
            AppScaffoldWithInsets(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    if (showBottomBar) {
                        AppBottomNavigation(
                            navController = navController,
                            isExpanded = isBottomNavExpanded,
                            onExpandedChange = { isBottomNavExpanded = it },
                            showCenterButton = true
                        )
                    }
                }
            ) { scaffoldPadding ->
                MainNavHost(
                    navController = navController,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(if (showBottomBar) scaffoldPadding else padding)
                )

                if (showBottomBar) {
                    BottomNavigationOverlay(
                        isExpanded = isBottomNavExpanded,
                        onDismiss = { isBottomNavExpanded = false },
                        onOptionClick = { option ->
                            when (option) {
                                "create_note" -> navController.navigate(CREATE_NOTE_ROUTE)
                                "categories" -> {
                                    navController.navigate(CATEGORIES_ROUTE) {
                                        launchSingleTop = true
                                    }
                                }
                                "tags" -> {
                                    navController.navigate(TAGS_ROUTE) {
                                        launchSingleTop = true
                                    }
                                }
                            }
                            isBottomNavExpanded = false
                        }
                    )
                }
            }
        }
    }
}
