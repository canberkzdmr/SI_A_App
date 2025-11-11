package com.cbo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cbo.ui.theme.MemCloudApplicationTheme

@Composable
fun AppSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    subtitle: String? = null,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                stateDescription = if (checked) "On" else "Off"
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (label != null || subtitle != null) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                if (label != null) {
                    AppRegular(
                        text = label,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    AppLabel(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledCheckedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                disabledUncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
            )
        )
    }
}

data class AppSwitchOption(
    val id: String,
    val label: String,
    val subtitle: String? = null,
    val enabled: Boolean = true,
)

@Composable
fun AppSwitchGroup(
    options: List<AppSwitchOption>,
    selectedOptionId: String?,
    onSelectionChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    spacing: Dp = 12.dp,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        if (title != null) {
            SectionHeader(
                title = title,
            )
        }

        options.forEach { option ->
            AppSwitch(
                checked = option.id == selectedOptionId,
                onCheckedChange = { checked ->
                    if (checked) {
                        onSelectionChange(option.id)
                    } else if (selectedOptionId == option.id) {
                        onSelectionChange(null)
                    }
                },
                label = option.label,
                subtitle = option.subtitle,
                enabled = option.enabled
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppSwitchPreview_Default() {
    MemCloudApplicationTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            AppSwitch(
                checked = true,
                onCheckedChange = {},
                label = "Notifications",
                subtitle = "Receive push notifications",
            )
            Spacer(modifier = Modifier.height(16.dp))
            AppSwitch(
                checked = false,
                onCheckedChange = {},
                label = "Dark Mode"
            )
            Spacer(modifier = Modifier.height(16.dp))
            AppSwitch(
                checked = false,
                onCheckedChange = {},
                enabled = false,
                label = "Location Access",
                subtitle = "Disabled by admin"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppSwitchGroupPreview() {
    MemCloudApplicationTheme {
        AppSwitchGroup(
            options = listOf(
                AppSwitchOption(
                    id = "auto",
                    label = "Automatic",
                    subtitle = "Let the app decide the best option"
                ),
                AppSwitchOption(
                    id = "manual",
                    label = "Manual",
                    subtitle = "You manage preferences yourself"
                ),
                AppSwitchOption(
                    id = "disabled",
                    label = "Disabled",
                    subtitle = "Turn off this feature",
                    enabled = false
                ),
            ),
            selectedOptionId = "manual",
            onSelectionChange = {},
            title = "Settings"
        )
    }
}
