package com.cbo.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Main AppCard composable with comprehensive customization options
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
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
    content: @Composable ColumnScope.() -> Unit
) {
    val cardColors = colors ?: getCardColors(variant)
    val cardElevation = elevation ?: getCardElevation(variant)
    val padding = getCardPadding(size)
    val indicatorThickness = getIndicatorThickness(indicatorSize)

    val finalModifier = modifier
        .fillMaxWidth()
        .then(
            when {
                // Pinned cards get a distinctive border with category/custom color or primary as fallback
                isPinned -> Modifier.border(
                    width = 1.5.dp,
                    color = pinnedBorderColor ?: MaterialTheme.colorScheme.primary,
                    shape = shape
                )
                // Outlined variant gets a subtle border
                variant == CardVariant.OUTLINED -> Modifier.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = shape
                )
                else -> Modifier
            }
        )

    val cardContent: @Composable ColumnScope.() -> Unit = {
        // 1. Standard layout (No Indicator)
        if (indicatorColor == null) {
            Column(
                modifier = Modifier.padding(padding),
                content = content
            )
        }
        // 2. Layout with Indicator
        else {
            // Define the indicator strip with effect
            val indicator = @Composable {
                Box(
                    modifier = Modifier
                        .then(
                            when (indicatorEffect) {
                                IndicatorEffect.SOLID -> Modifier.background(indicatorColor)
                                IndicatorEffect.GRADIENT -> Modifier.background(
                                    brush = getGradientBrush(
                                        color = indicatorColor,
                                        position = indicatorPosition
                                    )
                                )
                                IndicatorEffect.STRIPED -> Modifier.background(
                                    brush = getStripedBrush(
                                        color = indicatorColor,
                                        position = indicatorPosition
                                    )
                                )
                                IndicatorEffect.DOTTED -> Modifier.background(
                                    brush = getDottedBrush(
                                        color = indicatorColor,
                                        position = indicatorPosition
                                    )
                                )
                                IndicatorEffect.DUAL_TONE -> Modifier.background(
                                    brush = getDualToneBrush(
                                        color = indicatorColor,
                                        position = indicatorPosition
                                    )
                                )
                                IndicatorEffect.FADE_IN_OUT -> Modifier.background(
                                    brush = getFadeInOutBrush(
                                        color = indicatorColor,
                                        position = indicatorPosition
                                    )
                                )
                            }
                        )
                        .then(
                            // Adjust dimensions based on orientation
                            when (indicatorPosition) {
                                IndicatorPosition.START, IndicatorPosition.END ->
                                    Modifier.fillMaxHeight().width(indicatorThickness)
                                IndicatorPosition.TOP, IndicatorPosition.BOTTOM ->
                                    Modifier.fillMaxWidth().height(indicatorThickness)
                            }
                        )
                )
            }

            // Apply specific layout container based on position
            when (indicatorPosition) {
                IndicatorPosition.START, IndicatorPosition.END -> {
                    // Use IntrinsicSize.Min so the Column height matches content
                    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                        if (indicatorPosition == IndicatorPosition.START) indicator()

                        // The actual content column
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(padding),
                            content = content
                        )

                        if (indicatorPosition == IndicatorPosition.END) indicator()
                    }
                }
                IndicatorPosition.TOP, IndicatorPosition.BOTTOM -> {
                    Column {
                        if (indicatorPosition == IndicatorPosition.TOP) indicator()

                        // The actual content column
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(padding),
                            content = content
                        )

                        if (indicatorPosition == IndicatorPosition.BOTTOM) indicator()
                    }
                }
            }
        }
    }

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = finalModifier,
            shape = shape,
            colors = cardColors,
            elevation = cardElevation,
            content = cardContent
        )
    } else {
        Card(
            modifier = finalModifier,
            shape = shape,
            colors = cardColors,
            elevation = cardElevation,
            content = cardContent
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
    indicatorColor: Color? = null,
    indicatorPosition: IndicatorPosition = IndicatorPosition.START,
    indicatorEffect: IndicatorEffect = IndicatorEffect.SOLID,
    indicatorSize: IndicatorSize = IndicatorSize.MEDIUM,
    isPinned: Boolean = false,
    pinnedBorderColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    val cardColors = colors ?: getCardColors(variant)
    val cardElevation = elevation ?: getCardElevation(variant)
    val padding = getCardPadding(size)
    val indicatorThickness = getIndicatorThickness(indicatorSize)

    val finalModifier = modifier
        .fillMaxWidth()
        .then(
            when {
                // Pinned cards get a distinctive border with category/custom color or primary as fallback
                isPinned -> Modifier.border(
                    width = 1.5.dp,
                    color = pinnedBorderColor ?: MaterialTheme.colorScheme.primary,
                    shape = shape
                )
                // Outlined variant gets a subtle border
                variant == CardVariant.OUTLINED -> Modifier.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = shape
                )
                else -> Modifier
            }
        )

    val cardContent: @Composable ColumnScope.() -> Unit = {
        // 1. Standard layout (No Indicator)
        if (indicatorColor == null) {
            Row(
                modifier = Modifier.padding(padding),
                content = content
            )
        }
        // 2. Layout with Indicator
        else {
            // Define the indicator strip with effect
            val indicator = @Composable {
                Box(
                    modifier = Modifier
                        .then(
                            when (indicatorEffect) {
                                IndicatorEffect.SOLID -> Modifier.background(indicatorColor)
                                IndicatorEffect.GRADIENT -> Modifier.background(
                                    brush = getGradientBrush(
                                        color = indicatorColor,
                                        position = indicatorPosition
                                    )
                                )
                                IndicatorEffect.STRIPED -> Modifier.background(
                                    brush = getStripedBrush(
                                        color = indicatorColor,
                                        position = indicatorPosition
                                    )
                                )
                                IndicatorEffect.DOTTED -> Modifier.background(
                                    brush = getDottedBrush(
                                        color = indicatorColor,
                                        position = indicatorPosition
                                    )
                                )
                                IndicatorEffect.DUAL_TONE -> Modifier.background(
                                    brush = getDualToneBrush(
                                        color = indicatorColor,
                                        position = indicatorPosition
                                    )
                                )
                                IndicatorEffect.FADE_IN_OUT -> Modifier.background(
                                    brush = getFadeInOutBrush(
                                        color = indicatorColor,
                                        position = indicatorPosition
                                    )
                                )
                            }
                        )
                        .then(
                            // Adjust dimensions based on orientation
                            when (indicatorPosition) {
                                IndicatorPosition.START, IndicatorPosition.END ->
                                    Modifier.fillMaxHeight().width(indicatorThickness)
                                IndicatorPosition.TOP, IndicatorPosition.BOTTOM ->
                                    Modifier.fillMaxWidth().height(indicatorThickness)
                            }
                        )
                )
            }

            // Apply specific layout container based on position
            when (indicatorPosition) {
                IndicatorPosition.START, IndicatorPosition.END -> {
                    // Use IntrinsicSize.Min so the Row height matches the tallest content
                    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                        if (indicatorPosition == IndicatorPosition.START) indicator()

                        // The actual content row
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(padding),
                            content = content
                        )

                        if (indicatorPosition == IndicatorPosition.END) indicator()
                    }
                }
                IndicatorPosition.TOP, IndicatorPosition.BOTTOM -> {
                    Column {
                        if (indicatorPosition == IndicatorPosition.TOP) indicator()

                        // The actual content row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(padding),
                            content = content
                        )

                        if (indicatorPosition == IndicatorPosition.BOTTOM) indicator()
                    }
                }
            }
        }
    }

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = finalModifier,
            shape = shape,
            colors = cardColors,
            elevation = cardElevation,
            content = cardContent
        )
    } else {
        Card(
            modifier = finalModifier,
            shape = shape,
            colors = cardColors,
            elevation = cardElevation,
            content = cardContent
        )
    }
}

/**
 * Create gradient brush based on indicator position
 * Gradient goes from solid color to transparent
 */
private fun getGradientBrush(color: Color, position: IndicatorPosition): Brush {
    return when (position) {
        IndicatorPosition.START -> Brush.horizontalGradient(
            colors = listOf(color, Color.Transparent)
        )
        IndicatorPosition.END -> Brush.horizontalGradient(
            colors = listOf(Color.Transparent, color)
        )
        IndicatorPosition.TOP -> Brush.verticalGradient(
            colors = listOf(color, Color.Transparent)
        )
        IndicatorPosition.BOTTOM -> Brush.verticalGradient(
            colors = listOf(Color.Transparent, color)
        )
    }
}

/**
 * Create striped brush pattern
 * Creates alternating bands of color and transparent
 */
private fun getStripedBrush(color: Color, position: IndicatorPosition): Brush {
    val colors = listOf(
        color,
        color,
        Color.Transparent,
        Color.Transparent,
        color,
        color,
        Color.Transparent,
        Color.Transparent
    )

    val colorStops = listOf(
        0.0f,
        0.125f,
        0.125f,
        0.25f,
        0.25f,
        0.375f,
        0.375f,
        0.5f
    ).zip(colors).toTypedArray()

    return when (position) {
        IndicatorPosition.START, IndicatorPosition.END ->
            Brush.verticalGradient(colorStops = colorStops)
        IndicatorPosition.TOP, IndicatorPosition.BOTTOM ->
            Brush.horizontalGradient(colorStops = colorStops)
    }
}

/**
 * Create dotted brush pattern
 * Creates repeating dots along the indicator
 */
private fun getDottedBrush(color: Color, position: IndicatorPosition): Brush {
    // Creates small dots with spacing
    val colors = listOf(
        color,
        color,
        Color.Transparent,
        Color.Transparent,
        Color.Transparent,
        color,
        color,
        Color.Transparent,
        Color.Transparent,
        Color.Transparent
    )

    val colorStops = listOf(
        0.0f,
        0.1f,
        0.1f,
        0.2f,
        0.3f,
        0.3f,
        0.4f,
        0.4f,
        0.5f,
        0.6f
    ).zip(colors).toTypedArray()

    return when (position) {
        IndicatorPosition.START, IndicatorPosition.END ->
            Brush.verticalGradient(colorStops = colorStops)
        IndicatorPosition.TOP, IndicatorPosition.BOTTOM ->
            Brush.horizontalGradient(colorStops = colorStops)
    }
}

/**
 * Create dual-tone gradient brush
 * Gradient from primary color to a lighter/darker variant
 */
private fun getDualToneBrush(color: Color, position: IndicatorPosition): Brush {
    // Create a lighter version of the color for the gradient
    val lighterColor = color.copy(alpha = 0.5f)

    return when (position) {
        IndicatorPosition.START -> Brush.horizontalGradient(
            colors = listOf(color, lighterColor)
        )
        IndicatorPosition.END -> Brush.horizontalGradient(
            colors = listOf(lighterColor, color)
        )
        IndicatorPosition.TOP -> Brush.verticalGradient(
            colors = listOf(color, lighterColor)
        )
        IndicatorPosition.BOTTOM -> Brush.verticalGradient(
            colors = listOf(lighterColor, color)
        )
    }
}

/**
 * Create fade in-out brush pattern
 * Color fades to transparent in middle and back to color
 */
private fun getFadeInOutBrush(color: Color, position: IndicatorPosition): Brush {
    val colors = listOf(
        color,
        color.copy(alpha = 0.7f),
        Color.Transparent,
        color.copy(alpha = 0.7f),
        color
    )

    val colorStops = colors.mapIndexed { index, c ->
        Pair(index / (colors.size - 1).toFloat(), c)
    }.toTypedArray()

    return when (position) {
        IndicatorPosition.START, IndicatorPosition.END ->
            Brush.verticalGradient(colorStops = colorStops)
        IndicatorPosition.TOP, IndicatorPosition.BOTTOM ->
            Brush.horizontalGradient(colorStops = colorStops)
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
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
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

/**
 * Get indicator thickness based on size
 */
private fun getIndicatorThickness(size: IndicatorSize): Dp {
    return when (size) {
        IndicatorSize.THIN -> 4.dp
        IndicatorSize.MEDIUM -> 8.dp
        IndicatorSize.THICK -> 12.dp
    }
}

/**
 * Card variant enum for different visual styles
 */
enum class CardVariant {
    DEFAULT,        // Standard card with subtle elevation
    ELEVATED,       // Card with higher elevation for emphasis
    OUTLINED,       // Card with border instead of elevation
    FILLED,         // Card with filled background
    TONAL,          // Card with tonal background
    SURFACE        // Card matching surface color
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
 * Indicator effect enum for different visual styles
 */
enum class IndicatorEffect {
    SOLID,          // Solid color indicator
    GRADIENT,       // Gradient from color to transparent
    STRIPED,        // Striped pattern with color and transparent alternating
    DOTTED,         // Repeating dot pattern
    DUAL_TONE,      // Two-color gradient (needs secondary color)
    FADE_IN_OUT     // Gradient that fades color -> transparent -> color
}

enum class IndicatorPosition {
    START, END, TOP, BOTTOM
}

/**
 * Indicator size enum for different thickness options
 */
enum class IndicatorSize {
    THIN,           // 4dp - Subtle indicator
    MEDIUM,         // 8dp - Standard indicator
    THICK           // 12dp - Bold indicator
}
