package com.cbo.notes.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cbo.notes.R
import com.cbo.notes.domain.model.Note

/**
 * Reusable dropdown menu for note card actions
 * 
 * @param note The note to display menu for
 * @param isExpanded Whether the menu is currently expanded
 * @param onExpandedChange Callback when menu expanded state changes
 * @param onTogglePin Callback when pin action is triggered
 * @param onToggleFavorite Callback when favorite action is triggered
 * @param onArchive Callback when archive action is triggered
 * @param onDelete Callback when delete action is triggered
 * @param modifier Optional modifier for the menu container
 */
@Composable
fun NoteOptionsMenu(
    note: Note,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        IconButton(
            onClick = { onExpandedChange(true) },
            modifier = Modifier.size(24.dp),
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(id = R.string.more_options_cd),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { onExpandedChange(false) },
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(id = if (note.isPinned) R.string.unpin else R.string.pin)
                    )
                },
                onClick = {
                    onTogglePin()
                    onExpandedChange(false)
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = null,
                        tint = if (note.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(
                            id = if (note.isFavorite) R.string.remove_from_favorites else R.string.add_to_favorites
                        )
                    )
                },
                onClick = {
                    onToggleFavorite()
                    onExpandedChange(false)
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (note.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (note.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.archive)) },
                onClick = {
                    onArchive()
                    onExpandedChange(false)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Archive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.delete)) },
                onClick = {
                    onDelete()
                    onExpandedChange(false)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                },
            )
        }
    }
}

