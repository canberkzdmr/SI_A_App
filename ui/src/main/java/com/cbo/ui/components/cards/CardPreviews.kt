package com.cbo.ui.components.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cbo.ui.components.AppBody
import com.cbo.ui.components.AppTitle
import com.cbo.ui.theme.MemCloudApplicationTheme

@Preview(showBackground = true, name = "Card Variants")
@Composable
private fun CardVariantsPreview() {
    MemCloudApplicationTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppTitle(text = "Card Variants")
            
            // Default Card
            AppCard(variant = CardVariant.DEFAULT) {
                AppBody(text = "Default Card - Standard elevation and surface color")
            }
            
            // Elevated Card
            AppCard(variant = CardVariant.ELEVATED) {
                AppBody(text = "Elevated Card - Higher elevation for emphasis")
            }
            
            // Outlined Card
            AppCard(variant = CardVariant.OUTLINED) {
                AppBody(text = "Outlined Card - Border instead of elevation")
            }
            
            // Filled Card
            AppCard(variant = CardVariant.FILLED) {
                AppBody(text = "Filled Card - Primary container background")
            }
            
            // Tonal Card
            AppCard(variant = CardVariant.TONAL) {
                AppBody(text = "Tonal Card - Secondary container background")
            }
            
            // Surface Card
            AppCard(variant = CardVariant.SURFACE) {
                AppBody(text = "Surface Card - Surface container background")
            }
        }
    }
}

@Preview(showBackground = true, name = "Card Sizes")
@Composable
private fun CardSizesPreview() {
    MemCloudApplicationTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppTitle(text = "Card Sizes")
            
            // Small Card
            AppCard(variant = CardVariant.DEFAULT, size = CardSize.SMALL) {
                AppBody(text = "Small Card - Compact padding")
            }
            
            // Medium Card
            AppCard(variant = CardVariant.DEFAULT, size = CardSize.MEDIUM) {
                AppBody(text = "Medium Card - Standard padding")
            }
            
            // Large Card
            AppCard(variant = CardVariant.DEFAULT, size = CardSize.LARGE) {
                AppBody(text = "Large Card - Generous padding")
            }
        }
    }
}

@Preview(showBackground = true, name = "Header Cards")
@Composable
private fun HeaderCardsPreview() {
    MemCloudApplicationTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppTitle(text = "Header Cards")
            
            // Simple header
            HeaderCard(
                title = "Welcome",
                content = "This is a simple header card with title and content"
            )
            
            // Header with icon
            HeaderCard(
                icon = Icons.Default.Info,
                title = "Information",
                content = "This header card includes an icon for better visual hierarchy"
            )
            
            // Tonal header
            HeaderCard(
                icon = Icons.Default.CheckCircle,
                title = "Success",
                content = "This is a tonal header card for success messages",
                variant = CardVariant.TONAL
            )
        }
    }
}

@Preview(showBackground = true, name = "Content Cards")
@Composable
private fun ContentCardsPreview() {
    MemCloudApplicationTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppTitle(text = "Content Cards")
            
            // Simple content
            SimpleContentCard(
                content = "This is a simple content card with just text"
            )
            
            // Content with title
            ContentCard(
                title = "Project Update",
                content = "The project is progressing well. We've completed the initial design phase and are moving into development."
            )
            
            // Content with icon
            ContentCard(
                title = "Meeting Notes",
                subtitle = "Team Standup",
                content = "Discussed project roadmap and upcoming milestones.",
                leadingIcon = Icons.Default.MeetingRoom
            )
            
            // Info card
            InfoCard(
                title = "System Status",
                content = "All systems are operational and running smoothly.",
                icon = Icons.Default.CheckCircle
            )
        }
    }
}

@Preview(showBackground = true, name = "Action Cards")
@Composable
private fun ActionCardsPreview() {
    MemCloudApplicationTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppTitle(text = "Action Cards")
            
            // Action card with multiple actions
            ActionCard(
                title = "Update Available",
                content = "A new version of the app is available with bug fixes and improvements.",
                icon = Icons.Default.Update,
                primaryActionText = "Update Now",
                onPrimaryAction = { },
                secondaryActionText = "Later",
                onSecondaryAction = { }
            )
            
            // Simple action card
            SimpleActionCard(
                title = "Settings",
                content = "Manage your app preferences and configurations",
                actionText = "Open",
                onAction = { },
                icon = Icons.Default.Settings
            )
            
            // Settings card
            SettingsCard(
                title = "Notifications",
                content = "Manage your notification preferences",
                icon = Icons.Default.Notifications,
                trailingIcon = Icons.Default.ChevronRight
            )
        }
    }
}

@Preview(showBackground = true, name = "Horizontal Cards")
@Composable
private fun HorizontalCardsPreview() {
    MemCloudApplicationTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppTitle(text = "Horizontal Cards")
            
            // Horizontal content card
            AppCardHorizontal {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    AppBody(text = "Left Content")
                    AppBody(text = "Right Content")
                }
            }
            
            // Horizontal with icon
            AppCardHorizontal {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    AppBody(text = "User Profile")
                    Spacer(modifier = Modifier.weight(1f))
                    AppBody(text = "Active")
                }
            }
        }
    }
}
