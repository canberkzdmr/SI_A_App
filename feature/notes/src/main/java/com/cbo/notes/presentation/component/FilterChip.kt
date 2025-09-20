package com.cbo.notes.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.lang.Math.pow
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    color: String? = null,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected) {
        color?.let { Color(android.graphics.Color.parseColor(it)) } 
            ?: MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    val contentColor = if (selected) {
        if (color != null) {
            // Calculate contrasting color for custom colors
            val bgColor = Color(android.graphics.Color.parseColor(color))
            if (bgColor.luminance() > 0.5f) Color.Black else Color.White
        } else {
            MaterialTheme.colorScheme.onPrimary
        }
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            selectedContainerColor = backgroundColor,
            labelColor = MaterialTheme.colorScheme.onSurface,
            selectedLabelColor = contentColor
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected && color != null) backgroundColor else MaterialTheme.colorScheme.outline,
            selectedBorderColor = backgroundColor
        ),
        modifier = modifier
    )
}

@Composable
private fun Color.luminance(): Float {
    fun componentLuminance(component: Float): Float {
        return if (component <= 0.03928f) {
            component / 12.92f
        } else {
            ((component + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()
        }
    }

    val r = componentLuminance(red)
    val g = componentLuminance(green)
    val b = componentLuminance(blue)

    return 0.2126f * r + 0.7152f * g + 0.0722f * b
}
