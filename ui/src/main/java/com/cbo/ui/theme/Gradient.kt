package com.cbo.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Shape

/**
 * Central place for app gradients and a convenient modifier.
 */
object AppGradients {
    @Composable
    fun primary(): Brush {
        val start: Color = MaterialTheme.colorScheme.primary
        val end: Color = MaterialTheme.colorScheme.tertiary
        return Brush.linearGradient(listOf(start, end))
    }

    @Composable
    fun surface(): Brush {
        val start: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
        val end: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
        return Brush.linearGradient(listOf(start, end))
    }
}

fun Modifier.gradientBackground(brush: Brush, shape: Shape): Modifier =
    this
        .background(brush = brush, shape = shape)
        .clip(shape)


