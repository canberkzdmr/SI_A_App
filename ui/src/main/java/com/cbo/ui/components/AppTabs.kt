package com.cbo.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cbo.ui.theme.MemCloudApplicationTheme

/**
 * A tab item data class for use with AppTabRow
 *
 * @param title The text to display on the tab
 * @param icon Optional composable for an icon to display on the tab
 */
data class AppTabItem(
    val title: String,
    val icon: (@Composable () -> Unit)? = null
)

/**
 * A Material 3 styled tab row component
 *
 * @param tabs List of TabItem objects to display
 * @param selectedTabIndex The currently selected tab index
 * @param onTabSelected Callback when a tab is selected, providing the new index
 * @param modifier Modifier for the tab row
 * @param containerColor Background color for the tab row
 * @param contentColor Color for the tab content
 * @param indicatorColor Color for the selected tab indicator
 */
@Composable
fun AppTabRow(
    tabs: List<AppTabItem>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    indicatorColor: Color = MaterialTheme.colorScheme.primary,
) {
    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier.fillMaxWidth(),
        containerColor = containerColor,
        contentColor = contentColor,
        indicator = { tabPositions ->
            if (selectedTabIndex < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = indicatorColor
                )
            }
        }
    ) {
        tabs.forEachIndexed { index, tabItem ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = tabItem.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall
                    )
                },
                icon = tabItem.icon,
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * A simplified version of AppTabRow that only takes string titles
 *
 * @param titles List of tab titles
 * @param selectedTabIndex The currently selected tab index
 * @param onTabSelected Callback when a tab is selected, providing the new index
 * @param modifier Modifier for the tab row
 */
@Composable
fun AppTabRow(
    titles: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppTabRow(
        tabs = titles.map { AppTabItem(title = it) },
        selectedTabIndex = selectedTabIndex,
        onTabSelected = onTabSelected,
        modifier = modifier
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
fun AppTabRowPreview() {
    MemCloudApplicationTheme {
        var selectedIndex by remember { mutableIntStateOf(0) }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Simple string-based tabs
            AppTabRow(
                titles = listOf("Deleted", "Archived"),
                selectedTabIndex = selectedIndex,
                onTabSelected = { selectedIndex = it }
            )

            Text(
                text = "Selected tab: ${if (selectedIndex == 0) "Deleted" else "Archived"}",
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
fun AppTabRowMultiplePreview() {
    MemCloudApplicationTheme {
        var selectedIndex by remember { mutableIntStateOf(0) }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Multiple tabs example
            AppTabRow(
                titles = listOf("All", "Active", "Archived", "Deleted"),
                selectedTabIndex = selectedIndex,
                onTabSelected = { selectedIndex = it }
            )
        }
    }
}