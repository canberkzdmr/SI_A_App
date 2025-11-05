package com.cbo.ui.components.filter

import android.content.res.Configuration
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cbo.ui.components.AppBody
import com.cbo.ui.components.AppOutlinedTextField
import com.cbo.ui.components.AppTitle
import com.cbo.ui.components.cards.AppCard
import com.cbo.ui.components.cards.CardVariant
import com.cbo.ui.theme.MemCloudApplicationTheme

enum class SelectionMode { Single, Multiple }

// Controlled + fills remaining height when expanded
@Composable
fun <T> ExpandableFilterSection(
    items: List<T>,
    selectedItems: List<T>,
    onSelectionChange: (List<T>) -> Unit,
    itemLabel: (T) -> String,
    label: String,
    modifier: Modifier = Modifier,
    selectionMode: SelectionMode = SelectionMode.Multiple,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    fillAvailableWhenExpanded: Boolean = false
) {
    Log.d("ExpandableFilterSelection", "selectedItems: ${selectedItems}")
    var searchQuery by remember { mutableStateOf("") }

    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) items
        else items.filter { itemLabel(it).contains(searchQuery, ignoreCase = true) }
    }

    AppCard(
        modifier = modifier.fillMaxWidth(),
        variant = CardVariant.ELEVATED
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onExpandedChange(!expanded)
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    AppTitle(text = label, style = MaterialTheme.typography.titleSmall)
                    val summary =
                        if (selectedItems.isEmpty()) "None"
                        else if (selectionMode == SelectionMode.Single) itemLabel(selectedItems.first())
                        else selectedItems.joinToString(limit = 3, transform = itemLabel).let {
                            if (selectedItems.size > 3) "$it, +${selectedItems.size - 3}" else it
                        }
                    AppBody(text = summary, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                // Content area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (fillAvailableWhenExpanded) Modifier.weight(1f, fill = true) else Modifier)
                        .padding(vertical = 8.dp)
                ) {
                    AppOutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = "Search...",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.size(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.size(8.dp))

                    // List: either fills all remaining or capped height
                    val listModifier =
                        if (fillAvailableWhenExpanded)
                            Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = true)
                        else
                            Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)

                    LazyColumn(
                        modifier = listModifier,
                        contentPadding = PaddingValues(bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredItems) { item ->
                            val isSelected = item in selectedItems
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val updated = if (selectionMode == SelectionMode.Multiple) {
                                            if (isSelected) selectedItems - item else selectedItems + item
                                        } else {
                                            if (isSelected) emptyList()
                                            else listOf(item)
                                        }
                                        onSelectionChange(updated)
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (selectionMode == SelectionMode.Multiple) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = {
                                            val updated = if (isSelected) selectedItems - item else selectedItems + item
                                            onSelectionChange(updated)
                                        }
                                    )
                                } else {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            val updated = if (isSelected) emptyList() else listOf(item)
                                            onSelectionChange(updated)
                                        }
                                    )
                                }
                                Spacer(Modifier.size(8.dp))
                                AppBody(itemLabel(item))
                            }
                        }
                    }
                }
            }
        }
    }
}

/*@Composable
fun <T> ExpandableFilterSection(
    items: List<T>,
    selectedItems: List<T>,
    onSelectionChange: (List<T>) -> Unit,
    itemLabel: (T) -> String,
    label: String,
    modifier: Modifier = Modifier,
    selectionMode: SelectionMode = SelectionMode.Multiple,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) items
        else items.filter { itemLabel(it).contains(searchQuery, ignoreCase = true) }
    }

    AppCard(
        modifier = modifier.fillMaxWidth(),
        variant = CardVariant.ELEVATED
    ) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onExpandedChange(!expanded)
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                AppTitle(
                    text = label,
                    style = MaterialTheme.typography.titleSmall
                )
                val summary =
                    if (selectedItems.isEmpty()) "None"
                    else if (selectionMode == SelectionMode.Single) itemLabel(selectedItems.first())
                    else selectedItems.joinToString(limit = 3) { itemLabel(it) }
                        .let {
                            if (selectedItems.size > 3) "$it, +${selectedItems.size - 3}" else it
                        }
                AppBody(
                    text = summary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand"
            )

        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.padding()) {
                AppOutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Search...",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.size(8.dp))
                HorizontalDivider()
                Spacer(Modifier.size(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    contentPadding = PaddingValues(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredItems) { item ->
                        val isSelected = item in selectedItems
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding()
                                .clickable {
                                    val updated = if (selectionMode == SelectionMode.Multiple) {
                                            if (isSelected) selectedItems - item else selectedItems + item
                                    } else {
                                            if (isSelected) emptyList()
                                            else listOf(item)
                                    }
                                    onSelectionChange(updated)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectionMode == SelectionMode.Multiple) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = {
                                        val updated =
                                            if (isSelected) selectedItems - item else selectedItems + item
                                        onSelectionChange(updated)
                                    }
                                )
                            } else {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        val updated =
                                            if (isSelected) emptyList()  // tap again to clear selection
                                            else listOf(item)
                                        onSelectionChange(updated)
                                    }
                                )
                            }
                            Spacer(Modifier.size(8.dp))
                            AppBody(itemLabel(item))
                        }
                    }
                }

                Spacer(Modifier.size(4.dp))
            }
        }
    }
}*/

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark")
@Composable
private fun ExpandableFilterSectionMultiPreview() {
    MemCloudApplicationTheme {
        val items = remember { demoItems() }
        val selected: SnapshotStateList<String> = remember { mutableStateListOf("Work", "Project") }

        ExpandableFilterSection(
            items = items,
            selectedItems = selected.toList(),
            onSelectionChange = { updated ->
                selected.clear()
                selected.addAll(updated)
            },
            itemLabel = { it },
            label = "Tags (Multiple)",
            selectionMode = SelectionMode.Multiple,
            modifier = Modifier.padding(16.dp),
            expanded = false,
            onExpandedChange = {}
        )
    }
}

@Preview(showBackground = true, name = "Single Select")
@Composable
private fun ExpandableFilterSectionSinglePreview() {
    MemCloudApplicationTheme {
        val items = remember { demoItems() }
        val selected: SnapshotStateList<String> = remember { mutableStateListOf("Personal") }

        Column(Modifier.padding(16.dp)) {
            ExpandableFilterSection(
                items = items,
                selectedItems = selected.toList(),
                onSelectionChange = { updated ->
                    selected.clear()
                    selected.addAll(updated)
                },
                itemLabel = { it },
                label = "Category (Single)",
                selectionMode = SelectionMode.Single,
                expanded = true,
                onExpandedChange = {}
            )
            ExpandableFilterSection(
                items = items,
                selectedItems = selected.toList(),
                onSelectionChange = { updated ->
                    selected.clear()
                    selected.addAll(updated)
                },
                itemLabel = { it },
                label = "Tags (Multiple)",
                selectionMode = SelectionMode.Multiple,
                modifier = Modifier.padding(vertical = 8.dp),
                expanded = false,
                onExpandedChange = {}
            )
        }
    }
}

private fun demoItems(): List<String> =
    listOf(
        "Work", "Personal", "Ideas", "Learning", "Shopping",
        "Project", "Meeting", "Research", "Urgent", "Later",
        "Android", "Compose", "Kotlin", "Backend", "Design"
    )