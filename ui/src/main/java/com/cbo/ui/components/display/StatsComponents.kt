package com.cbo.ui.components.display

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cbo.ui.components.AppBody
import com.cbo.ui.components.AppCaption
import com.cbo.ui.components.AppLabelLarge
import com.cbo.ui.components.AppTitle
import com.cbo.ui.components.AppTitleMedium

/**
 * Stat card component
 */
@Composable
fun AppStatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val cardContent: @Composable ColumnScope.() -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                AppBody(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                AppTitle(
                    text = value
                )
                
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    AppBody(
                        text = subtitle,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    if (onClick != null) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            onClick = onClick,
            content = cardContent
        )
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            content = cardContent
        )
    }
}

/**
 * Stat grid component
 */
@Composable
fun AppStatGrid(
    stats: List<StatData>,
    columns: Int = 2,
    modifier: Modifier = Modifier,
    onStatClick: ((StatData) -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        stats.forEach { stat ->
            AppStatCard(
                title = stat.title,
                value = stat.value,
                subtitle = stat.subtitle,
                icon = stat.icon,
                onClick = { onStatClick?.invoke(stat) }
            )
        }
    }
}

/**
 * Stat data class
 */
data class StatData(
    val title: String,
    val value: String,
    val subtitle: String? = null,
    val icon: ImageVector? = null
)

/**
 * Metric card component
 */
@Composable
fun AppMetricCard(
    label: String,
    value: String,
    unit: String? = null,
    change: String? = null,
    isPositiveChange: Boolean = true,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppBody(
                text = label,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppTitle(
                    text = value
                )
                
                if (unit != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    AppBody(
                        text = unit,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            if (change != null) {
                Spacer(modifier = Modifier.height(4.dp))
                
                AppBody(
                    text = change,
                    color = if (isPositiveChange) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }
        }
    }
}

/**
 * KPI card component with colored tint, icon, bold value, and label
 */
@Composable
fun AppKpiCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        onClick = onClick ?: {},
        enabled = onClick != null,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            AppTitle(
                text = value,
                color = color,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            AppCaption(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Section card component providing a standardized card container with icon and title header
 */
@Composable
fun AppSectionCard(
    title: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                AppTitleMedium(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                action?.invoke()
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

/**
 * Streak card component with emoji, value and label
 */
@Composable
fun AppStreakCard(
    value: Int,
    label: String,
    emoji: String,
    color: Color = MaterialTheme.colorScheme.primary,
    unit: String = "gün",
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppTitle(text = emoji, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            AppTitleMedium(
                text = "$value $unit",
                fontWeight = FontWeight.Bold,
                color = color
            )
            AppCaption(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Insight/Alert banner component with icon, optional title, and description
 */
@Composable
fun AppInsightBanner(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    title: String? = null,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.08f),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = if (title != null) Alignment.Top else Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                if (title != null) {
                    AppLabelLarge(
                        text = title,
                        color = color,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                AppBody(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (title != null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Compact stat item displaying value, label, and optional icon vertically aligned
 */
@Composable
fun AppCompactStatItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
        }
        AppTitleMedium(
            text = value,
            fontWeight = FontWeight.Bold,
            color = if (icon == null) color else MaterialTheme.colorScheme.onSurface
        )
        AppCaption(
            text = label,
            color = if (icon == null) color.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

