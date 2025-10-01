package com.cbo.notes.presentation.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cbo.ui.components.AppLabelLarge
import com.cbo.ui.components.AppOutlinedTextField
import com.cbo.ui.components.ColorPicker
import com.cbo.ui.components.CustomAlertDialog

@Composable
internal fun CreateTagDialog(
    tagName: String,
    selectedColor: String?,
    isCreating: Boolean,
    onTagNameChange: (String) -> Unit,
    onColorChange: (String?) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    CustomAlertDialog(
        onDismiss = { onDismiss() },
        title = if (tagName.isBlank()) "Create New Tag" else "Edit Tag",
        content = {
            AppOutlinedTextField(
                value = tagName,
                onValueChange = { name ->
                    onTagNameChange(name.trim())
                },
                label = "Tag name",
                placeholder = "Enter tag name...",
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = tagName.isBlank(),
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
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
            TextButton(
                onClick = onConfirm,
                enabled = tagName.isNotBlank() && !isCreating,
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Create")
                }
            }
        },
    )
}