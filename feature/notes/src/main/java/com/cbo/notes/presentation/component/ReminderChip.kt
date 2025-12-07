package com.cbo.notes.presentation.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cbo.notes.R
import com.cbo.ui.theme.MemCloudApplicationTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A chip that displays the reminder status of a note.
 * Shows the reminder time if set, or can be used as a button to set a reminder.
 */
@Composable
fun ReminderChip(
    reminderTime: Long?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val hasReminder = reminderTime != null
    val isExpired = reminderTime != null && reminderTime <= System.currentTimeMillis()
    
    val backgroundColor = when {
        isExpired -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
        hasReminder -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    
    val contentColor = when {
        isExpired -> MaterialTheme.colorScheme.onErrorContainer
        hasReminder -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val formattedTime = remember(reminderTime) {
        reminderTime?.let {
            val formatter = if (compact) {
                SimpleDateFormat("MMM d", Locale.getDefault())
            } else {
                SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
            }
            formatter.format(Date(it))
        }
    }
    
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (hasReminder && !isExpired) 
                Icons.Default.NotificationsActive 
            else 
                Icons.Outlined.NotificationsOff,
            contentDescription = stringResource(R.string.reminder),
            modifier = Modifier.size(if (compact) 14.dp else 16.dp),
            tint = contentColor
        )
        
        if (!compact || hasReminder) {
            Text(
                text = formattedTime ?: stringResource(R.string.set_reminder),
                style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * A small indicator icon for showing that a note has a reminder.
 * Suitable for compact note cards.
 */
@Composable
fun ReminderIndicator(
    hasReminder: Boolean,
    modifier: Modifier = Modifier
) {
    if (hasReminder) {
        Icon(
            imageVector = Icons.Default.NotificationsActive,
            contentDescription = stringResource(R.string.has_reminder),
            modifier = modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReminderChipWithReminderPreview() {
    MemCloudApplicationTheme {
        ReminderChip(
            reminderTime = System.currentTimeMillis() + 86400000, // Tomorrow
            onClick = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun ReminderChipNoReminderPreview() {
    MemCloudApplicationTheme {
        ReminderChip(
            reminderTime = null,
            onClick = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReminderChipExpiredPreview() {
    MemCloudApplicationTheme {
        ReminderChip(
            reminderTime = System.currentTimeMillis() - 3600000, // 1 hour ago
            onClick = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun ReminderChipCompactPreview() {
    MemCloudApplicationTheme {
        ReminderChip(
            reminderTime = System.currentTimeMillis() + 86400000,
            onClick = {},
            compact = true
        )
    }
}


