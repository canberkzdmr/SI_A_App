package com.cbo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AppDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: (() -> Unit)? = null,
    confirmText: String = "OK",
    dismissText: String = "Cancel",
    showDismissButton: Boolean = true,
) {
    AlertDialog(
        onDismissRequest = { onDismiss?.invoke() },
        title = { AppTitle(title) },
        text = { AppBody(message) },
        confirmButton = {
            TertiaryButton(confirmText, onClick = onConfirm)
        },
        dismissButton =
            if (showDismissButton) {
                {
                    DestructiveButton(dismissText, onClick = { onDismiss?.invoke() })
                }
            } else {
                null
            },
    )
}

@Composable
fun CustomAlertDialog(
    title: String? = null,
    onDismiss: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
    buttons: @Composable RowScope.() -> Unit,
) {
    AlertDialog(
        title = {
            title?.let {
                it
                AppTitle(title)
            }
        },
        onDismissRequest = { onDismiss?.invoke() },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                content = content,
            )
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                content = buttons,
            )
        },
        dismissButton = {}, // dismiss handled inside buttons if needed
    )
}
