package com.cbo.ui.components.filter

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.ui.res.stringResource
import com.cbo.ui.R
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.cbo.ui.theme.MemCloudApplicationTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> FilterDropDown(
    items: List<T>,
    selectedItems: List<T>,
    onSelectionChange: (List<T>) -> Unit,
    itemLabel: (T) -> String,
    label: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) items
        else items.filter { itemLabel(it).contains(searchQuery, ignoreCase = true) }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        AppOutlinedTextField(
            value = selectedItems.joinToString(", ") { itemLabel(it) },
            onValueChange = {},
            label = label,
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .heightIn(max = 300.dp)
        ) {
            AppOutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = stringResource(id = R.string.search_placeholder),
                singleLine = true,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            )

            LazyColumn {
                items(filteredItems) { item ->
                    val isSelected = item in selectedItems
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = null,
                                )
                                Spacer(Modifier.width(8.dp))
                                AppBody(itemLabel(item))
                            }
                        },
                        onClick = {
                            val updated = if (isSelected)
                                selectedItems - item
                            else
                                selectedItems + item
                            onSelectionChange(updated)
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark")
@Composable
fun FilterDropDownStringPreview() {
    MemCloudApplicationTheme {
        val items = remember {
            listOf(
                "Work", "Personal", "Ideas", "Learning", "Shopping",
                "Project", "Meeting", "Research", "Urgent", "Later",
                "Android", "Compose", "Kotlin", "Backend", "Design"
            )
        }
        val selected: SnapshotStateList<String> = remember { mutableStateListOf("Work", "Project") }

        FilterDropDown(
            items = items,
            selectedItems = selected.toList(),
            onSelectionChange = { updated ->
                selected.clear()
                selected.addAll(updated)
            },
            itemLabel = { it },
            label = "Select tags"
        )
    }
}