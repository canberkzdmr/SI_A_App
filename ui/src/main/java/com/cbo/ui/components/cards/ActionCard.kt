package com.cbo.ui.components.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.cbo.ui.components.AppBody
import com.cbo.ui.components.PrimaryButton
import com.cbo.ui.components.AppTitleMedium
import com.cbo.ui.components.SecondaryButton
import com.cbo.ui.components.TertiaryButton

/**
 * Action card with primary and secondary actions
 */
@Composable
fun ActionCard(
    modifier: Modifier = Modifier,
    title: String,
    content: String? = null,
    icon: ImageVector? = null,
    primaryActionText: String? = null,
    onPrimaryAction: (() -> Unit)? = null,
    secondaryActionText: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
    variant: CardVariant = CardVariant.GLASS,
    size: CardSize = CardSize.MEDIUM
) {
    AppCard(
        modifier = modifier,
        variant = variant,
        size = size
    ) {
        Column {
            // Header with icon and title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                AppTitleMedium(text = title)
            }
            
            if (content != null) {
                Spacer(modifier = Modifier.height(8.dp))
                AppBody(
                    text = content,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
            
            // Actions
            if (primaryActionText != null || secondaryActionText != null) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (primaryActionText != null && onPrimaryAction != null) {
                        PrimaryButton(
                            text = primaryActionText,
                            onClick = onPrimaryAction
                        )
                    }
                    
                    if (secondaryActionText != null && onSecondaryAction != null) {
                        SecondaryButton(
                            text = secondaryActionText,
                            onClick = onSecondaryAction
                        )
                    }
                }
            }
        }
    }
}

/**
 * Simple action card with single action
 */
@Composable
fun SimpleActionCard(
    modifier: Modifier = Modifier,
    title: String,
    content: String? = null,
    actionText: String,
    onAction: () -> Unit,
    icon: ImageVector? = null,
    variant: CardVariant = CardVariant.GLASS,
    size: CardSize = CardSize.MEDIUM
) {
    AppCard(
        modifier = modifier,
        variant = variant,
        size = size,
        onClick = onAction
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column {
                    AppTitleMedium(text = title)
                    if (content != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        AppBody(
                            text = content,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            TertiaryButton(
                text = actionText,
                onClick = onAction
            )
        }
    }
}

/**
 * Settings card for configuration options
 */
@Composable
fun SettingsCard(
    modifier: Modifier = Modifier,
    title: String,
    content: String? = null,
    icon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    variant: CardVariant = CardVariant.GLASS,
    size: CardSize = CardSize.MEDIUM,
    onClick: (() -> Unit)? = null
) {
    AppCard(
        modifier = modifier,
        variant = variant,
        size = size,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column {
                    AppTitleMedium(text = title)
                    if (content != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        AppBody(
                            text = content,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            if (trailingIcon != null) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
