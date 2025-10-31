package com.cbo.ui.components.cards

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    size: CardSize = CardSize.MEDIUM,
    shape: Shape = RoundedCornerShape(16.dp),
    colors: CardColors? = null,
    elevation: CardElevation? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable (androidx.compose.foundation.layout.ColumnScope.() -> Unit)
) {
    AppCard(
        modifier = modifier,
        variant = CardVariant.GLASS,
        size = size,
        shape = shape,
        colors = colors,
        elevation = elevation,
        onClick = onClick,
        content = content
    )
}


