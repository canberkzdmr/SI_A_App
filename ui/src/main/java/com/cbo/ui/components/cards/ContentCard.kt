package com.cbo.ui.components.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.cbo.ui.components.AppBody
import com.cbo.ui.components.AppTitle
import com.cbo.ui.components.AppTitleMedium

/**
 * Content card for displaying structured information
 */
@Composable
fun ContentCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    content: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    variant: CardVariant = CardVariant.DEFAULT,
    size: CardSize = CardSize.MEDIUM,
    onClick: (() -> Unit)? = null
) {
    AppCard(
        modifier = modifier,
        variant = variant,
        size = size,
        onClick = onClick
    ) {
        if (title != null || subtitle != null || content != null || leadingIcon != null || trailingIcon != null) {
            // Header section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (leadingIcon != null) {
                        Icon(
                            imageVector = leadingIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Column {
                        if (title != null) {
                            AppTitleMedium(text = title)
                        }
                        if (subtitle != null) {
                            AppBody(
                                text = subtitle,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
            
            if (content != null) {
                Spacer(modifier = Modifier.height(8.dp))
                AppBody(text = content)
            }
        }
        
    }
}

/**
 * Simple content card with just text content
 */
@Composable
fun SimpleContentCard(
    modifier: Modifier = Modifier,
    content: String,
    variant: CardVariant = CardVariant.DEFAULT,
    size: CardSize = CardSize.MEDIUM,
    onClick: (() -> Unit)? = null
) {
    AppCard(
        modifier = modifier,
        variant = variant,
        size = size,
        onClick = onClick
    ) {
        AppBody(text = content)
    }
}

/**
 * Info card for displaying informational content
 */
@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    title: String,
    content: String,
    icon: ImageVector? = null,
    variant: CardVariant = CardVariant.TONAL,
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
                Spacer(modifier = Modifier.height(4.dp))
                AppBody(
                    text = content,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}
