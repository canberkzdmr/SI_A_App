package com.cbo.notes.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun NotesEmptyState(
    hasNotes: Boolean,
    searchQuery: String,
    onCreateNote: () -> Unit,
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
                else -> Icons.Default.NoteAdd
            },
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = when {
                searchQuery.isNotEmpty() -> "No notes found"
                hasNotes -> "No notes match your filters"
                else -> "No notes yet"
            },
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = when {
                searchQuery.isNotEmpty() -> "Try adjusting your search terms or create a new note with \"$searchQuery\""
                hasNotes -> "Try clearing your filters or search to see all notes"
                else -> "Start by creating your first note"
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Action button
        if (!hasNotes || searchQuery.isNotEmpty()) {
            Button(
                onClick = onCreateNote,
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (searchQuery.isNotEmpty()) "Create \"$searchQuery\"" else "Create Note"
                )
            }
        }
    }
}
