package com.cbo.notes.presentation.component

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cbo.notes.domain.model.Category
import com.cbo.notes.domain.model.Tag
import com.cbo.ui.components.AppOutlinedTextField
import com.cbo.ui.components.PrimaryButton
import com.cbo.ui.components.SecondaryButton
import com.cbo.ui.theme.MemCloudApplicationTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersBottomSheet(
    allCategories: List<Category>,
    allTags: List<Tag>,
    selectedCategory: Category?,
    selectedTags: List<Tag>,
    onUpdateSelectedTags: (List<Tag>) -> Unit,
    onApply: (Category?, List<Tag>) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier.fillMaxHeight(0.85f)
    ) {
        FiltersSheetBody(
            allCategories = allCategories,
            allTags = allTags,
            selectedCategory = selectedCategory,
            selectedTags = selectedTags,
            onUpdateSelectedTags = onUpdateSelectedTags,
            onApply = onApply,
            onClearAll = onClearAll,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun FiltersSheetBody(
    allCategories: List<Category>,
    allTags: List<Tag>,
    selectedCategory: Category?,
    selectedTags: List<Tag>,
    onApply: (Category?, List<Tag>) -> Unit,
    onUpdateSelectedTags: (List<Tag>) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit,
) {
    var categoryQuery by remember { mutableStateOf("") }
    var tagQuery by remember { mutableStateOf("") }

    var tempSelectedCategory by remember { mutableStateOf(selectedCategory) }
    var tempSelectedTags by remember { mutableStateOf(selectedTags.toMutableList()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = Icons.Default.FilterList, contentDescription = null)
            Text("Manage Filters", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = {
                // Reset local temp states so UI updates immediately
                tempSelectedCategory = null
                tempSelectedTags = mutableListOf()
                onClearAll()
            }) { Text("Clear all") }
        }

        // Category search + list
        Text(text = "Category", style = MaterialTheme.typography.labelLarge)
        AppOutlinedTextField(
            value = categoryQuery,
            onValueChange = { categoryQuery = it },
            placeholder = "Search categories…",
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        val filteredCategories = remember(allCategories, categoryQuery) {
            if (categoryQuery.isBlank()) allCategories else allCategories.filter { it.name.contains(categoryQuery, ignoreCase = true) }
        }

        LazyColumn(
            modifier = Modifier.weight(0.4f, fill = false)
        ) {
            item {
                ListItem(
                    headlineContent = { Text("None") },
                    trailingContent = {
                        if (tempSelectedCategory == null) Icon(Icons.Default.Check, contentDescription = null)
                    },
                    modifier = Modifier.clickable { tempSelectedCategory = null }
                )
            }
            items(filteredCategories) { category ->
                ListItem(
                    headlineContent = { Text(category.name) },
                    trailingContent = {
                        if (tempSelectedCategory?.id == category.id) Icon(Icons.Default.Check, contentDescription = null)
                    },
                    modifier = Modifier.clickable {
                        tempSelectedCategory = if (tempSelectedCategory?.id == category.id) null else category
                    }
                )
            }
        }

        // Tag search + list
        Text(text = "Tags", style = MaterialTheme.typography.labelLarge)
        AppOutlinedTextField(
            value = tagQuery,
            onValueChange = { tagQuery = it },
            placeholder = "Search tags…",
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        val filteredTags = remember(allTags, tagQuery) {
            if (tagQuery.isBlank()) allTags else allTags.filter { it.name.contains(tagQuery, ignoreCase = true) }
        }

        LazyColumn(
            modifier = Modifier.weight(0.6f, fill = false)
        ) {
            items(items = filteredTags, key = { it.id }) { tag ->
                val isSelected = tempSelectedTags.any { it.id == tag.id }
                Log.d("FiltersBottomSheet","isSelected (${tag.name}(${tag.id})) $isSelected")
                ListItem(
                    headlineContent = { Text("#${tag.name}") },
                    trailingContent = {
                        if (isSelected) Icon(Icons.Default.Check, contentDescription = null)
                    },
                    modifier = Modifier.clickable {
                        tempSelectedTags = if (isSelected) {
                            tempSelectedTags.filterNot { it.id == tag.id }.toMutableList()
                        } else {
                            (tempSelectedTags + tag).toMutableList()
                        }
                        onUpdateSelectedTags(tempSelectedTags)
                    }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SecondaryButton(
                text = "Cancel",
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                leadingIcon = { Icon(imageVector = Icons.Default.Close, contentDescription = null) }
            )
            PrimaryButton(
                text = "Apply",
                onClick = { onApply(tempSelectedCategory, tempSelectedTags) },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

// Previews
@Preview(showBackground = true, name = "Filters Bottom Sheet • Default")
@Composable
private fun FiltersBottomSheet_Default_Preview() {
    MemCloudApplicationTheme {
        // Render the body directly for preview so it appears expanded
        FiltersSheetBody(
            allCategories = samplePreviewCategories(),
            allTags = samplePreviewTags(),
            selectedCategory = null,
            selectedTags = emptyList(),
            onUpdateSelectedTags = {},
            onApply = { _, _ -> },
            onClearAll = { },
            onDismiss = { }
        )
    }
}

@Preview(showBackground = true, name = "Filters Bottom Sheet • With Selection")
@Composable
private fun FiltersBottomSheet_Selected_Preview() {
    MemCloudApplicationTheme {
        val cats = samplePreviewCategories()
        val tags = samplePreviewTags()
        FiltersSheetBody(
            allCategories = cats,
            allTags = tags,
            selectedCategory = cats.first(),
            selectedTags = tags.take(3),
            onUpdateSelectedTags = {},
            onApply = { _, _ -> },
            onClearAll = { },
            onDismiss = { }
        )
    }
}

private fun samplePreviewCategories(): List<Category> = listOf(
    Category(id = 1, userId = 1, name = "Work", color = "#FF6B6B", description = null, notesCount = 5),
    Category(id = 2, userId = 1, name = "Personal", color = "#4ECDC4", description = null, notesCount = 3),
    Category(id = 3, userId = 1, name = "Ideas", color = "#45B7D1", description = null, notesCount = 2),
)

private fun samplePreviewTags(): List<Tag> = listOf(
    Tag(id = 1, userId = 1, name = "urgent", color = "#FF6B6B", usageCount = 3),
    Tag(id = 2, userId = 1, name = "meeting", color = "#4ECDC4", usageCount = 2),
    Tag(id = 3, userId = 1, name = "project", color = "#45B7D1", usageCount = 5),
    Tag(id = 4, userId = 1, name = "idea", color = "#FFA07A", usageCount = 4),
)


