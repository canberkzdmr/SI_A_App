package com.cbo.notes.presentation.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import kotlin.math.pow

@Composable
fun FilterChip(
    modifier: Modifier = Modifier,
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    color: String? = null,
    isDeleteMode: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    val backgroundColor =
        if (selected) {
            color?.let {
                if (it.isNotBlank()) {
                    Color(it.toColorInt())
                } else {
                    MaterialTheme.colorScheme.primary
                }
            }
                ?: MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surface
        }

    val contentColor =
        if (selected) {
            if (color != null && color.isNotBlank()) {
                val bgColor = Color(color.toColorInt())
                if (bgColor.luminance() > 0.5f) Color.Black else Color.White
            } else {
                MaterialTheme.colorScheme.onPrimary
            }
        } else {
            MaterialTheme.colorScheme.onSurface
        }

    val targetBorderColor =
        when {
            isDeleteMode -> MaterialTheme.colorScheme.error
            selected && color != null && (color.isNotBlank()) -> backgroundColor
            else -> MaterialTheme.colorScheme.outline
        }

    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = tween(durationMillis = 300), // adjust speed if needed
        label = "borderColorAnim",
    )

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
            )
        },
        colors =
            FilterChipDefaults.filterChipColors(
                containerColor = backgroundColor,
                selectedContainerColor = backgroundColor,
                labelColor = contentColor,
                selectedLabelColor = contentColor,
            ),
        border =
            FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = selected,
                borderColor = animatedBorderColor,
                selectedBorderColor = animatedBorderColor,
            ),
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        modifier = modifier,
    )
}

@Composable
private fun Color.luminance(): Float {
    fun componentLuminance(component: Float): Float =
        if (component <= 0.03928f) {
            component / 12.92f
        } else {
            ((component + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()
        }

    val r = componentLuminance(red)
    val g = componentLuminance(green)
    val b = componentLuminance(blue)

    return 0.2126f * r + 0.7152f * g + 0.0722f * b
}

@Preview(showBackground = true)
@Composable
fun FilterChipPreview() {
    MaterialTheme {
        Column(
            modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = false,
                onClick = {},
                label = "Unselected",
            )
            FilterChip(
                selected = false,
                onClick = {},
                label = "Unselected",
                isDeleteMode = true,
            )
            FilterChip(
                selected = true,
                onClick = {},
                label = "Selected Default",
            )
            FilterChip(
                selected = true,
                onClick = {},
                label = "Selected Default",
                isDeleteMode = true,
            )
            FilterChip(
                selected = true,
                onClick = {},
                label = "Custom Color",
                color = "#FF9800", // orange
            )
            FilterChip(
                selected = true,
                onClick = {},
                label = "With Icons",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}
