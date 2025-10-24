package com.cbo.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cbo.ui.components.cards.HeaderCard as NewHeaderCard
import com.cbo.ui.components.cards.CardVariant

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