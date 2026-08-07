package com.cbo.ui.components.cards

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardElevation
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cbo.ui.components.AppBody
import com.cbo.ui.components.AppCaption
import com.cbo.ui.components.AppTitleMedium

/**
 * GroupView composable component for grouping related UI elements (e.g. settings sections,
 * form sections, metadata blocks).
 *
 * Built on top of [AppCard] to ensure design system consistency across the application.
 */
@Composable
fun GroupView(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    trailingAction: (@Composable () -> Unit)? = null,
    variant: CardVariant = CardVariant.DEFAULT,
    size: CardSize = CardSize.MEDIUM,
    shape: Shape = RoundedCornerShape(12.dp),
    indicatorColor: Color? = null,
    indicatorPosition: IndicatorPosition = IndicatorPosition.START,
    indicatorEffect: IndicatorEffect = IndicatorEffect.SOLID,
    indicatorSize: IndicatorSize = IndicatorSize.MEDIUM,
    isPinned: Boolean = false,
    pinnedBorderColor: Color? = null,
    colors: CardColors? = null,
    elevation: CardElevation? = null,
    onClick: (() -> Unit)? = null,
    showDividerAfterHeader: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    AppCard(
        modifier = modifier,
        variant = variant,
        size = size,
        shape = shape,
        indicatorColor = indicatorColor,
        indicatorPosition = indicatorPosition,
        indicatorEffect = indicatorEffect,
        indicatorSize = indicatorSize,
        isPinned = isPinned,
        pinnedBorderColor = pinnedBorderColor,
        colors = colors,
        elevation = elevation,
        onClick = onClick
    ) {
        // Group Header Section
        val hasHeader = title != null || subtitle != null || leadingIcon != null || trailingAction != null
        if (hasHeader) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (showDividerAfterHeader) 12.dp else 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (leadingIcon != null) {
                        Icon(
                            imageVector = leadingIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp)
                        )
                    }

                    Column {
                        title?.let {
                            AppTitleMedium(
                                text = it,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        subtitle?.let {
                            Spacer(modifier = Modifier.height(2.dp))
                            AppCaption(
                                text = it,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (trailingAction != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    trailingAction()
                }
            }

            if (showDividerAfterHeader) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }

        // Group Body Content
        content()
    }
}

/**
 * Reusable GroupItem composable representing a row within a [GroupView].
 */
@Composable
fun GroupItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    leadingIconTint: Color = MaterialTheme.colorScheme.primary,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    showDivider: Boolean = false
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) {
                        Modifier.clickable(enabled = enabled, onClick = onClick)
                    } else {
                        Modifier
                    }
                )
                .padding(vertical = 10.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = if (enabled) leadingIconTint else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        modifier = Modifier
                            .size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column {
                    AppBody(
                        text = title,
                        color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        AppCaption(
                            text = subtitle,
                            color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                            maxLines = 2
                        )
                    }
                }
            }

            if (trailingContent != null) {
                Spacer(modifier = Modifier.width(12.dp))
                trailingContent()
            }
        }

        if (showDivider) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                thickness = 1.dp,
                modifier = Modifier.padding(start = if (leadingIcon != null) 32.dp else 4.dp)
            )
        }
    }
}
