package com.cbo.notes.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cbo.ui.components.AppBody
import com.cbo.ui.components.AppHeadline
import com.cbo.ui.components.PrimaryButton
import com.cbo.ui.theme.MemCloudApplicationTheme

@Composable
fun NotesEmptyState(
    hasNotes: Boolean,
    searchQuery: String,
    onCreateNote: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon
        Icon(
            imageVector = when {
                searchQuery.isNotEmpty() -> Icons.Default.SearchOff
                hasNotes -> Icons.Default.FilterList
                else -> Icons.AutoMirrored.Filled.NoteAdd
            },
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        AppHeadline(
            text = when {
                searchQuery.isNotEmpty() -> "No notes found"
                hasNotes -> "No notes match your filters"
                else -> "No notes yet"
            },
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        AppBody(
            text = when {
                searchQuery.isNotEmpty() -> "Try adjusting your search terms or create a new note with \"$searchQuery\""
                hasNotes -> "Try clearing your filters or search to see all notes"
                else -> "Start by creating your first note"
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Action button
        if (!hasNotes || searchQuery.isNotEmpty()) {
            PrimaryButton(
                text = if (searchQuery.isNotEmpty()) "Create \"$searchQuery\"" else "Create Note",
                onClick = onCreateNote,
                modifier = Modifier.fillMaxWidth(0.6f),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                }
            )
        } else {
            PrimaryButton(
                text = "Clear Filters",
                onClick = onClearFilters,
                modifier = Modifier.fillMaxWidth(0.6f),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.FilterListOff,
                        contentDescription = null
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true, name = "Empty Notes - No Notes Yet")
@Composable
fun PreviewNotesEmptyState_NoNotes() {
    MemCloudApplicationTheme {
        NotesEmptyState(
            hasNotes = false,
            searchQuery = "",
            onCreateNote = {},
            onClearFilters = {},
        )
    }
}

@Preview(showBackground = true, name = "Empty Notes - With Notes but Filtered Out")
@Composable
fun PreviewNotesEmptyState_WithNotesFiltered() {
    MaterialTheme {
        NotesEmptyState(
            hasNotes = true,
            searchQuery = "",
            onCreateNote = {},
            onClearFilters = {},
        )
    }
}

@Preview(showBackground = true, name = "Empty Notes - Search No Result")
@Composable
fun PreviewNotesEmptyState_SearchNoResult() {
    MaterialTheme {
        NotesEmptyState(
            hasNotes = true,
            searchQuery = "Meeting notes",
            onCreateNote = {},
            onClearFilters = {},
        )
    }
}