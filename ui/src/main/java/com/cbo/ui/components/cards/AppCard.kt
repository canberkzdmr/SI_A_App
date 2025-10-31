package com.cbo.ui.components.cards

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cbo.ui.theme.AppGradients
import com.cbo.ui.theme.gradientBackground

/**
 * Card variant enum for different visual styles
 */
enum class CardVariant {
    DEFAULT,        // Standard card with subtle elevation
    ELEVATED,       // Card with higher elevation for emphasis
    OUTLINED,       // Card with border instead of elevation
    FILLED,         // Card with filled background
    TONAL,          // Card with tonal background
    SURFACE,        // Card matching surface color
    GLASS          // Liquid glass effect
}

/**
 * Card size enum for different content densities
 */
enum class CardSize {
    SMALL,          // Compact padding and spacing
    MEDIUM,         // Standard padding and spacing
    LARGE           // Generous padding and spacing
}

/**
 * Main AppCard composable with comprehensive customization options
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.GLASS,
    size: CardSize = CardSize.MEDIUM,
    shape: Shape = RoundedCornerShape(12.dp),
    colors: CardColors? = null,
    elevation: CardElevation? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardColors = colors ?: getCardColors(variant)
    val cardElevation = elevation ?: getCardElevation(variant)
    val padding = getCardPadding(size)

    val glassModifier = if (variant == CardVariant.GLASS) {
        modifier
            .fillMaxWidth()
            .gradientBackground(
                brush = AppGradients.surface(),
                shape = shape
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.08f)
                    )
                ),
                shape = shape
            )
    } else {
        modifier.fillMaxWidth()
    }

    Card(
        modifier = glassModifier,
        shape = shape,
        colors = cardColors,
        elevation = cardElevation,
        onClick = onClick ?: {}
    ) {
        Column(
            modifier = Modifier.padding(padding),
            content = content
        )
    }
}

/**
 * Horizontal card layout for side-by-side content
 */
@Composable
fun AppCardHorizontal(
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.DEFAULT,
    size: CardSize = CardSize.MEDIUM,
    shape: Shape = RoundedCornerShape(12.dp),
    colors: CardColors? = null,
    elevation: CardElevation? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    val cardColors = colors ?: getCardColors(variant)
    val cardElevation = elevation ?: getCardElevation(variant)
    val padding = getCardPadding(size)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        colors = cardColors,
        elevation = cardElevation,
        onClick = onClick ?: {}
    ) {
        Row(
            modifier = Modifier.padding(padding),
            content = content
        )
    }
}

/**
 * Get card colors based on variant
 */
@Composable
private fun getCardColors(variant: CardVariant): CardColors {
    return when (variant) {
        CardVariant.DEFAULT -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
        CardVariant.ELEVATED -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
        CardVariant.OUTLINED -> CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
        CardVariant.FILLED -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
        CardVariant.TONAL -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
        CardVariant.SURFACE -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
        CardVariant.GLASS -> CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.04f),
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Get card elevation based on variant
 */
@Composable
private fun getCardElevation(variant: CardVariant): CardElevation {
    return when (variant) {
        CardVariant.DEFAULT -> CardDefaults.cardElevation(defaultElevation = 2.dp)
        CardVariant.ELEVATED -> CardDefaults.cardElevation(defaultElevation = 8.dp)
        CardVariant.OUTLINED -> CardDefaults.cardElevation(defaultElevation = 0.dp)
        CardVariant.FILLED -> CardDefaults.cardElevation(defaultElevation = 0.dp)
        CardVariant.TONAL -> CardDefaults.cardElevation(defaultElevation = 0.dp)
        CardVariant.SURFACE -> CardDefaults.cardElevation(defaultElevation = 1.dp)
        CardVariant.GLASS -> CardDefaults.cardElevation(defaultElevation = 0.dp)
    }
}

/**
 * Get card padding based on size
 */
private fun getCardPadding(size: CardSize): Dp {
    return when (size) {
        CardSize.SMALL -> 8.dp
        CardSize.MEDIUM -> 16.dp
        CardSize.LARGE -> 24.dp
    }
}
