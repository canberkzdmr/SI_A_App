package com.cbo.ui.components.forms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cbo.ui.components.AppBody
import com.cbo.ui.components.AppTitle
import com.cbo.ui.components.PrimaryButton
import com.cbo.ui.components.SecondaryButton

/**
 * Form field group for organizing related fields
 */
@Composable
fun AppFormFieldGroup(
    title: String? = null,
    description: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (title != null) {
                AppTitle(text = title)
            }
            
            if (description != null) {
                AppBody(
                    text = description,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            content()
        }
    }
}

/**
 * Form section with title and optional description
 */
@Composable
fun AppFormSection(
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppTitle(text = title)
        
        if (description != null) {
            AppBody(
                text = description,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        content()
    }
}

/**
 * Form actions container
 */
@Composable
fun AppFormActions(
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    saveText: String = "Save",
    cancelText: String = "Cancel",
    isLoading: Boolean = false,
    showCancel: Boolean = true
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (showCancel) {
            SecondaryButton(
                text = cancelText,
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            )
        }
        
        PrimaryButton(
            text = saveText,
            onClick = onSave,
            isLoading = isLoading,
            modifier = if (showCancel) Modifier.weight(1f) else Modifier.fillMaxWidth()
        )
    }
}

/**
 * Form container with scroll support
 */
@Composable
fun AppFormContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        content()
    }
}

/**
 * Form validation result
 */
data class FormValidationResult(
    val isValid: Boolean,
    val errors: Map<String, String> = emptyMap()
)

/**
 * Form state management
 */
data class FormState(
    val isLoading: Boolean = false,
    val isValid: Boolean = true,
    val errors: Map<String, String> = emptyMap()
)


