package com.cbo.ui.components.expandable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cbo.ui.components.cards.CardVariant

/**
 * Operating mode for [AppAccordion].
 */
enum class AccordionMode {
    /** Only one section can be expanded at any given time. Opening a new section collapses others. */
    SINGLE,
    /** Multiple sections can be expanded simultaneously. */
    MULTIPLE
}

/**
 * Visual presentation style for [AppAccordion].
 */
enum class AccordionStyle {
    /** Renders accordion items inside styled card containers ([AppExpandableCard]). */
    CARD,
    /** Renders accordion items as clean borderless list tiles ([AppExpandableListItem]) with optional dividers. */
    BORDERLESS
}

/**
 * Data representation of a single section within an [AppAccordion].
 */
data class AccordionItem<T>(
    val id: T,
    val title: String,
    val subtitle: String? = null,
    val leadingIcon: ImageVector? = null,
    val badgeText: String? = null,
    val content: @Composable () -> Unit
)

/**
 * An Accordion component that coordinates state across multiple expandable sections.
 *
 * @param items List of accordion items to render.
 * @param modifier Modifier applied to the accordion column container.
 * @param mode Expansion mode ([AccordionMode.SINGLE] or [AccordionMode.MULTIPLE]).
 * @param style Visual style ([AccordionStyle.CARD] or [AccordionStyle.BORDERLESS]).
 * @param cardVariant Card variant when [style] is [AccordionStyle.CARD].
 * @param showDividers Whether to display dividers when [style] is [AccordionStyle.BORDERLESS].
 * @param initialExpandedId Initially expanded item ID when in [AccordionMode.SINGLE] mode.
 * @param initialExpandedIds Initially expanded item IDs when in [AccordionMode.MULTIPLE] mode.
 * @param itemSpacing Vertical spacing between items (default: 8.dp for CARD, 0.dp for BORDERLESS).
 */
@Composable
fun <T> AppAccordion(
    items: List<AccordionItem<T>>,
    modifier: Modifier = Modifier,
    mode: AccordionMode = AccordionMode.SINGLE,
    style: AccordionStyle = AccordionStyle.CARD,
    cardVariant: CardVariant = CardVariant.OUTLINED,
    showDividers: Boolean = true,
    initialExpandedId: T? = null,
    initialExpandedIds: Set<T> = emptySet(),
    itemSpacing: Dp = if (style == AccordionStyle.CARD) 8.dp else 0.dp
) {
    var expandedSingleId by remember { mutableStateOf(initialExpandedId ?: items.firstOrNull()?.id) }
    var expandedMultipleIds by remember { mutableStateOf(initialExpandedIds) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(itemSpacing)
    ) {
        items.forEachIndexed { index, item ->
            val isExpanded = when (mode) {
                AccordionMode.SINGLE -> expandedSingleId == item.id
                AccordionMode.MULTIPLE -> expandedMultipleIds.contains(item.id)
            }

            val onExpandedChange: (Boolean) -> Unit = { newlyExpanded ->
                when (mode) {
                    AccordionMode.SINGLE -> {
                        expandedSingleId = if (newlyExpanded) item.id else null
                    }
                    AccordionMode.MULTIPLE -> {
                        expandedMultipleIds = if (newlyExpanded) {
                            expandedMultipleIds + item.id
                        } else {
                            expandedMultipleIds - item.id
                        }
                    }
                }
            }

            when (style) {
                AccordionStyle.CARD -> {
                    AppExpandableCard(
                        title = item.title,
                        subtitle = item.subtitle,
                        leadingIcon = item.leadingIcon,
                        variant = cardVariant,
                        expanded = isExpanded,
                        onExpandedChange = onExpandedChange,
                        content = item.content
                    )
                }
                AccordionStyle.BORDERLESS -> {
                    val isLastItem = index == items.lastIndex
                    AppExpandableListItem(
                        title = item.title,
                        subtitle = item.subtitle,
                        leadingIcon = item.leadingIcon,
                        badgeText = item.badgeText,
                        expanded = isExpanded,
                        onExpandedChange = onExpandedChange,
                        showDivider = showDividers && !isLastItem,
                        content = item.content
                    )
                }
            }
        }
    }
}
