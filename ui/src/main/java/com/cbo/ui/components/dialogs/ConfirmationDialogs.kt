package com.cbo.ui.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.cbo.ui.R
import com.cbo.ui.components.AppBody
import com.cbo.ui.components.AppTitle
import com.cbo.ui.components.DestructiveButton
import com.cbo.ui.components.PrimaryButton
import com.cbo.ui.components.SecondaryButton

/**
 * Dialog type enum for different confirmation styles
 */
enum class DialogType {
    INFO,       // Blue/info styling
    WARNING,    // Orange/warning styling  
    DANGER,     // Red/danger styling
    SUCCESS     // Green/success styling
}

/**
 * Confirmation dialog with different types
 */
@Composable
fun AppConfirmationDialog(
    type: DialogType = DialogType.INFO,
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = stringResource(id = R.string.btn_confirm),
    dismissText: String = stringResource(id = R.string.btn_cancel),
    showDismissButton: Boolean = true,
    isLoading: Boolean = false
) {
    val (icon, confirmButtonStyle) = when (type) {
        DialogType.INFO -> Icons.Default.Info to DialogButtonStyle.PRIMARY
        DialogType.WARNING -> Icons.Default.Warning to DialogButtonStyle.PRIMARY
        DialogType.DANGER -> Icons.Default.Error to DialogButtonStyle.DANGER
        DialogType.SUCCESS -> Icons.Default.CheckCircle to DialogButtonStyle.PRIMARY
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = when (type) {
                        DialogType.INFO -> MaterialTheme.colorScheme.primary
                        DialogType.WARNING -> MaterialTheme.colorScheme.tertiary
                        DialogType.DANGER -> MaterialTheme.colorScheme.error
                        DialogType.SUCCESS -> MaterialTheme.colorScheme.primary
                    }
                )
                AppTitle(text = title)
            }
        },
        text = {
            AppBody(text = message)
        },
        confirmButton = {
            when (confirmButtonStyle) {
                DialogButtonStyle.PRIMARY -> PrimaryButton(
                    text = confirmText,
                    onClick = onConfirm,
                    isLoading = isLoading
                )
                DialogButtonStyle.DANGER -> DestructiveButton(
                    text = confirmText,
                    onClick = onConfirm
                )
            }
        },
        dismissButton = if (showDismissButton) {
            {
                SecondaryButton(
                    text = dismissText,
                    onClick = onDismiss
                )
            }
        } else null
    )
}

/**
 * Delete confirmation dialog
 */
@Composable
fun AppDeleteDialog(
    title: String = "Delete Item",
    message: String,
    itemName: String? = null,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    deleteText: String = "Delete",
    cancelText: String = "Cancel",
    isLoading: Boolean = false
) {
    val fullMessage = if (itemName != null) {
        "$message\n\nItem: $itemName"
    } else {
        message
    }
    
    AppConfirmationDialog(
        type = DialogType.DANGER,
        title = title,
        message = fullMessage,
        onConfirm = onDelete,
        onDismiss = onDismiss,
        confirmText = deleteText,
        dismissText = cancelText,
        isLoading = isLoading
    )
}

/**
 * Success dialog
 */
@Composable
fun AppSuccessDialog(
    title: String = "Success!",
    message: String,
    onDismiss: () -> Unit,
    confirmText: String = "OK"
) {
    AppConfirmationDialog(
        type = DialogType.SUCCESS,
        title = title,
        message = message,
        onConfirm = onDismiss,
        onDismiss = onDismiss,
        confirmText = confirmText,
        showDismissButton = false
    )
}

/**
 * Warning dialog
 */
@Composable
fun AppWarningDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "Continue",
    dismissText: String = "Cancel"
) {
    AppConfirmationDialog(
        type = DialogType.WARNING,
        title = title,
        message = message,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        confirmText = confirmText,
        dismissText = dismissText
    )
}

/**
 * Info dialog
 */
@Composable
fun AppInfoDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    confirmText: String = "OK"
) {
    AppConfirmationDialog(
        type = DialogType.INFO,
        title = title,
        message = message,
        onConfirm = onDismiss,
        onDismiss = onDismiss,
        confirmText = confirmText,
        showDismissButton = false
    )
}

/**
 * Dialog button style enum
 */
private enum class DialogButtonStyle {
    PRIMARY, DANGER
}
