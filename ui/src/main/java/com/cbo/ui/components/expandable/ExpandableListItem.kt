package com.cbo.ui.components.expandable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.stringResource
import com.cbo.ui.R
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.cbo.ui.components.AppBody
import com.cbo.ui.components.AppTitleMedium

/**
 * An expandable list item tile composable, ideal for settings groups, FAQs, or nested lists.
 *
 * @param title Primary title text.
 * @param modifier Modifier applied to the item surface.
 * @param subtitle Optional subtitle text.
 * @param leadingIcon Optional leading icon vector.
 * @param leadingIconTint Tint color for the leading icon.
 * @param badgeText Optional badge or status text displayed near the arrow.
 * @param expanded Controlled expanded state.
 * @param onExpandedChange Callback when expanded state changes.
 * @param initialExpanded Default state when uncontrolled.
 * @param showDivider Whether to render a horizontal divider at the bottom.
 * @param content Body content expanded when visible.
 */
@Composable
fun AppExpandableListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    leadingIconTint: Color = MaterialTheme.colorScheme.primary,
    badgeText: String? = null,
    badgeColor: Color = MaterialTheme.colorScheme.primaryContainer,
    badgeTextColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    expanded: Boolean? = null,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    initialExpanded: Boolean = false,
    showDivider: Boolean = true,
    content: @Composable () -> Unit
) {
    var internalExpanded by remember { mutableStateOf(initialExpanded) }
    val isExpanded = expanded ?: internalExpanded

    val toggleExpand = {
        val newState = !isExpanded
        if (onExpandedChange != null) {
            onExpandedChange(newState)
        } else {
            internalExpanded = newState
        }
    }

    val arrowRotationDegree by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "ExpandableListItemArrowRotation"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = toggleExpand),
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
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
                            tint = leadingIconTint,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        AppTitleMedium(
                            text = title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (!subtitle.isNullOrBlank()) {
                            AppBody(
                                text = subtitle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!badgeText.isNullOrBlank()) {
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = badgeColor,
                            contentColor = badgeTextColor
                        ) {
                            AppBody(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) {
                            stringResource(id = R.string.collapse)
                        } else {
                            stringResource(id = R.string.expand)
                        },
                        modifier = Modifier.rotate(arrowRotationDegree),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
            exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                content()
            }
        }

        if (showDivider) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp
            )
        }
    }
}
