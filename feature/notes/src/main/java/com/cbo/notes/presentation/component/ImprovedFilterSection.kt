package com.cbo.notes.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cbo.notes.domain.model.Category
import com.cbo.notes.domain.model.Tag
import com.cbo.ui.components.AppLabel
import com.cbo.ui.components.TertiaryButton
import com.cbo.ui.components.SecondaryButton
import androidx.compose.ui.tooling.preview.Preview
import com.cbo.ui.theme.MemCloudApplicationTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImprovedFilterSection(
    categories: List<Category>,
    tags: List<Tag>,
    selectedCategory: Category?,
    selectedTags: List<Tag>,
    onCategorySelected: (Category?) -> Unit,
    onTagSelected: (Tag) -> Unit,
    onClearFilters: () -> Unit,
    onManageFiltersClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (categories.isNotEmpty() || tags.isNotEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header with Clear / Manage buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppLabel(text = "Filters", modifier = Modifier.padding(vertical = 8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (selectedCategory != null || selectedTags.isNotEmpty()) {
                            TertiaryButton(
                                "Clear All",
                                onClick = onClearFilters,
                            )
                        }
                        SecondaryButton(
                            text = "Manage filters",
                            onClick = onManageFiltersClick,
                            leadingIcon = { Icon(imageVector = Icons.Default.FilterList, contentDescription = null) }
                        )
                    }
                }

                // Selected filters summary chips
                val hasSelection = selectedCategory != null || selectedTags.isNotEmpty()
                if (hasSelection) {
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        selectedCategory?.let { category ->
                            FilterChip(
                                selected = true,
                                onClick = { onCategorySelected(null) },
                                label = category.name,
                                color = category.color,
                                trailingIcon = {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = null)
                                }
                            )
                        }

                        selectedTags.forEach { tag ->
                            FilterChip(
                                selected = true,
                                onClick = { onTagSelected(tag) },
                                label = "#${tag.name}",
                                color = tag.color,
                                trailingIcon = {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = null)
                                }
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No filters applied",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Removed inline lists; bottom sheet provides vertical, searchable selection

@Preview(showBackground = true, name = "Filters • Default")
@Composable
private fun ImprovedFilterSection_Default_Preview() {
    MemCloudApplicationTheme {
        ImprovedFilterSection(
            categories = samplePreviewCategories(),
            tags = samplePreviewTags(),
            selectedCategory = null,
            selectedTags = emptyList(),
            onCategorySelected = {},
            onTagSelected = {},
            onClearFilters = {},
            onManageFiltersClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, name = "Filters • With Selection")
@Composable
private fun ImprovedFilterSection_Selected_Preview() {
    MemCloudApplicationTheme {
        val categories = samplePreviewCategories()
        val tags = samplePreviewTags()
        ImprovedFilterSection(
            categories = categories,
            tags = tags,
            selectedCategory = categories.first(),
            selectedTags = tags.take(3),
            onCategorySelected = {},
            onTagSelected = {},
            onClearFilters = {},
            onManageFiltersClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, name = "Filters • Empty")
@Composable
private fun ImprovedFilterSection_Empty_Preview() {
    MemCloudApplicationTheme {
        ImprovedFilterSection(
            categories = emptyList(),
            tags = emptyList(),
            selectedCategory = null,
            selectedTags = emptyList(),
            onCategorySelected = {},
            onTagSelected = {},
            onClearFilters = {},
            onManageFiltersClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun samplePreviewCategories(): List<Category> = listOf(
    Category(id = 1, userId = 1, name = "Work", color = "#FF6B6B", description = null, notesCount = 5),
    Category(id = 2, userId = 1, name = "Personal", color = "#4ECDC4", description = null, notesCount = 3),
    Category(id = 3, userId = 1, name = "Ideas", color = "#45B7D1", description = null, notesCount = 2),
    Category(id = 4, userId = 1, name = "Learning", color = "#FFA07A", description = null, notesCount = 4),
)

private fun samplePreviewTags(): List<Tag> = listOf(
    Tag(id = 1, userId = 1, name = "urgent", color = "#FF6B6B", usageCount = 3),
    Tag(id = 2, userId = 1, name = "meeting", color = "#4ECDC4", usageCount = 2),
    Tag(id = 3, userId = 1, name = "project", color = "#45B7D1", usageCount = 5),
    Tag(id = 4, userId = 1, name = "idea", color = "#FFA07A", usageCount = 4),
    Tag(id = 5, userId = 1, name = "todo", color = "#DDA0DD", usageCount = 6),
    Tag(id = 6, userId = 1, name = "research", color = "#98D8C8", usageCount = 2),
)
