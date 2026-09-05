package com.cbo.ui.components.expandable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.stringResource
import com.cbo.ui.R
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
import com.cbo.ui.components.cards.AppCard
import com.cbo.ui.components.cards.CardVariant

/**
 * An expandable card container component built on top of [AppCard].
 *
 * Supports both controlled and uncontrolled states.
 *
 * @param title The primary title text for the card header.
 * @param modifier Modifier to be applied to the card.
 * @param subtitle Optional secondary subtitle text for the card header.
 * @param leadingIcon Optional icon displayed before the title.
 * @param expanded State parameter when controlled externally.
 * @param onExpandedChange State change callback when controlled externally.
 * @param initialExpanded Initial state when used in uncontrolled mode.
 * @param variant Visual variant of the card (DEFAULT, ELEVATED, OUTLINED, etc.).
 * @param headerTrailingContent Optional custom composable slot placed before the expand arrow.
 * @param content The expandable body content composable slot.
 */
@Composable
fun AppExpandableCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    leadingIconTint: Color = MaterialTheme.colorScheme.primary,
    expanded: Boolean? = null,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    initialExpanded: Boolean = false,
    variant: CardVariant = CardVariant.OUTLINED,
    headerTrailingContent: (@Composable () -> Unit)? = null,
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
        label = "ExpandableCardArrowRotation"
    )

    AppCard(
        modifier = modifier.fillMaxWidth(),
        variant = variant
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = toggleExpand
                    ),
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
                            modifier = Modifier.padding(end = 12.dp)
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
                    if (headerTrailingContent != null) {
                        headerTrailingContent()
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

            // Expandable Content Body
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    content()
                }
            }
        }
    }
}
