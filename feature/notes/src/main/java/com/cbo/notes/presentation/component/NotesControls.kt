package com.cbo.notes.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.cbo.core.domain.model.ViewMode
import com.cbo.notes.presentation.SortOrder
import com.cbo.ui.components.AppOutlinedTextField
import androidx.compose.ui.tooling.preview.Preview
import com.cbo.ui.components.AppOutlinedCard
import com.cbo.ui.theme.MemCloudApplicationTheme
import com.cbo.notes.domain.model.Category
import com.cbo.notes.domain.model.Tag
import com.cbo.ui.components.AppLabel
import com.cbo.ui.components.TertiaryButton

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotesControls(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    viewMode: ViewMode,
    onViewModeChange: (ViewMode) -> Unit,
    currentSortOrder: SortOrder,
    onSortOrderSelected: (SortOrder) -> Unit,
    selectedCategory: Category?,
    selectedTags: List<Tag>,
    onCategorySelected: (Category?) -> Unit,
    onTagSelected: (Tag) -> Unit,
    onClearFilters: () -> Unit,
    onManageFiltersClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppOutlinedCard(
        onClick={},
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search moved to TopAppBar; keep vertical space minimal

            // View Mode and Sort Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // View Mode Toggle
                ViewModeSelector(
                    viewMode = viewMode,
                    onViewModeChange = onViewModeChange
                )

                // Sort Options
                SortSelector(
                    currentSortOrder = currentSortOrder,
                    onSortOrderSelected = onSortOrderSelected
                )
            }

            // Filters summary and actions
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppLabel(text = "Filters")

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (selectedCategory != null || selectedTags.isNotEmpty()) {
                            TertiaryButton("Clear All", onClick = onClearFilters)
                        }
                        OutlinedButton(onClick = onManageFiltersClick) {
                            Icon(imageVector = Icons.Default.FilterList, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Manage filters")
                        }
                    }
                }

                val hasSelection = selectedCategory != null || selectedTags.isNotEmpty()
                if (hasSelection) {
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

@Preview(showBackground = true, name = "Notes Controls • List")
@Composable
private fun NotesControls_List_Preview() {
    var query by remember { mutableStateOf("Meeting") }
    var view by remember { mutableStateOf(ViewMode.LIST) }
    var sort by remember { mutableStateOf(SortOrder.UPDATED_DESC) }
    val cats = samplePreviewCategories()
    val tags = samplePreviewTags()
    var selectedCat by remember { mutableStateOf<Category?>(cats.first()) }
    var selectedTags by remember { mutableStateOf(tags.take(2)) }
    MemCloudApplicationTheme {
        NotesControls(
            searchQuery = query,
            onSearchQueryChange = { query = it },
            onClearSearch = { query = "" },
            viewMode = view,
            onViewModeChange = { view = it },
            currentSortOrder = sort,
            onSortOrderSelected = { sort = it },
            selectedCategory = selectedCat,
            selectedTags = selectedTags,
            onCategorySelected = { selectedCat = it },
            onTagSelected = { tag ->
                selectedTags = selectedTags.toMutableList().also { list ->
                    if (list.any { it.id == tag.id }) list.removeAll { it.id == tag.id } else list.add(tag)
                }
            },
            onClearFilters = { selectedCat = null; selectedTags = emptyList() },
            onManageFiltersClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Notes Controls • Grid")
@Composable
private fun NotesControls_Grid_Preview() {
    var query by remember { mutableStateOf("") }
    var view by remember { mutableStateOf(ViewMode.GRID) }
    var sort by remember { mutableStateOf(SortOrder.TITLE_ASC) }
    val cats = samplePreviewCategories()
    val tags = samplePreviewTags()
    MemCloudApplicationTheme {
        NotesControls(
            searchQuery = query,
            onSearchQueryChange = { query = it },
            onClearSearch = { query = "" },
            viewMode = view,
            onViewModeChange = { view = it },
            currentSortOrder = sort,
            onSortOrderSelected = { sort = it },
            selectedCategory = null,
            selectedTags = emptyList(),
            onCategorySelected = {},
            onTagSelected = {},
            onClearFilters = {},
            onManageFiltersClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Notes Controls • Compact")
@Composable
private fun NotesControls_Compact_Preview() {
    var query by remember { mutableStateOf("ideas") }
    var view by remember { mutableStateOf(ViewMode.COMPACT) }
    var sort by remember { mutableStateOf(SortOrder.CREATED_DESC) }
    val cats = samplePreviewCategories()
    val tags = samplePreviewTags()
    MemCloudApplicationTheme {
        NotesControls(
            searchQuery = query,
            onSearchQueryChange = { query = it },
            onClearSearch = { query = "" },
            viewMode = view,
            onViewModeChange = { view = it },
            currentSortOrder = sort,
            onSortOrderSelected = { sort = it },
            selectedCategory = null,
            selectedTags = emptyList(),
            onCategorySelected = {},
            onTagSelected = {},
            onClearFilters = {},
            onManageFiltersClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

// Preview helpers
private fun samplePreviewCategories(): List<Category> = listOf(
    Category(id = 1, userId = 1, name = "Work", color = "#FF6B6B", description = null, notesCount = 5),
    Category(id = 2, userId = 1, name = "Personal", color = "#4ECDC4", description = null, notesCount = 3),
)

private fun samplePreviewTags(): List<Tag> = listOf(
    Tag(id = 1, userId = 1, name = "urgent", color = "#FF6B6B", usageCount = 3),
    Tag(id = 2, userId = 1, name = "meeting", color = "#4ECDC4", usageCount = 2),
    Tag(id = 3, userId = 1, name = "project", color = "#45B7D1", usageCount = 5),
)
@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppOutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = modifier,
        placeholder = "Search notes...",
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = onClearSearch) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true
    )
}

@Composable
private fun ViewModeSelector(
    viewMode: ViewMode,
    onViewModeChange: (ViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "View:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ViewModeButton(
                icon = Icons.Default.ViewStream,
                contentDescription = "List view",
                isSelected = viewMode == ViewMode.LIST,
                onClick = { onViewModeChange(ViewMode.LIST) }
            )

            ViewModeButton(
                icon = Icons.Default.GridView,
                contentDescription = "Grid view",
                isSelected = viewMode == ViewMode.GRID,
                onClick = { onViewModeChange(ViewMode.GRID) }
            )

            ViewModeButton(
                icon = Icons.AutoMirrored.Filled.ViewList,
                contentDescription = "Compact view",
                isSelected = viewMode == ViewMode.COMPACT,
                onClick = { onViewModeChange(ViewMode.COMPACT) }
            )
        }
    }
}

@Composable
private fun ViewModeButton(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(40.dp)
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.RadioButton
            ),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SortSelector(
    currentSortOrder: SortOrder,
    onSortOrderSelected: (SortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Sort,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = getSortOrderDisplayText(currentSortOrder),
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SortOption(
                text = "Recently Updated",
                icon = Icons.Default.Schedule,
                isSelected = currentSortOrder == SortOrder.UPDATED_DESC,
                onClick = {
                    onSortOrderSelected(SortOrder.UPDATED_DESC)
                    expanded = false
                }
            )

            SortOption(
                text = "Oldest Updated",
                icon = Icons.Default.Schedule,
                isSelected = currentSortOrder == SortOrder.UPDATED_ASC,
                onClick = {
                    onSortOrderSelected(SortOrder.UPDATED_ASC)
                    expanded = false
                }
            )

            SortOption(
                text = "Recently Created",
                icon = Icons.Default.Add,
                isSelected = currentSortOrder == SortOrder.CREATED_DESC,
                onClick = {
                    onSortOrderSelected(SortOrder.CREATED_DESC)
                    expanded = false
                }
            )

            SortOption(
                text = "Oldest Created",
                icon = Icons.Default.Add,
                isSelected = currentSortOrder == SortOrder.CREATED_ASC,
                onClick = {
                    onSortOrderSelected(SortOrder.CREATED_ASC)
                    expanded = false
                }
            )

            SortOption(
                text = "Title A-Z",
                icon = Icons.Default.SortByAlpha,
                isSelected = currentSortOrder == SortOrder.TITLE_ASC,
                onClick = {
                    onSortOrderSelected(SortOrder.TITLE_ASC)
                    expanded = false
                }
            )

            SortOption(
                text = "Title Z-A",
                icon = Icons.Default.SortByAlpha,
                isSelected = currentSortOrder == SortOrder.TITLE_DESC,
                onClick = {
                    onSortOrderSelected(SortOrder.TITLE_DESC)
                    expanded = false
                }
            )
        }
    }
}

@Composable
private fun SortOption(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenuItem(
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = text,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        },
        onClick = onClick,
        modifier = modifier,
        trailingIcon = if (isSelected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else null
    )
}

private fun getSortOrderDisplayText(sortOrder: SortOrder): String {
    return when (sortOrder) {
        SortOrder.UPDATED_DESC -> "Recent"
        SortOrder.UPDATED_ASC -> "Oldest"
        SortOrder.CREATED_DESC -> "New First"
        SortOrder.CREATED_ASC -> "Old First"
        SortOrder.TITLE_ASC -> "A-Z"
        SortOrder.TITLE_DESC -> "Z-A"
    }
}

