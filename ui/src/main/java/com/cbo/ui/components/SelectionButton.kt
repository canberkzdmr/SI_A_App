package com.cbo.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cbo.ui.theme.MemCloudApplicationTheme

/**
 * Single selection button (like radio button group)
 * Use this when only one option can be selected at a time
 */
@Composable
fun <T> SingleSelectionButton(
    options: List<SelectionOption<T>>,
    selectedValue: T?,
    onSelectionChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    style: SelectionButtonStyle = SelectionButtonStyle.Filled,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = option.value == selectedValue
            SelectionButtonItem(
                text = option.label,
                isSelected = isSelected,
                onClick = { onSelectionChange(option.value) },
                enabled = option.enabled,
                icon = option.icon,
                style = style,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Multi selection button (like checkbox group)
 * Use this when multiple options can be selected
 */
@Composable
fun <T> MultiSelectionButton(
    options: List<SelectionOption<T>>,
    selectedValues: Set<T>,
    onSelectionChange: (Set<T>) -> Unit,
    modifier: Modifier = Modifier,
    style: SelectionButtonStyle = SelectionButtonStyle.Filled,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = selectedValues.contains(option.value)
            SelectionButtonItem(
                text = option.label,
                isSelected = isSelected,
                onClick = {
                    val newSelection = if (isSelected) {
                        selectedValues - option.value
                    } else {
                        selectedValues + option.value
                    }
                    onSelectionChange(newSelection)
                },
                enabled = option.enabled,
                icon = option.icon,
                style = style,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Single selection button item
 */
@Composable
private fun SelectionButtonItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    style: SelectionButtonStyle = SelectionButtonStyle.Filled,
) {
    when (style) {
        SelectionButtonStyle.Filled -> FilledSelectionButton(
            text = text,
            isSelected = isSelected,
            onClick = onClick,
            enabled = enabled,
            icon = icon,
            modifier = modifier
        )
        SelectionButtonStyle.Outlined -> OutlinedSelectionButton(
            text = text,
            isSelected = isSelected,
            onClick = onClick,
            enabled = enabled,
            icon = icon,
            modifier = modifier
        )
        SelectionButtonStyle.Tonal -> TonalSelectionButton(
            text = text,
            isSelected = isSelected,
            onClick = onClick,
            enabled = enabled,
            icon = icon,
            modifier = modifier
        )
    }
}

@Composable
private fun FilledSelectionButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        SelectionButtonContent(text = text, icon = icon)
    }
}

@Composable
private fun OutlinedSelectionButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                Color.Transparent,
            contentColor = if (isSelected)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onSurface,
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.outline
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        SelectionButtonContent(text = text, icon = icon)
    }
}

@Composable
private fun TonalSelectionButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surface,
            contentColor = if (isSelected)
                MaterialTheme.colorScheme.onSecondaryContainer
            else
                MaterialTheme.colorScheme.onSurface,
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        SelectionButtonContent(text = text, icon = icon)
    }
}

@Composable
private fun SelectionButtonContent(
    text: String,
    icon: ImageVector?,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Data class for selection options
 */
data class SelectionOption<T>(
    val value: T,
    val label: String,
    val icon: ImageVector? = null,
    val enabled: Boolean = true,
)

/**
 * Style variants for selection buttons
 */
enum class SelectionButtonStyle {
    Filled,
    Outlined,
    Tonal
}

// Preview composables
@Preview(name = "Selection Buttons", showBackground = true)
@Composable
private fun SelectionButtonPreview() {
    MemCloudApplicationTheme {
        var selectedGender by remember { mutableStateOf<String?>("male") }
        var selectedLanguages by remember { mutableStateOf<Set<String>>(setOf("en")) }
        var selectedSize by remember { mutableStateOf<String?>("medium") }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Gender selection example
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Gender (Filled Style)",
                    style = MaterialTheme.typography.titleSmall
                )
                SingleSelectionButton(
                    options = listOf(
                        SelectionOption("male", "Male"),
                        SelectionOption("female", "Female"),
                    ),
                    selectedValue = selectedGender,
                    onSelectionChange = { selectedGender = it },
                    style = SelectionButtonStyle.Filled
                )
            }

            // Size selection with outlined style
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Size (Outlined Style)",
                    style = MaterialTheme.typography.titleSmall
                )
                SingleSelectionButton(
                    options = listOf(
                        SelectionOption("small", "S"),
                        SelectionOption("medium", "M"),
                        SelectionOption("large", "L"),
                        SelectionOption("xlarge", "XL"),
                    ),
                    selectedValue = selectedSize,
                    onSelectionChange = { selectedSize = it },
                    style = SelectionButtonStyle.Outlined
                )
            }

            // Multi-selection example
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Languages (Multi-select, Tonal)",
                    style = MaterialTheme.typography.titleSmall
                )
                MultiSelectionButton(
                    options = listOf(
                        SelectionOption("en", "EN"),
                        SelectionOption("es", "ES"),
                        SelectionOption("fr", "FR"),
                    ),
                    selectedValues = selectedLanguages,
                    onSelectionChange = { selectedLanguages = it },
                    style = SelectionButtonStyle.Tonal
                )
            }

            // With icons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "With Icons",
                    style = MaterialTheme.typography.titleSmall
                )
                var selectedView by remember { mutableStateOf<String?>("list") }
                SingleSelectionButton(
                    options = listOf(
                        SelectionOption(
                            "list",
                            "List",
                            icon = Icons.AutoMirrored.Filled.List
                        ),
                        SelectionOption(
                            "grid",
                            "Grid",
                            icon = androidx.compose.material.icons.Icons.Default.GridView
                        ),
                    ),
                    selectedValue = selectedView,
                    onSelectionChange = { selectedView = it },
                    style = SelectionButtonStyle.Filled
                )
            }

            // Disabled state
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Disabled Option",
                    style = MaterialTheme.typography.titleSmall
                )
                var selectedOption by remember { mutableStateOf<String?>("option1") }
                SingleSelectionButton(
                    options = listOf(
                        SelectionOption("option1", "Available"),
                        SelectionOption("option2", "Disabled", enabled = false),
                        SelectionOption("option3", "Available"),
                    ),
                    selectedValue = selectedOption,
                    onSelectionChange = { selectedOption = it },
                    style = SelectionButtonStyle.Outlined
                )
            }
        }
    }
}