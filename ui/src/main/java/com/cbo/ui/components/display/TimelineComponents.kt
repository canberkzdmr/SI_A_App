package com.cbo.ui.components.display

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.cbo.ui.components.AppBody
import com.cbo.ui.components.AppTitle

/**
 * Timeline item data class
 */
data class TimelineItem(
    val title: String,
    val description: String,
    val timestamp: String,
    val icon: ImageVector? = null,
    val isCompleted: Boolean = false
)

/**
 * Timeline component
 */
@Composable
fun AppTimeline(
    items: List<TimelineItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items.forEachIndexed { index, item ->
            AppTimelineItem(
                item = item,
                isLast = index == items.size - 1
            )
        }
    }
}

/**
 * Timeline item component
 */
@Composable
fun AppTimelineItem(
    item: TimelineItem,
    isLast: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Timeline indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.size(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (item.isCompleted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                shape = androidx.compose.foundation.shape.CircleShape
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.icon != null) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (item.isCompleted) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            }
                        )
                    } else if (item.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            
            if (!isLast) {
                Spacer(modifier = Modifier.height(8.dp))
                
                androidx.compose.material3.VerticalDivider(
                    modifier = Modifier
                        .width(2.dp)
                        .height(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Timeline content
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppTitle(text = item.title)
                    
                    AppBody(
                        text = item.timestamp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                AppBody(
                    text = item.description,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

/**
 * Activity feed component
 */
@Composable
fun AppActivityFeed(
    activities: List<ActivityItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        activities.forEach { activity ->
            AppActivityItem(activity = activity)
        }
    }
}

/**
 * Activity item data class
 */
data class ActivityItem(
    val title: String,
    val description: String,
    val timestamp: String,
    val icon: ImageVector? = null,
    val type: ActivityType = ActivityType.INFO
)

/**
 * Activity type enum
 */
enum class ActivityType {
    INFO, SUCCESS, WARNING, ERROR
}

/**
 * Activity item component
 */
@Composable
fun AppActivityItem(
    activity: ActivityItem,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Activity icon
        Card(
            modifier = Modifier.size(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (activity.type) {
                    ActivityType.SUCCESS -> MaterialTheme.colorScheme.primary
                    ActivityType.WARNING -> MaterialTheme.colorScheme.tertiary
                    ActivityType.ERROR -> MaterialTheme.colorScheme.error
                    ActivityType.INFO -> MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            shape = androidx.compose.foundation.shape.CircleShape
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (activity.icon != null) {
                    Icon(
                        imageVector = activity.icon,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = when (activity.type) {
                            ActivityType.SUCCESS -> MaterialTheme.colorScheme.onPrimary
                            ActivityType.WARNING -> MaterialTheme.colorScheme.onTertiary
                            ActivityType.ERROR -> MaterialTheme.colorScheme.onError
                            ActivityType.INFO -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Activity content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            AppBody(text = activity.title)
            
            AppBody(
                text = activity.description,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            AppBody(
                text = activity.timestamp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}



