package com.cbo.notes.presentation.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.cbo.notes.R
import com.cbo.ui.theme.MemCloudApplicationTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import com.cbo.notes.domain.model.ReminderPriority
import com.cbo.notes.domain.model.ReminderRepeat
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.items

/**
 * Dialog for setting or editing a note reminder.
 * Allows the user to pick a date and time for the reminder.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDialog(
    existingReminderTime: Long? = null,
    existingRepeat: ReminderRepeat = ReminderRepeat.NONE,
    existingPriority: ReminderPriority = ReminderPriority.DEFAULT,
    onConfirm: (Long, ReminderRepeat, ReminderPriority) -> Unit,
    onRemove: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val calendar = remember {
        Calendar.getInstance().apply {
            existingReminderTime?.let { timeInMillis = it }
            // If no existing reminder, set to next hour
            if (existingReminderTime == null) {
                add(Calendar.HOUR_OF_DAY, 1)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        }
    }
    
    var selectedDate by remember { mutableStateOf(calendar.timeInMillis) }
    var selectedHour by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(calendar.get(Calendar.MINUTE)) }
    
    var selectedRepeat by remember { mutableStateOf(existingRepeat) }
    var selectedPriority by remember { mutableStateOf(existingPriority) }
    
    val dateFormatter = remember { SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    
    fun getSelectedDateTime(): Long {
        return Calendar.getInstance().apply {
            timeInMillis = selectedDate
            set(Calendar.HOUR_OF_DAY, selectedHour)
            set(Calendar.MINUTE, selectedMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    
    fun updateDateTime(timeMs: Long) {
        val cal = Calendar.getInstance().apply { timeInMillis = timeMs }
        selectedDate = cal.timeInMillis
        selectedHour = cal.get(Calendar.HOUR_OF_DAY)
        selectedMinute = cal.get(Calendar.MINUTE)
    }
    
    fun setSmartChipTime(type: String) {
        val cal = Calendar.getInstance()
        when (type) {
            "Evening" -> {
                cal.set(Calendar.HOUR_OF_DAY, 18)
                cal.set(Calendar.MINUTE, 0)
                if (cal.timeInMillis < System.currentTimeMillis()) {
                    cal.add(Calendar.DAY_OF_YEAR, 1) // Next evening
                }
            }
            "Morning" -> {
                cal.add(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 9)
                cal.set(Calendar.MINUTE, 0)
            }
            "Weekend" -> {
                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                val daysToSaturday = if (dayOfWeek == Calendar.SATURDAY) 7 else (Calendar.SATURDAY - dayOfWeek + 7) % 7
                cal.add(Calendar.DAY_OF_YEAR, daysToSaturday)
                cal.set(Calendar.HOUR_OF_DAY, 10)
                cal.set(Calendar.MINUTE, 0)
            }
        }
        updateDateTime(cal.timeInMillis)
    }
    
    fun isValidReminderTime(): Boolean {
        return getSelectedDateTime() > System.currentTimeMillis()
    }

    var repeatExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = if (existingReminderTime != null) 
                    stringResource(R.string.edit_reminder) 
                else 
                    stringResource(R.string.set_reminder)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Smart Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = false,
                            onClick = { setSmartChipTime("Evening") },
                            label = { Text(stringResource(R.string.reminder_chip_this_evening)) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = false,
                            onClick = { setSmartChipTime("Morning") },
                            label = { Text(stringResource(R.string.reminder_chip_tomorrow_morning)) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = false,
                            onClick = { setSmartChipTime("Weekend") },
                            label = { Text(stringResource(R.string.reminder_chip_weekend)) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Date selector
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = dateFormatter.format(Date(selectedDate)),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    
                    // Time selector
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = timeFormatter.format(
                                Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, selectedHour)
                                    set(Calendar.MINUTE, selectedMinute)
                                }.time
                            ),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                // Repeat Dropdown
                ExposedDropdownMenuBox(
                    expanded = repeatExpanded,
                    onExpandedChange = { repeatExpanded = !repeatExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedRepeat.toDisplayString(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.reminder_repeat)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = repeatExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = repeatExpanded,
                        onDismissRequest = { repeatExpanded = false }
                    ) {
                        ReminderRepeat.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.toDisplayString()) },
                                onClick = {
                                    selectedRepeat = option
                                    repeatExpanded = false
                                }
                            )
                        }
                    }
                }

                // Priority Dropdown
                ExposedDropdownMenuBox(
                    expanded = priorityExpanded,
                    onExpandedChange = { priorityExpanded = !priorityExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedPriority.toDisplayString(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.reminder_priority)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = priorityExpanded,
                        onDismissRequest = { priorityExpanded = false }
                    ) {
                        ReminderPriority.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.toDisplayString()) },
                                onClick = {
                                    selectedPriority = option
                                    priorityExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Warning if time is in the past
                if (!isValidReminderTime()) {
                    Text(
                        text = stringResource(R.string.reminder_time_past),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(getSelectedDateTime(), selectedRepeat, selectedPriority) },
                enabled = isValidReminderTime()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            Row {
                if (existingReminderTime != null && onRemove != null) {
                    TextButton(onClick = onRemove) {
                        Text(
                            text = stringResource(R.string.remove_reminder),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    )
    
    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    // Allow today and future dates
                    val today = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    return utcTimeMillis >= today
                }
            }
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDate = it }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.apply))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // Time Picker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour,
            initialMinute = selectedMinute
        )
        
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = {
                selectedHour = timePickerState.hour
                selectedMinute = timePickerState.minute
                showTimePicker = false
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

/**
 * Custom dialog wrapper for TimePicker since Material3 doesn't provide a TimePickerDialog.
 */
@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.pick_time),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                )
                
                content()
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onConfirm) {
                        Text(stringResource(R.string.apply))
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderRepeat.toDisplayString(): String = when (this) {
    ReminderRepeat.NONE -> stringResource(R.string.reminder_repeat_none)
    ReminderRepeat.DAILY -> stringResource(R.string.reminder_repeat_daily)
    ReminderRepeat.WEEKLY -> stringResource(R.string.reminder_repeat_weekly)
    ReminderRepeat.MONTHLY -> stringResource(R.string.reminder_repeat_monthly)
    ReminderRepeat.YEARLY -> stringResource(R.string.reminder_repeat_yearly)
}

@Composable
private fun ReminderPriority.toDisplayString(): String = when (this) {
    ReminderPriority.LOW -> stringResource(R.string.reminder_priority_low)
    ReminderPriority.DEFAULT -> stringResource(R.string.reminder_priority_default)
    ReminderPriority.HIGH -> stringResource(R.string.reminder_priority_high)
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReminderDialogPreview() {
    MemCloudApplicationTheme {
        ReminderDialog(
            existingReminderTime = null,
            onConfirm = { _, _, _ -> },
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun ReminderDialogWithExistingPreview() {
    MemCloudApplicationTheme {
        ReminderDialog(
            existingReminderTime = System.currentTimeMillis() + 86400000, // Tomorrow
            onConfirm = { _, _, _ -> },
            onRemove = {},
            onDismiss = {}
        )
    }
}


