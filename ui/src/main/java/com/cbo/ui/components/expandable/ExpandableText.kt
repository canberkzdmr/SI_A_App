package com.cbo.ui.components.expandable

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * An expandable text component that limits visible lines and provides a toggle button
 * ("Daha fazla göster" / "Daha az göster") with smooth height animations.
 *
 * @param text Full string content.
 * @param modifier Modifier applied to the container.
 * @param collapsedMaxLines Maximum lines displayed when collapsed (default: 3).
 * @param expandLabel Button text when collapsed (default: "Daha fazla göster").
 * @param collapseLabel Button text when expanded (default: "Daha az göster").
 * @param style Typography style for the text body.
 * @param textColor Color of the body text.
 * @param toggleColor Color of the toggle action button text.
 * @param initialExpanded Initial expansion state.
 */
@Composable
fun AppExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    collapsedMaxLines: Int = 3,
    expandLabel: String = "Daha fazla göster",
    collapseLabel: String = "Daha az göster",
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    toggleColor: Color = MaterialTheme.colorScheme.primary,
    initialExpanded: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(initialExpanded) }
    var isClickable by remember { mutableStateOf(false) }
    var lastTextLayoutResultState by remember { mutableStateOf<Boolean?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(durationMillis = 300))
    ) {
        Text(
            text = text,
            style = style,
            color = textColor,
            maxLines = if (isExpanded) Int.MAX_VALUE else collapsedMaxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { textLayoutResult ->
                if (lastTextLayoutResultState == null) {
                    if (textLayoutResult.hasVisualOverflow || textLayoutResult.lineCount > collapsedMaxLines) {
                        isClickable = true
                    }
                    lastTextLayoutResultState = isClickable
                }
            }
        )

        if (isClickable || isExpanded) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isExpanded) collapseLabel else expandLabel,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = toggleColor,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isExpanded = !isExpanded
                    }
            )
        }
    }
}
