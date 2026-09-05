package com.cbo.notes.presentation.component

import androidx.compose.foundation.layout.Arrangement
import com.cbo.core.logger.AppLogger
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cbo.notes.R
import com.cbo.notes.domain.model.Category
import com.cbo.notes.domain.model.Tag
import com.cbo.ui.components.TertiaryButton
import com.cbo.ui.components.filter.ExpandableFilterSection
import com.cbo.ui.components.filter.SelectionMode
import com.cbo.ui.theme.MemCloudApplicationTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionBottomSheet(
    allCategories: List<Category>,
    allTags: List<Tag>,
    selectedCategory: Category?,
    selectedTags: List<Tag>,
    onApply: (Category?, List<Tag>) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier.fillMaxHeight(1f)
    ) {
        SelectionSheetBody(
            allCategories,
            allTags,
            selectedCategory,
            selectedTags,
            onApply,
            onClearAll,
            onDismiss,
        )
    }
}

@Composable
private fun SelectionSheetBody(
    allCategories: List<Category>,
    allTags: List<Tag>,
    selectedCategory: Category?,
    selectedTags: List<Tag>,
    onApply: (Category?, List<Tag>) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    var tempCategory by remember { mutableStateOf(selectedCategory) }
    var tempTags by remember { mutableStateOf(selectedTags) }
    var activePanel by remember { mutableStateOf(null as Panel?) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Content area fills available height above the actions
        Column(
            modifier = Modifier
                .weight(1f, fill = true)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category (single) — takes remaining space when expanded
            ExpandableFilterSection(
                items = allCategories,
                selectedItems = listOfNotNull(tempCategory),
                onSelectionChange = { updated -> tempCategory = updated.firstOrNull() },
                itemLabel = { it.name },
                label = stringResource(R.string.category),
                selectionMode = SelectionMode.Single,
                expanded = activePanel == Panel.Category,
                onExpandedChange = { expand -> activePanel = if (expand) Panel.Category else null },
                fillAvailableWhenExpanded = true,
                modifier = Modifier.then(if (activePanel == Panel.Category) Modifier.weight(1f, fill = true) else Modifier)
            )

            // Tags (multi) — takes remaining space when expanded
            ExpandableFilterSection(
                items = allTags,
                selectedItems = tempTags,
                onSelectionChange = { updated ->
                    tempTags = updated
                },
                itemLabel = { "#${it.name}" },
                label = stringResource(R.string.tags),
                selectionMode = SelectionMode.Multiple,
                expanded = activePanel == Panel.Tags,
                onExpandedChange = { expand -> activePanel = if (expand) Panel.Tags else null },
                fillAvailableWhenExpanded = true,
                modifier = Modifier.then(if (activePanel == Panel.Tags) Modifier.weight(1f, fill = true) else Modifier)
            )
        }

        // Fixed bottom action bar
        HorizontalDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TertiaryButton(
                text = stringResource(R.string.clear_all),
                onClick = {
                    tempCategory = null
                    tempTags = emptyList()
                    onClearAll()
                })

            Spacer(Modifier.weight(1f))

            TertiaryButton(text = stringResource(R.string.cancel), onClick = onDismiss)

            TertiaryButton(text = stringResource(R.string.apply), onClick = {
                AppLogger.d("Selected Category: $tempCategory\nSelected Tags: $tempTags")
                onApply(tempCategory, tempTags)
                onDismiss()
            })
        }
    }
}

private enum class Panel { Category, Tags }

@Preview(showBackground = true)
@Composable
private fun SelectionBottomSheet_Default_Preview() {
    MemCloudApplicationTheme {
        SelectionSheetBody(
            allCategories = samplePreviewCategories(),
            allTags = samplePreviewTags(),
            selectedCategory = null,
            selectedTags = listOf(),
            onApply = { _, _ -> },
            onClearAll = {},
            onDismiss = {}
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