package com.cbo.notes.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cbo.notes.presentation.SortOrder
import com.cbo.ui.components.AppHeadline
import com.cbo.ui.theme.MemCloudApplicationTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortBottomSheet(
    currentSortOrder: SortOrder,
    onSortOrderSelected: (SortOrder) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .selectableGroup()
        ) {
            AppHeadline(
                text = "Sort by",
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SortOption(
                text = "Recently Updated",
                icon = Icons.Default.Schedule,
                isSelected = currentSortOrder == SortOrder.UPDATED_DESC,
                onClick = { 
                    onSortOrderSelected(SortOrder.UPDATED_DESC)
                }
            )

            SortOption(
                text = "Oldest Updated",
                icon = Icons.Default.Schedule,
                isSelected = currentSortOrder == SortOrder.UPDATED_ASC,
                onClick = { 
                    onSortOrderSelected(SortOrder.UPDATED_ASC)
                }
            )

            SortOption(
                text = "Recently Created",
                icon = Icons.Default.Add,
                isSelected = currentSortOrder == SortOrder.CREATED_DESC,
                onClick = { 
                    onSortOrderSelected(SortOrder.CREATED_DESC)
                }
            )

            SortOption(
                text = "Oldest Created",
                icon = Icons.Default.Add,
                isSelected = currentSortOrder == SortOrder.CREATED_ASC,
                onClick = { 
                    onSortOrderSelected(SortOrder.CREATED_ASC)
                }
            )

            SortOption(
                text = "Title A-Z",
                icon = Icons.Default.SortByAlpha,
                isSelected = currentSortOrder == SortOrder.TITLE_ASC,
                onClick = { 
                    onSortOrderSelected(SortOrder.TITLE_ASC)
                }
            )

            SortOption(
                text = "Title Z-A",
                icon = Icons.Default.SortByAlpha,
                isSelected = currentSortOrder == SortOrder.TITLE_DESC,
                onClick = { 
                    onSortOrderSelected(SortOrder.TITLE_DESC)
                }
            )

            // Add some bottom padding for the last item
            Spacer(modifier = Modifier.height(16.dp))
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null, // null because the Row handles the click
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.width(16.dp))

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

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    showBackground = true,
    heightDp = 480, // Give it a fixed height to see the sheet
    name = "Sort Bottom Sheet Preview"
)
@Composable
fun SortBottomSheetPreview() {
    // We use a mock state and theme for the preview
    MemCloudApplicationTheme {
        // Use a state that is visible by default for the preview
        val mockSortOrder = remember { mutableStateOf(SortOrder.CREATED_DESC) }

        // Note: For a reliable preview of a ModalBottomSheet, sometimes using
        // rememberStandardBottomSheetState(initialValue = SheetValue.Expanded) is needed.
        // However, for the ModalBottomSheet *content* to be visible in the preview pane
        // without the scrim/dialog context, we use the original composable, but it may
        // not appear correctly.

        // A direct preview of the ModalBottomSheet itself. This is often inconsistent.
        // If this doesn't show in the Preview pane, use the trick below.
        SortBottomSheet(
            currentSortOrder = mockSortOrder.value,
            onSortOrderSelected = { mockSortOrder.value = it },
            onDismiss = {}
        )
    }
}

// --- Preview Trick: Preview the content directly ---

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    showBackground = true,
    heightDp = 480,
    name = "Sort Bottom Sheet Content Only"
)
@Composable
fun SortBottomSheetContentPreview() {
    MemCloudApplicationTheme {
        val mockSortOrder = remember { mutableStateOf(SortOrder.CREATED_DESC) }

        // Manually reproduce the content column of SortBottomSheet
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .selectableGroup()
        ) {
            // NOTE: 'AppHeadline' and 'Icons.Default.*' need to be available in your project.
            // Replace 'AppHeadline' with Text if it's a custom component you don't want to define here.
            Text(
                text = "Sort by",
                style = MaterialTheme.typography.headlineSmall, // Guessing the style for AppHeadline
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // ... (All your SortOption calls from the original composable)
            SortOption(
                text = "Recently Updated",
                icon = Icons.Default.Schedule,
                isSelected = mockSortOrder.value == SortOrder.UPDATED_DESC,
                onClick = { mockSortOrder.value = SortOrder.UPDATED_DESC }
            )
            // ... (Include all other 5 SortOption calls similarly)

            // Add some bottom padding for the last item
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
