package com.cbo.notes.presentation.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cbo.core.domain.FieldValidationRules
import com.cbo.ui.components.AppLabelLarge
import com.cbo.ui.components.AppOutlinedTextField
import com.cbo.ui.components.ColorPicker
import com.cbo.ui.components.CustomAlertDialog
import com.cbo.ui.components.TertiaryButton

@Composable
internal fun CreateTagDialog(
    tagName: String,
    selectedColor: String?,
    isCreating: Boolean,
    onTagNameChange: (String) -> Unit,
    onColorChange: (String?) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isEdit: Boolean,
) {
    val focusRequester = remember { FocusRequester() }

    CustomAlertDialog(
        onDismiss = { onDismiss() },
        title = if (isEdit) "Edit Tag" else "Create New Tag",
        content = {
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }

            AppOutlinedTextField(
                value = tagName,
                onValueChange = { newValue ->
                    if (newValue.endsWith(" ") && newValue.trim().isNotBlank()) {
                        val tagName = newValue.trim()
                        if (tagName.isNotBlank()) {
                            onTagNameChange(tagName)
                        }
                    } else {
                        onTagNameChange(newValue.trim())
                    }
                },
                label = "Tag name (max ${FieldValidationRules.MAX_TAG_NAME_LENGTH} chars)",
                isValid = tagName.length <= FieldValidationRules.MAX_TAG_NAME_LENGTH,
                validationErrorMessage = "Tag name must be less than ${FieldValidationRules.MAX_TAG_NAME_LENGTH} characters",
                placeholder = "Enter tag name...",
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = true,
                isError = tagName.isBlank() || tagName.length > FieldValidationRules.MAX_TAG_NAME_LENGTH,
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppLabelLarge(
                text = "Color (optional)",
            )

            Spacer(modifier = Modifier.height(8.dp))

            ColorPicker(
                selectedColor = selectedColor,
                onColorChange = onColorChange
            )
        },
        buttons = {
            TertiaryButton(
                text = "Cancel",
                onClick = onDismiss
            )
            TertiaryButton(
                text = "Create",
                onClick = onConfirm,
                enabled = tagName.isNotBlank() && !isCreating && tagName.length <= FieldValidationRules.MAX_TAG_NAME_LENGTH,
                isInProgress = isCreating,
            )
        },
    )
}