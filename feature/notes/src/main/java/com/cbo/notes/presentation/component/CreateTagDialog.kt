package com.cbo.notes.presentation.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cbo.core.domain.FieldValidationRules
import com.cbo.notes.R
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
        title = if (isEdit) stringResource(R.string.edit_tag) else stringResource(R.string.create_new_tag),
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
                label = stringResource(R.string.tag_name_max_chars, FieldValidationRules.MAX_TAG_NAME_LENGTH),
                isValid = tagName.length <= FieldValidationRules.MAX_TAG_NAME_LENGTH,
                validationErrorMessage = stringResource(R.string.tag_name_validation_error, FieldValidationRules.MAX_TAG_NAME_LENGTH),
                placeholder = stringResource(R.string.enter_tag_name),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = true,
                isError = tagName.isBlank() || tagName.length > FieldValidationRules.MAX_TAG_NAME_LENGTH,
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppLabelLarge(
                text = stringResource(R.string.color_optional),
            )

            Spacer(modifier = Modifier.height(8.dp))

            ColorPicker(
                selectedColor = selectedColor,
                onColorChange = onColorChange
            )
        },
        buttons = {
            TertiaryButton(
                text = stringResource(R.string.cancel),
                onClick = onDismiss
            )
            TertiaryButton(
                text = if (isEdit) stringResource(R.string.update) else stringResource(R.string.create),
                onClick = onConfirm,
                enabled = tagName.isNotBlank() && !isCreating && tagName.length <= FieldValidationRules.MAX_TAG_NAME_LENGTH,
                isInProgress = isCreating,
            )
        },
    )
}