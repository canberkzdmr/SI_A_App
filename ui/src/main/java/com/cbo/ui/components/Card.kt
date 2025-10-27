package com.cbo.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cbo.ui.components.cards.CardVariant
import com.cbo.ui.components.cards.HeaderCard as NewHeaderCard

/**
 * Legacy HeaderCard - DEPRECATED
 * Use com.cbo.ui.components.cards.HeaderCard instead
 * 
 * This function is kept for backward compatibility but delegates to the new card system.
 * Consider migrating to the new HeaderCard for better design system integration.
 */
@Deprecated(
    message = "Use com.cbo.ui.components.cards.HeaderCard instead",
    replaceWith = ReplaceWith(
        "HeaderCard(modifier, iconSelected, title, content, CardVariant.DEFAULT, CardSize.MEDIUM, elevation)"
    )
)
@Composable
fun HeaderCard(
    modifier: Modifier = Modifier,
    iconSelected: ImageVector? = null,
    title: String = "",
    content: String = "",
    elevation: Dp = 0.dp,
) {
    NewHeaderCard(
        modifier = modifier,
        icon = iconSelected,
        title = title,
        content = content,
        variant = CardVariant.DEFAULT,
        size = com.cbo.ui.components.cards.CardSize.MEDIUM,
        elevation = elevation
    )
}