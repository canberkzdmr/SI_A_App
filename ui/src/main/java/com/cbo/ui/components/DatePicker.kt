package com.cbo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cbo.core.common.util.DatePattern
import com.cbo.core.common.util.DateUtil
import com.cbo.ui.theme.MemCloudApplicationTheme
import java.util.Locale

/**
 * Date picker field - Click to open date picker dialog
 * Based on official Android Material 3 DatePicker documentation
 * Uses Modifier.pointerInput as Modifier.clickable doesn't work for text fields
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    selectedDate: Long?,
    onDateSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Select Date",
    placeholder: String = "Choose a date",
    enabled: Boolean = true,
    error: String? = null,
    datePattern: String = DatePattern.SHORT_DATE,
    leadingIcon: ImageVector = Icons.Default.CalendarToday,
    showClearButton: Boolean = true,
    yearRange: IntRange = IntRange(1900, 2010),
) {
    var showModal by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedDate?.let { DateUtil.format(it, datePattern) } ?: "",
        onValueChange = { },
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(selectedDate) {
                awaitEachGesture {
                    // Modifier.clickable doesn't work for text fields, so we use Modifier.pointerInput
                    // in the Initial pass to observe events before the text field consumes them
                    // in the Main pass.
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    if (upEvent != null && enabled) {
                        showModal = true
                    }
                }
            },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null
            )
        },
        trailingIcon = if (showClearButton && selectedDate != null && enabled) {
            {
                IconButton(onClick = { onDateSelected(null) }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear date"
                    )
                }
            }
        } else null,
        supportingText = if (error != null) {
            { Text(error) }
        } else null,
        isError = error != null,
        readOnly = true,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors()
    )

    if (showModal) {
        AppDatePickerModal(
            onDateSelected = { date ->
                onDateSelected(date)
                showModal = false
            },
            onDismiss = { showModal = false },
            initialDate = selectedDate,
            yearRange = yearRange
        )
    }
}

/**
 * Modal date picker dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    initialDate: Long? = null,
    confirmButtonText: String = "OK",
    dismissButtonText: String = "Cancel",
    yearRange: IntRange = IntRange(1900, 2025),
    title: String = "Select Date",
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate,
        yearRange = IntRange(1900, 2010)
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
            }) {
                AppLabel(text = confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                AppLabel(text = dismissButtonText)
            }
        },
        modifier = modifier
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                AppTitleMedium(
                    text = title,
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                )
            },
        )
    }
}

/**
 * Date range picker field
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerField(
    startDate: Long?,
    endDate: Long?,
    onDateRangeSelected: (Long?, Long?) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Select Date Range",
    placeholder: String = "Choose dates",
    enabled: Boolean = true,
    error: String? = null,
    datePattern: String = DatePattern.SHORT_DATE,
    leadingIcon: ImageVector = Icons.Default.CalendarToday,
) {
    var showModal by remember { mutableStateOf(false) }

    val displayText = if (startDate != null && endDate != null) {
        "${DateUtil.format(startDate, datePattern)} - ${DateUtil.format(endDate, datePattern)}"
    } else if (startDate != null) {
        DateUtil.format(startDate, datePattern)
    } else {
        ""
    }

    OutlinedTextField(
        value = displayText,
        onValueChange = { },
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(startDate, endDate) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    if (upEvent != null && enabled) {
                        showModal = true
                    }
                }
            },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null
            )
        },
        trailingIcon = if (startDate != null && enabled) {
            {
                IconButton(onClick = { onDateRangeSelected(null, null) }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear dates"
                    )
                }
            }
        } else null,
        supportingText = if (error != null) {
            { Text(error) }
        } else null,
        isError = error != null,
        readOnly = true,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors()
    )

    if (showModal) {
        AppDateRangePickerModal(
            initialStartDate = startDate,
            initialEndDate = endDate,
            onDateRangeSelected = { start, end ->
                onDateRangeSelected(start, end)
                showModal = false
            },
            onDismiss = { showModal = false }
        )
    }
}

/**
 * Modal date range picker dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDateRangePickerModal(
    initialStartDate: Long?,
    initialEndDate: Long?,
    onDateRangeSelected: (Long?, Long?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmButtonText: String = "OK",
    dismissButtonText: String = "Cancel",
    title: String = "Select Date Range",
) {
    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStartDate,
        initialSelectedEndDateMillis = initialEndDate
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onDateRangeSelected(
                        dateRangePickerState.selectedStartDateMillis,
                        dateRangePickerState.selectedEndDateMillis
                    )
                }
            ) {
                AppLabel(text = confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                AppLabel(text = dismissButtonText)
            }
        },
        modifier = modifier
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            title = {
                AppTitleMedium(
                    text = title,
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                )
            },
            headline = {
                DateRangePickerDefaults.DateRangePickerHeadline(
                    selectedStartDateMillis = dateRangePickerState.selectedStartDateMillis,
                    selectedEndDateMillis = dateRangePickerState.selectedEndDateMillis,
                    displayMode = dateRangePickerState.displayMode,
                    dateFormatter = DatePickerDefaults.dateFormatter(),
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, bottom = 12.dp)
                )
            }
        )
    }
}

/**
 * Compact date picker button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactDatePickerButton(
    selectedDate: Long?,
    onDateSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Date",
    datePattern: String = DatePattern.SHORT_DATE,
) {
    var showModal by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { showModal = true },
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CalendarToday,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        AppLabelLarge(
            text = selectedDate?.let { DateUtil.format(it, datePattern) } ?: placeholder
        )
    }

    if (showModal) {
        AppDatePickerModal(
            initialDate = selectedDate,
            onDateSelected = { date ->
                onDateSelected(date)
                showModal = false
            },
            onDismiss = { showModal = false }
        )
    }
}

// Previews
@Preview(name = "Date Picker Field", showBackground = true, locale = "tr")
@Preview(name = "Date Picker Field", showBackground = true, locale = "en")
@Composable
private fun DatePickerFieldPreview() {
    MemCloudApplicationTheme {
        var selectedDate by remember { mutableStateOf<Long?>(System.currentTimeMillis()) }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppHeadline(text = "Date Picker Examples")

            DatePickerField(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                label = "Birth Date",
                placeholder = "Select your birth date"
            )

            DatePickerField(
                selectedDate = null,
                onDateSelected = { },
                label = "Empty State",
                placeholder = "Click anywhere to select"
            )

            DatePickerField(
                selectedDate = selectedDate,
                onDateSelected = { },
                label = "With Error",
                error = "Date is required"
            )

            DatePickerField(
                selectedDate = selectedDate,
                onDateSelected = { },
                label = "Disabled",
                enabled = false
            )

            CompactDatePickerButton(
                selectedDate = selectedDate,
                onDateSelected = { },
                placeholder = "Date of Birth",
                datePattern = DatePattern.READABLE,
                modifier = Modifier.fillMaxWidth()
                )
        }
    }
}

@Preview(name = "Date Range Picker", locale = "tr", showBackground = true)
@Composable
private fun DateRangePickerFieldPreview() {
    MemCloudApplicationTheme {
        var startDate by remember { mutableStateOf<Long?>(System.currentTimeMillis()) }
        var endDate by remember {
            mutableStateOf<Long?>(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L)
        }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppHeadline(text = "Date Range Picker")

            DateRangePickerField(
                startDate = startDate,
                endDate = endDate,
                onDateRangeSelected = { start, end ->
                    startDate = start
                    endDate = end
                },
                label = "Trip Dates",
                placeholder = "Select travel dates"
            )
        }
    }
}