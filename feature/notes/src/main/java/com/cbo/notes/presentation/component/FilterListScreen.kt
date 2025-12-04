package com.cbo.notes.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cbo.notes.R
import com.cbo.notes.domain.model.Category
import com.cbo.notes.domain.model.Tag
import com.cbo.ui.components.AppBody
import com.cbo.ui.components.AppLabel
import com.cbo.ui.components.AppTitle
import com.cbo.ui.components.ScreenWithTopBarAndInsets
import com.cbo.ui.components.TertiaryButton
import com.cbo.ui.theme.MemCloudApplicationTheme

/**
 * Enum representing the types of filters available
 */
enum class FilterType {
    CATEGORY,
    TAGS,
    PINNED,
    FAVORITE,
}

/**
 * Screen that displays a list of available filter types.
 * Users can select a filter type to navigate to the detail screen
 * or clear all applied filters.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterListScreen(
    selectedCategories: List<Category>,
    selectedTags: List<Tag>,
    filterPinned: Boolean,
    filterFavorites: Boolean,
    onNavigateBack: () -> Unit,
    onFilterTypeClick: (FilterType) -> Unit,
    onPinnedToggle: () -> Unit,
    onFavoritesToggle: () -> Unit,
    onClearAllFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasActiveFilters = selectedCategories.isNotEmpty() || selectedTags.isNotEmpty() || filterPinned || filterFavorites

    ScreenWithTopBarAndInsets(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.filters),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
                actions = {
                    if (hasActiveFilters) {
                        TertiaryButton(
                            text = stringResource(id = R.string.clear_all),
                            onClick = onClearAllFilters
                        )
                    }
                }
            )
        }
    ) {
        FilterListContent(
            selectedCategories = selectedCategories,
            selectedTags = selectedTags,
            isPinnedChecked = filterPinned,
            isFavoritedChecked = filterFavorites,
            onFilterTypeClick = onFilterTypeClick,
            onPinnedToggle = onPinnedToggle,
            onFavoritesToggle = onFavoritesToggle
        )
    }
}

@Composable
private fun FilterListContent(
    selectedCategories: List<Category>,
    selectedTags: List<Tag>,
    isPinnedChecked: Boolean,
    isFavoritedChecked: Boolean,
    onFilterTypeClick: (FilterType) -> Unit,
    onPinnedToggle: () -> Unit,
    onFavoritesToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasActiveFilters = selectedCategories.isNotEmpty() || selectedTags.isNotEmpty() || isPinnedChecked || isFavoritedChecked

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
    ) {
        // Filter Types List
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterTypeItem(
                    icon = Icons.Default.Category,
                    title = stringResource(id = R.string.categories),
                    subtitle = if (selectedCategories.isEmpty()) {
                        stringResource(id = R.string.no_category_selected)
                    } else {
                        stringResource(id = R.string.selected_count, selectedCategories.size)
                    },
                    hasSelection = selectedCategories.isNotEmpty(),
                    onClick = { onFilterTypeClick(FilterType.CATEGORY) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            item {
                FilterTypeItem(
                    icon = Icons.AutoMirrored.Filled.Label,
                    title = stringResource(id = R.string.tags),
                    subtitle = if (selectedTags.isEmpty()) {
                        stringResource(id = R.string.no_tags_selected)
                    } else {
                        stringResource(id = R.string.selected_count, selectedTags.size)
                    },
                    hasSelection = selectedTags.isNotEmpty(),
                    onClick = { onFilterTypeClick(FilterType.TAGS) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            item {
                CheckboxListItem(
                    icon = Icons.Default.PushPin,
                    title = stringResource(R.string.note_pinned),
                    isChecked = isPinnedChecked,
                    onCheckedChange = { onPinnedToggle() }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            item {
                CheckboxListItem(
                    icon = Icons.Default.Star,
                    title = stringResource(R.string.added_to_favorites),
                    isChecked = isFavoritedChecked,
                    onCheckedChange = { onFavoritesToggle() }
                )
            }
        }

        // Active Filters Summary
        if (hasActiveFilters) {
            Spacer(modifier = Modifier.weight(1f))
            ActiveFiltersSummary(
                selectedCategories = selectedCategories,
                selectedTags = selectedTags,
                isPinned = isPinnedChecked,
                isFavorites = isFavoritedChecked,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun FilterTypeItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    hasSelection: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = {
            AppTitle(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        },
        supportingContent = {
            AppBody(
                text = subtitle,
                color = if (hasSelection) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (hasSelection) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(24.dp)
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier.clickable(onClick = onClick)
    )
}

@Composable
fun CheckboxListItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    isChecked: Boolean,
    icon: ImageVector,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = {
            AppTitle(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isChecked) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        },
        supportingContent = subtitle?.let {
            {
                AppBody(
                    text = it,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = if (isChecked) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        modifier = modifier.clickable { onCheckedChange(!isChecked) }
    )
}

@Composable
private fun ActiveFiltersSummary(
    selectedCategories: List<Category>,
    selectedTags: List<Tag>,
    isPinned: Boolean,
    isFavorites: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AppLabel(
            text = stringResource(id = R.string.active_filters),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (selectedCategories.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                AppBody(
                    text = selectedCategories.joinToString(", ") { it.name },
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
            }
        }

        if (selectedTags.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Label,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                AppBody(
                    text = selectedTags.joinToString(", ") { "#${it.name}" },
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
            }
        }

        if (isPinned) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                AppBody(
                    text = stringResource(R.string.note_pinned),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (isFavorites) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                AppBody(
                    text = stringResource(R.string.added_to_favorites),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// Previews - Preview the content directly for reliable rendering
@Preview(showBackground = true, name = "Filter List • No Filters")
@Composable
private fun FilterListContentNoFiltersPreview() {
    MemCloudApplicationTheme {
        FilterListContent(
            selectedCategories = emptyList(),
            selectedTags = emptyList(),
            isPinnedChecked = false,
            isFavoritedChecked = false,
            onFilterTypeClick = {},
            onPinnedToggle = {},
            onFavoritesToggle = {}
        )
    }
}

@Preview(showBackground = true, name = "Filter List • With Categories")
@Composable
private fun FilterListContentWithCategoriesPreview() {
    MemCloudApplicationTheme {
        FilterListContent(
            selectedCategories = listOf(
                Category(id = 1, userId = 1, name = "Work", color = "#FF6B6B", description = null, notesCount = 5),
                Category(id = 2, userId = 1, name = "Personal", color = "#4ECDC4", description = null, notesCount = 3)
            ),
            selectedTags = emptyList(),
            isPinnedChecked = false,
            isFavoritedChecked = false,
            onFilterTypeClick = {},
            onPinnedToggle = {},
            onFavoritesToggle = {}
        )
    }
}

@Preview(showBackground = true, name = "Filter List • With Tags and Pinned")
@Composable
private fun FilterListContentWithTagsPreview() {
    MemCloudApplicationTheme {
        FilterListContent(
            selectedCategories = emptyList(),
            selectedTags = listOf(
                Tag(id = 1, userId = 1, name = "urgent", color = "#FF6B6B", usageCount = 3),
                Tag(id = 2, userId = 1, name = "meeting", color = "#4ECDC4", usageCount = 2)
            ),
            isPinnedChecked = true,
            isFavoritedChecked = false,
            onFilterTypeClick = {},
            onPinnedToggle = {},
            onFavoritesToggle = {}
        )
    }
}

@Preview(showBackground = true, name = "Filter List • All Filters")
@Composable
private fun FilterListContentAllFiltersPreview() {
    MemCloudApplicationTheme {
        FilterListContent(
            selectedCategories = listOf(
                Category(id = 1, userId = 1, name = "Work", color = "#FF6B6B", description = null, notesCount = 5)
            ),
            selectedTags = listOf(
                Tag(id = 1, userId = 1, name = "urgent", color = "#FF6B6B", usageCount = 3),
                Tag(id = 2, userId = 1, name = "meeting", color = "#4ECDC4", usageCount = 2),
                Tag(id = 3, userId = 1, name = "project", color = "#45B7D1", usageCount = 5)
            ),
            isPinnedChecked = true,
            isFavoritedChecked = true,
            onFilterTypeClick = {},
            onPinnedToggle = {},
            onFavoritesToggle = {}
        )
    }
}
