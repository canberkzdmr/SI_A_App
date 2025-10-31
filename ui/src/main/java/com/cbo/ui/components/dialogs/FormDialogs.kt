package com.cbo.ui.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cbo.ui.components.AppOutlinedTextField
import com.cbo.ui.components.AppTitle
import com.cbo.ui.components.PrimaryButton
import com.cbo.ui.components.SecondaryButton

/**
 * Form field data class
 */
data class FormField(
    val key: String,
    val label: String,
    val value: String,
    val placeholder: String? = null,
    val isRequired: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val onValueChange: (String) -> Unit
)

/**
 * Form data result
 */
data class FormData(
    val fields: Map<String, String>
)

/**
 * Form dialog for data entry
 */
@Composable
fun AppFormDialog(
    title: String,
    fields: List<FormField>,
    onConfirm: (FormData) -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "Save",
    cancelText: String = "Cancel",
    isLoading: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            AppTitle(text = title)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                fields.forEach { field ->
                    AppOutlinedTextField(
                        value = field.value,
                        onValueChange = field.onValueChange,
                        label = field.label,
                        placeholder = field.placeholder,
                        isError = field.isError,
                        validationErrorMessage = field.errorMessage ?: "",
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            PrimaryButton(
                text = confirmText,
                onClick = {
                    val formData = FormData(
                        fields = fields.associate { it.key to it.value }
                    )
                    onConfirm(formData)
                },
                isLoading = isLoading
            )
        },
        dismissButton = {
            SecondaryButton(
                text = cancelText,
                onClick = onDismiss
            )
        }
    )
}

/**
 * Simple form dialog with single field
 */
@Composable
fun AppSimpleFormDialog(
    title: String,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    placeholder: String? = null,
    confirmText: String = "Save",
    cancelText: String = "Cancel",
    isLoading: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            AppTitle(text = title)
        },
        text = {
            AppOutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = label,
                placeholder = placeholder,
                singleLine = true
            )
        },
        confirmButton = {
            PrimaryButton(
                text = confirmText,
                onClick = { onConfirm(value) },
                isLoading = isLoading
            )
        },
        dismissButton = {
            SecondaryButton(
                text = cancelText,
                onClick = onDismiss
            )
        }
    )
}


