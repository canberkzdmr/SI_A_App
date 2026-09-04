package com.cbo.notes.presentation.component

import androidx.compose.foundation.clickable
import com.cbo.core.logger.AppLogger
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.cbo.notes.R
import com.cbo.notes.domain.model.Category
import com.cbo.notes.domain.model.Tag
import com.cbo.ui.components.AppOutlinedTextField
import com.cbo.ui.components.PrimaryButton
import com.cbo.ui.components.ScreenWithTopBarAndInsets
import com.cbo.ui.components.TertiaryButton
import com.cbo.ui.components.states.AppEmptyState
import com.cbo.ui.theme.MemCloudApplicationTheme

/**
 * Generic filter detail screen that handles both Category and Tags selection.
 * Both support multi-select with OR filtering logic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDetailScreen(
    filterType: FilterType,
    categories: List<Category>,
    tags: List<Tag>,
    selectedCategories: List<Category>,
    selectedTags: List<Tag>,
    onCategoryToggled: (Category) -> Unit,
    onTagToggled: (Tag) -> Unit,
    onApply: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = when (filterType) {
        FilterType.CATEGORY -> stringResource(id = R.string.select_categories)
        FilterType.TAGS -> stringResource(id = R.string.select_tags)
        FilterType.PINNED -> stringResource(id = R.string.note_pinned)
        FilterType.FAVORITE -> stringResource(id = R.string.added_to_favorites)
    }

    ScreenWithTopBarAndInsets(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
                actions = {
                    when (filterType) {
                        FilterType.CATEGORY -> {
                            if (selectedCategories.isNotEmpty()) {
                                TertiaryButton(
                                    text = stringResource(
                                        id = R.string.clear_count,
                                        selectedCategories.size
                                    ),
                                    onClick = {
                                        selectedCategories.forEach { onCategoryToggled(it) }
                                    }
                                )
                            }
                        }
                        FilterType.TAGS -> {
                            if (selectedTags.isNotEmpty()) {
                                TertiaryButton(
                                    text = stringResource(
                                        id = R.string.clear_count,
                                        selectedTags.size
                                    ),
                                    onClick = {
                                        selectedTags.forEach { onTagToggled(it) }
                                    }
                                )
                            }
                        }
                        else -> {
                            AppLogger.d("Favorite or pinned log")
                        }
                    }
                }
            )
        }
    ) {
        FilterDetailContent(
            filterType = filterType,
            categories = categories,
            tags = tags,
            selectedCategories = selectedCategories,
            selectedTags = selectedTags,
            onCategoryToggled = onCategoryToggled,
            onTagToggled = onTagToggled,
            onApply = onApply
        )
    }
}

@Composable
private fun FilterDetailContent(
    filterType: FilterType,
    categories: List<Category>,
    tags: List<Tag>,
    selectedCategories: List<Category>,
    selectedTags: List<Tag>,
    onCategoryToggled: (Category) -> Unit,
    onTagToggled: (Tag) -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search field
        AppOutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = when (filterType) {
                FilterType.CATEGORY -> stringResource(id = R.string.search_categories)
                FilterType.TAGS -> stringResource(id = R.string.search_tags)
                FilterType.PINNED -> "PINNED"
                FilterType.FAVORITE -> "FAVORITESS"
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(id = R.string.clear_search)
                        )
                    }
                }
            }
        )

        // Content based on filter type
        when (filterType) {
            FilterType.CATEGORY -> {
                CategoryFilterContent(
                    categories = categories,
                    selectedCategories = selectedCategories,
                    searchQuery = searchQuery,
                    onCategoryToggled = onCategoryToggled,
                    modifier = Modifier.weight(1f)
                )
            }
            FilterType.TAGS -> {
                TagsFilterContent(
                    tags = tags,
                    selectedTags = selectedTags,
                    searchQuery = searchQuery,
                    onTagToggled = onTagToggled,
                    modifier = Modifier.weight(1f)
                )
            }
            else -> {}
        }

        // Apply button
        PrimaryButton(
            text = stringResource(id = R.string.apply),
            onClick = onApply,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun CategoryFilterContent(
    categories: List<Category>,
    selectedCategories: List<Category>,
    searchQuery: String,
    onCategoryToggled: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredCategories = remember(categories, searchQuery) {
        if (searchQuery.isBlank()) {
            categories
        } else {
            categories.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    if (filteredCategories.isEmpty() && categories.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AppEmptyState(
                title = stringResource(id = R.string.no_categories_yet),
                message = stringResource(id = R.string.create_categories_to_filter)
            )
        }
    } else if (filteredCategories.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AppEmptyState(
                title = stringResource(id = R.string.no_matching_categories),
                message = stringResource(id = R.string.try_different_search)
            )
        }
    } else {
        LazyColumn(
            modifier = modifier
        ) {
            items(filteredCategories, key = { it.id }) { category ->
                val isSelected = selectedCategories.any { it.id == category.id }
                CategoryListItem(
                    category = category,
                    isSelected = isSelected,
                    onClick = { onCategoryToggled(category) }
                )
            }
        }
    }
}

@Composable
private fun CategoryListItem(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = {
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        },
        supportingContent = category.description?.let { desc ->
            {
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        },
        leadingContent = {
            val color = category.color
            if (color != null && color.isNotBlank()) {
                Box(
                    modifier = Modifier.padding(4.dp)
                ) {
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxWidth(0.05f)
                            .padding(4.dp)
                    ) {
                        drawCircle(
                            color = Color(color.toColorInt()),
                            radius = 12.dp.toPx()
                        )
                    }
                }
            }
        },
        trailingContent = {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        modifier = modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun TagsFilterContent(
    tags: List<Tag>,
    selectedTags: List<Tag>,
    searchQuery: String,
    onTagToggled: (Tag) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredTags = remember(tags, searchQuery) {
        if (searchQuery.isBlank()) {
            tags
        } else {
            tags.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    if (filteredTags.isEmpty() && tags.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AppEmptyState(
                title = stringResource(id = R.string.no_tags_yet),
                message = stringResource(id = R.string.create_tags_to_filter)
            )
        }
    } else if (filteredTags.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AppEmptyState(
                title = stringResource(id = R.string.no_matching_tags),
                message = stringResource(id = R.string.try_different_search)
            )
        }
    } else {
        LazyColumn(
            modifier = modifier
        ) {
            items(filteredTags, key = { it.id }) { tag ->
                val isSelected = selectedTags.any { it.id == tag.id }
                TagListItem(
                    tag = tag,
                    isSelected = isSelected,
                    onClick = { onTagToggled(tag) }
                )
            }
        }
    }
}

@Composable
private fun TagListItem(
    tag: Tag,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = {
            Text(
                text = "#${tag.name}",
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        },
        supportingContent = {
            Text(
                text = stringResource(id = R.string.used_count, tag.usageCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            val defaultColor = MaterialTheme.colorScheme.primary
            Box(
                modifier = Modifier.padding(4.dp)
            ) {
                androidx.compose.foundation.Canvas(
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth(0.05f)
                        .padding(4.dp)
                ) {
                    tag.color?.let {
                        drawCircle(
                            color = Color(it.toColorInt()),
                            radius = 12.dp.toPx()
                        )
                    } ?: run { 
                        drawCircle(
                            color = defaultColor,
                            radius = 12.dp.toPx()
                        )
                    }
                }
            }
        },
        trailingContent = {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        modifier = modifier.clickable(onClick = onClick)
    )
}

// Previews
@Preview(showBackground = true, name = "Filter Detail • Categories Multi-Select")
@Composable
private fun FilterDetailCategoriesPreview() {
    MemCloudApplicationTheme {
        FilterDetailContent(
            filterType = FilterType.CATEGORY,
            categories = listOf(
                Category(id = 1, userId = 1, name = "Work", color = "#FF6B6B", description = "Work related", notesCount = 5),
                Category(id = 2, userId = 1, name = "Personal", color = "#4ECDC4", description = null, notesCount = 3),
                Category(id = 3, userId = 1, name = "Ideas", color = "#45B7D1", description = null, notesCount = 2)
            ),
            tags = emptyList(),
            selectedCategories = listOf(
                Category(id = 1, userId = 1, name = "Work", color = "#FF6B6B", description = null, notesCount = 5),
                Category(id = 3, userId = 1, name = "Ideas", color = "#45B7D1", description = null, notesCount = 2)
            ),
            selectedTags = emptyList(),
            onCategoryToggled = {},
            onTagToggled = {},
            onApply = {}
        )
    }
}

@Preview(showBackground = true, name = "Filter Detail • Categories No Selection")
@Composable
private fun FilterDetailCategoriesNoSelectionPreview() {
    MemCloudApplicationTheme {
        FilterDetailContent(
            filterType = FilterType.CATEGORY,
            categories = listOf(
                Category(id = 1, userId = 1, name = "Work", color = "#FF6B6B", description = null, notesCount = 5),
                Category(id = 2, userId = 1, name = "Personal", color = "#4ECDC4", description = null, notesCount = 3)
            ),
            tags = emptyList(),
            selectedCategories = emptyList(),
            selectedTags = emptyList(),
            onCategoryToggled = {},
            onTagToggled = {},
            onApply = {}
        )
    }
}

@Preview(showBackground = true, name = "Filter Detail • Tags Multi-Select")
@Composable
private fun FilterDetailTagsPreview() {
    MemCloudApplicationTheme {
        FilterDetailContent(
            filterType = FilterType.TAGS,
            categories = emptyList(),
            tags = listOf(
                Tag(id = 1, userId = 1, name = "urgent", color = "#FF6B6B", usageCount = 3),
                Tag(id = 2, userId = 1, name = "meeting", usageCount = 2),
                Tag(id = 3, userId = 1, name = "project", color = "#45B7D1", usageCount = 5),
                Tag(id = 4, userId = 1, name = "idea", color = "#FFA07A", usageCount = 4)
            ),
            selectedCategories = emptyList(),
            selectedTags = listOf(
                Tag(id = 1, userId = 1, name = "urgent", color = "#FF6B6B", usageCount = 3),
                Tag(id = 3, userId = 1, name = "project", color = "#45B7D1", usageCount = 5)
            ),
            onCategoryToggled = {},
            onTagToggled = {},
            onApply = {}
        )
    }
}

@Preview(showBackground = true, name = "Filter Detail • Empty")
@Composable
private fun FilterDetailEmptyPreview() {
    MemCloudApplicationTheme {
        FilterDetailContent(
            filterType = FilterType.CATEGORY,
            categories = emptyList(),
            tags = emptyList(),
            selectedCategories = emptyList(),
            selectedTags = emptyList(),
            onCategoryToggled = {},
            onTagToggled = {},
            onApply = {}
        )
    }
}
