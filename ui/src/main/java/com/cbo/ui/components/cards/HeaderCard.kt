package com.cbo.ui.components.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.cbo.ui.components.AppBody
import com.cbo.ui.components.AppTitle

/**
 * Header card with icon, title, and content
 * Replaces the existing HeaderCard with improved design system integration
 */
@Composable
fun HeaderCard(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    title: String = "",
    content: String = "",
    variant: CardVariant = CardVariant.DEFAULT,
    size: CardSize = CardSize.MEDIUM,
    elevation: Dp? = null,
    onClick: (() -> Unit)? = null
) {
    AppCard(
        modifier = modifier,
        variant = variant,
        size = size,
        elevation = elevation?.let { CardDefaults.cardElevation(defaultElevation = it) },
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            
            if (title.isNotEmpty()) {
                AppTitle(
                    text = title,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            
            if (title.isNotEmpty() && content.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            if (content.isNotEmpty()) {
                AppBody(
                    text = content,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}
