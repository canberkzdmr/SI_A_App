package com.cbo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.cbo.ui.R
import com.cbo.ui.theme.MemCloudApplicationTheme

@Composable
fun AppAlertDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: (() -> Unit)? = null,
    confirmText: String = stringResource(R.string.btn_ok),
    dismissText: String = stringResource(R.string.btn_cancel),
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
fun AppInfoDialog(
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    onDismiss: () -> Unit,
    confirmText: String = stringResource(R.string.btn_ok),
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AppTitle(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppBody(
                    text = message,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                PrimaryButton(
                    onClick = onDismiss,
                    text = confirmText,
                )
            }
        }
    }
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

@Preview(showBackground = true)
@Composable
private fun InfoDialogPreview() {
    MemCloudApplicationTheme {
        var showDialog by remember { mutableStateOf(true) }

        if (showDialog) {
            AppInfoDialog(
                title = "Info",
                message = "\uD83D\uDCA1 Tip: Swipe left to delete, right to archive",
                onDismiss = { showDialog = false },
                confirmText = "Got it"
            )
        }
    }
}
