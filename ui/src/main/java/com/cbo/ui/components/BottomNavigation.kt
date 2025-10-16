package com.cbo.ui.components

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateDpAsState
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlin.math.cos
import kotlin.math.sin

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
 * Data class for navigation tabs
 */
data class TabItem(
    val id: String,
    val icon: ImageVector,
    val label: String
)

/**
 * Data class for center action options
 */
data class CenterOption(
    val id: String,
    val icon: ImageVector,
    val label: String,
    val color: Color
)

/**
 * Main app bottom navigation bar component with animated center button
 */
@Composable
fun AppBottomNavigation(
    navController: NavController,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = {}
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination

    val currentRoute = currentDestination?.route ?: "main_notes"

    BottomNavigationBar(
        activeRoute = currentRoute,
        isExpanded = isExpanded,
        onTabClick = { route ->
            if (route == "center") {
                onExpandedChange(!isExpanded)
            } else {
                navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                onExpandedChange(false)
            }
        },
        modifier = modifier
    )
}

/**
 * Overlay component for the expanded bottom navigation
 * Should be rendered by the parent composable that has access to the full screen
 */
@Composable
fun BottomNavigationOverlay(
    isExpanded: Boolean,
    onDismiss: () -> Unit,
    onOptionClick: (String) -> Unit
) {
    AnimatedVisibility(
        visible = isExpanded,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable { onDismiss() }
        )
    }
    
    AnimatedVisibility(
        visible = isExpanded,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            CenterActionOptions(
                onOptionClick = onOptionClick
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    activeRoute: String,
    isExpanded: Boolean,
    onTabClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val mainTabs = listOf(
        TabItem("main_notes", Icons.AutoMirrored.Filled.Note, "Notes"),
        TabItem("main_categories", Icons.Default.Category, "Categories"),
        TabItem("center", Icons.Default.Add, "Create"),
        TabItem("main_tags", Icons.Default.Tag, "Tags"),
        TabItem("main_profile", Icons.Default.Person, "Profile"),
    )
    
    // Find the index of the active tab (excluding center button)
    val activeTabIndex = mainTabs.indexOfFirst { it.id == activeRoute && it.id != "center" }
    Log.d("BottomNavigation", "activeTabIndex: $activeTabIndex")
    // Adjust index for center button position (center is at index 1)
    val adjustedIndex = when {
        activeTabIndex < 0 -> -1
        activeTabIndex == 0 -> 0 // Notes
        activeTabIndex == 1 -> 1
        activeTabIndex > 2 -> activeTabIndex - 1 // Categories, Tags, Profile (shift left by 1)
        else -> activeTabIndex
    }

    Log.d("BottomNavigation","adjustedIndex: $adjustedIndex")
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 12.dp, start = 8.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                mainTabs.forEachIndexed { index, tab ->
                    if (tab.id == "center") {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CenterButton(
                                isExpanded = isExpanded,
                                onClick = { onTabClick(tab.id) }
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            NavigationTab(
                                tab = tab,
                                isActive = activeRoute == tab.id,
                                onClick = { onTabClick(tab.id) },
                                showIndicator = false // Don't show individual indicators
                            )
                        }
                    }
                }
            }
            
            // Sliding indicator - show for all non-center tabs
            if (activeTabIndex >= 0) {
                SlidingIndicator(
                    tabCount = 4, // 4 regular tabs (excluding center button)
                    activeTabIndex = adjustedIndex,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
fun NavigationTab(
    tab: TabItem,
    isActive: Boolean,
    onClick: () -> Unit,
    showIndicator: Boolean = true
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isActive) 1.1f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "scale"
    )
    
    val indicatorScale by animateFloatAsState(
        targetValue = if (isActive && showIndicator) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "indicator_scale"
    )
    
    val indicatorAlpha by animateFloatAsState(
        targetValue = if (isActive && showIndicator) 1f else 0f,
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutSlowInEasing
        ),
        label = "indicator_alpha"
    )
    
    val iconColor by animateColorAsState(
        targetValue = if (isActive) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        },
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "icon_color"
    )
    
    val textColor by animateColorAsState(
        targetValue = if (isActive) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        },
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "text_color"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = false, radius = 28.dp)
            ) { onClick() }
            .padding(vertical = 4.dp)
            .scale(animatedScale)
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            tint = iconColor,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Active indicator with animation - always take up space for consistent alignment
        Spacer(modifier = Modifier.height(2.dp))
        if (showIndicator) {
            Box(
                modifier = Modifier
                    .size(2.dp)
                    .scale(indicatorScale)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = indicatorAlpha),
                        CircleShape
                    )
            )
        } else {
            Log.d("BottomNavigation", "NavigationTab/showIndicator is false")
            // Invisible spacer to maintain consistent height
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun SlidingIndicator(
    tabCount: Int,
    activeTabIndex: Int,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    // With weight-based layout, each item (including center) gets equal space
    // Total items: 5 (Notes, Categories, Center, Tags, Profile)
    val totalItems = 5
    val itemWidth = (screenWidth - 16.dp) / totalItems // Subtract total horizontal padding (8dp each side)
    
    // Calculate the position based on active tab index
    // Layout: [Tab0=Notes] [Tab1=Categories] [Center] [Tab2=Tags] [Tab3=Profile]
    // Adjusted indices map to positions: 0->0, 1->1, 2->3, 3->4
    val positionIndex = when (activeTabIndex) {
        0 -> 0 // Notes (position 0)
        1 -> 1 // Categories (position 1, before center)
        2 -> 3 // Tags (position 3)
        3 -> 4 // Profile (position 4)
        else -> 0
    }

    Log.d("BottomNavigation", "SlidingIndicatior/positionIndex: $positionIndex")
    
    val targetOffset = 8.dp + (itemWidth * positionIndex) + (itemWidth / 2)
    
    val animatedOffset by animateDpAsState(
        targetValue = targetOffset,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "indicator_offset"
    )
    
    val indicatorWidth by animateDpAsState(
        targetValue = 20.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "indicator_width"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(indicatorWidth)
                .height(4.dp)
                .offset(x = animatedOffset - (indicatorWidth / 2))
                .background(
                    MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
        )
    }
}

@Composable
fun CenterButton(
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val animatedRotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "rotation"
    )

    val animatedScale by animateFloatAsState(
        targetValue = if (isExpanded) 1.1f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.offset(y = (-12).dp)
    ) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(56.dp)
                .scale(animatedScale)
                .rotate(animatedRotation)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(28.dp)
            )
        }

        if (!isExpanded) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Create",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            // Invisible spacer to match the indicator space in other tabs
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
fun CenterActionOptions(
    onOptionClick: (String) -> Unit
) {
    val centerOptions = listOf(
        CenterOption("categories", Icons.Default.Category, "Categories", Color(0xFFFF6B6B)),
        CenterOption("tags", Icons.Default.Tag, "Tags", Color(0xFF4ECDC4)),
        CenterOption("search", Icons.Default.Search, "Search", Color(0xFF45B7D1)),
        CenterOption("settings", Icons.Default.Settings, "Settings", Color(0xFF96CEB4))
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        centerOptions.forEachIndexed { index, option ->
            // Spread buttons in a semi-circle above the center button
            // Starting from left (-135°) to right (-45°) in the upper half
            val startAngle = 225.0 // Start from bottom-left
            val angleSpan = 90.0 // Span 90 degrees (semi-circle in upper half)
            val angle = startAngle + (index * angleSpan / (centerOptions.size - 1))
            
            val radius = 135.dp
            val angleRad = Math.toRadians(angle)
            val x = (cos(angleRad) * radius.value).dp
            val y = (sin(angleRad) * radius.value).dp

            AnimatedVisibility(
                visible = true,
                enter = scaleIn(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessHigh
                    ),
                    initialScale = 0.3f
                ) + fadeIn(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessHigh
                    )
                ),
                modifier = Modifier
                    .offset(x = x, y = y - 10.dp)
            ) {
                FloatingActionButton(
                    onClick = { onOptionClick(option.id) },
                    containerColor = option.color,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = option.label,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview(name = "Bottom Navigation Bar - Collapsed", showBackground = true)
@Composable
private fun BottomNavigationBarPreview() {
    MaterialTheme {
        BottomNavigationBar(
            activeRoute = "main_notes",
            isExpanded = false,
            onTabClick = {}
        )
    }
}

@Preview(name = "Bottom Navigation Bar - Expanded", showBackground = true)
@Composable
private fun BottomNavigationBarExpandedPreview() {
    MaterialTheme {
        BottomNavigationBar(
            activeRoute = "main_notes",
            isExpanded = true,
            onTabClick = {}
        )
    }
}

@Preview(name = "Navigation Tab - Active", showBackground = true)
@Composable
private fun NavigationTabActivePreview() {
    MaterialTheme {
        Surface {
            NavigationTab(
                tab = TabItem("main_notes", Icons.AutoMirrored.Filled.Note, "Notes"),
                isActive = true,
                onClick = {}
            )
        }
    }
}

@Preview(name = "Navigation Tab - Inactive", showBackground = true)
@Composable
private fun NavigationTabInactivePreview() {
    MaterialTheme {
        Surface {
            NavigationTab(
                tab = TabItem("main_profile", Icons.Default.Person, "Profile"),
                isActive = false,
                onClick = {}
            )
        }
    }
}

@Preview(name = "Center Button - Collapsed", showBackground = true)
@Composable
private fun CenterButtonPreview() {
    MaterialTheme {
        Surface {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 40.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                CenterButton(
                    isExpanded = false,
                    onClick = {}
                )
            }
        }
    }
}

@Preview(name = "Center Button - Expanded", showBackground = true)
@Composable
private fun CenterButtonExpandedPreview() {
    MaterialTheme {
        Surface {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 40.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                CenterButton(
                    isExpanded = true,
                    onClick = {}
                )
            }
        }
    }
}

@Preview(name = "Center Action Options", showBackground = true, heightDp = 400)
@Composable
private fun CenterActionOptionsPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(Color.LightGray)
        ) {
            CenterActionOptions(
                onOptionClick = {}
            )
        }
    }
}

@Preview(name = "Bottom Navigation Overlay", showBackground = true, heightDp = 600)
@Composable
private fun BottomNavigationOverlayPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            // Mock content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text("Main Content", style = MaterialTheme.typography.headlineMedium)
            }
            
            // Overlay
            BottomNavigationOverlay(
                isExpanded = true,
                onDismiss = {},
                onOptionClick = {}
            )
        }
    }
}

@Preview(name = "Dark Mode - Navigation Bar", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BottomNavigationBarDarkPreview() {
    MaterialTheme {
        BottomNavigationBar(
            activeRoute = "main_categories",
            isExpanded = false,
            onTabClick = {}
        )
    }
}

@Preview(name = "Sliding Indicator - Position 0", showBackground = true)
@Composable
private fun SlidingIndicatorPosition0Preview() {
    MaterialTheme {
        Surface {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(16.dp)
            ) {
                SlidingIndicator(
                    tabCount = 4,
                    activeTabIndex = 0,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Preview(name = "Sliding Indicator - Position 2", showBackground = true)
@Composable
private fun SlidingIndicatorPosition2Preview() {
    MaterialTheme {
        Surface {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(16.dp)
            ) {
                SlidingIndicator(
                    tabCount = 4,
                    activeTabIndex = 2,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}