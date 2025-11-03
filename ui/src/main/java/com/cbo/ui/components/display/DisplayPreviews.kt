package com.cbo.ui.components.display

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tag
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cbo.ui.theme.MemCloudApplicationTheme

@Preview(showBackground = true, name = "Stat Cards")
@Composable
fun StatCardsPreview() {
    MemCloudApplicationTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppStatCard(
                title = "Total Notes",
                value = "1,234",
                subtitle = "+12% from last month",
                icon = Icons.AutoMirrored.Filled.Note
            )
            
            AppStatCard(
                title = "Categories",
                value = "8",
                subtitle = "Active categories",
                icon = Icons.Default.Folder
            )
        }
    }
}

@Preview(showBackground = true, name = "Stat Grid")
@Composable
fun StatGridPreview() {
    MemCloudApplicationTheme {
        val stats = listOf(
            StatData(
                title = "Notes",
                value = "1,234",
                subtitle = "+12%",
                icon = Icons.AutoMirrored.Filled.Note
            ),
            StatData(
                title = "Categories",
                value = "8",
                subtitle = "Active",
                icon = Icons.Default.Folder
            ),
            StatData(
                title = "Tags",
                value = "24",
                subtitle = "Used",
                icon = Icons.Default.Tag
            ),
            StatData(
                title = "Storage",
                value = "2.4 GB",
                subtitle = "Used",
                icon = Icons.Default.Storage
            )
        )
        
        AppStatGrid(
            stats = stats,
            columns = 2
        )
    }
}

@Preview(showBackground = true, name = "Progress Cards")
@Composable
fun ProgressCardsPreview() {
    MemCloudApplicationTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppProgressCard(
                title = "Storage Usage",
                progress = 0.65f,
                progressText = "65%",
                subtitle = "2.4 GB of 3.7 GB used"
            )
            
            AppCircularProgressCard(
                title = "Sync Progress",
                progress = 0.8f,
                progressText = "80%",
                subtitle = "Syncing notes to cloud"
            )
        }
    }
}

@Preview(showBackground = true, name = "Step Progress")
@Composable
fun StepProgressPreview() {
    MemCloudApplicationTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppStepProgress(
                steps = listOf("Setup", "Profile", "Preferences", "Complete"),
                currentStep = 2
            )
        }
    }
}

@Preview(showBackground = true, name = "Timeline")
@Composable
fun TimelinePreview() {
    MemCloudApplicationTheme {
        val timelineItems = listOf(
            TimelineItem(
                title = "Account Created",
                description = "Your account was successfully created",
                timestamp = "2 hours ago",
                icon = Icons.Default.Person,
                isCompleted = true
            ),
            TimelineItem(
                title = "First Note Created",
                description = "You created your first note",
                timestamp = "1 hour ago",
                icon = Icons.AutoMirrored.Filled.Note,
                isCompleted = true
            ),
            TimelineItem(
                title = "Profile Updated",
                description = "Your profile information was updated",
                timestamp = "30 minutes ago",
                icon = Icons.Default.Edit,
                isCompleted = false
            )
        )
        
        AppTimeline(
            items = timelineItems
        )
    }
}

@Preview(showBackground = true, name = "Activity Feed")
@Composable
fun ActivityFeedPreview() {
    MemCloudApplicationTheme {
        val activities = listOf(
            ActivityItem(
                title = "Note Created",
                description = "New note 'Meeting Notes' was created",
                timestamp = "2 hours ago",
                icon = Icons.AutoMirrored.Filled.Note,
                type = ActivityType.SUCCESS
            ),
            ActivityItem(
                title = "Category Added",
                description = "New category 'Work' was added",
                timestamp = "1 hour ago",
                icon = Icons.Default.Folder,
                type = ActivityType.INFO
            ),
            ActivityItem(
                title = "Sync Failed",
                description = "Failed to sync notes to cloud",
                timestamp = "30 minutes ago",
                icon = Icons.Default.Error,
                type = ActivityType.ERROR
            )
        )
        
        AppActivityFeed(
            activities = activities
        )
    }
}

@Preview(showBackground = true, name = "Metric Cards")
@Composable
fun MetricCardsPreview() {
    MemCloudApplicationTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppMetricCard(
                label = "Notes Created",
                value = "1,234",
                unit = "notes",
                change = "+12%",
                isPositiveChange = true
            )
            
            AppMetricCard(
                label = "Storage Used",
                value = "2.4",
                unit = "GB",
                change = "+5%",
                isPositiveChange = false
            )
        }
    }
}



