package com.cbo.ui.components

import android.health.connect.datatypes.units.Length
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cbo.ui.theme.MemCloudApplicationTheme

@Composable
fun AppOutlinedTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    isValid: Boolean = true,
    validationErrorMessage: String = "",
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    minLength: Int = 0,
    maxLength: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE,
    onClick: (() -> Unit)? = null,
) {
    val clickableModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .clickable { onClick() }
    } else {
        modifier.fillMaxWidth()
    }

    OutlinedTextField(
        value = value,
        onValueChange = {
            if (it.length <= maxLength) {
                onValueChange(it)
            }
        },
        modifier = clickableModifier,
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it) } },
        supportingText = {
            if (!isValid) {
                Text(
                    text = validationErrorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        isError = isError,
        readOnly = readOnly,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        shape = RoundedCornerShape(8.dp),
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary,
            ),
    )
}

@Composable
fun AppBasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    cursorBrush: androidx.compose.ui.graphics.Brush = SolidColor(MaterialTheme.colorScheme.primary),
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit = { innerTextField ->
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty() && placeholder != null) {
                Text(
                    text = placeholder,
                    style = textStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            innerTextField()
        }
    }
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = textStyle,
        cursorBrush = cursorBrush,
        singleLine = singleLine,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        decorationBox = decorationBox
    )
}


@Preview(showBackground = true, name = "Outlined Text Field Preview")
@Composable
private fun AppOutlinedTextFieldPreview() {
    var text by remember { mutableStateOf("test1") }

    MemCloudApplicationTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            AppOutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = "Username",
                placeholder = "Enter your username",
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppOutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = "Password",
                placeholder = "Enter your password",
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                isError = text.length < 6,
            )

            AppOutlinedTextField(
                value = text,
                onValueChange = { text = it },
                isValid = false,
                validationErrorMessage = "Test Validation Error",
                label = "Password",
                placeholder = "Enter your password",
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                isError = text.length < 6,
            )
        }
    }
}

@Preview(showBackground = true, name = "Basic Text Field Preview")
@Composable
private fun AppBasicTextFieldPreview() {
    var text by remember { mutableStateOf("") }

    MemCloudApplicationTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            AppBasicTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = "Enter note title...",
                textStyle = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppBasicTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = "Multi-line text...",
                singleLine = false,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
