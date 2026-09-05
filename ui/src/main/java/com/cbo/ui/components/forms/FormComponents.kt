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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cbo.ui.R
import com.cbo.ui.components.AppBody
import com.cbo.ui.components.AppTitle
import com.cbo.ui.components.PrimaryButton
import com.cbo.ui.components.SecondaryButton

/**
 * Form field group for organizing related fields
 */
@Composable
fun AppFormFieldGroup(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppTitle(text = title)
            
            description?.let {
                AppBody(
                    text = it,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            content()
        }
    }
}

/**
 * Form section container
 */
@Composable
fun AppFormSection(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
    saveText: String = stringResource(R.string.btn_save),
    cancelText: String = stringResource(R.string.btn_cancel),
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




