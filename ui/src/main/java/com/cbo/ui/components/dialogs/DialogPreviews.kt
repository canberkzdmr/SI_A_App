package com.cbo.ui.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cbo.ui.components.AppOutlinedTextField
import com.cbo.ui.theme.MemCloudApplicationTheme

@Preview(showBackground = true, name = "Form Dialogs")
@Composable
fun FormDialogsPreview() {
    MemCloudApplicationTheme {
        var showDialog by remember { mutableStateOf(false) }
        
        if (showDialog) {
            val fields = listOf(
                FormField(
                    key = "name",
                    label = "Name",
                    value = "",
                    placeholder = "Enter name",
                    isRequired = true,
                    onValueChange = { }
                ),
                FormField(
                    key = "email",
                    label = "Email",
                    value = "",
                    placeholder = "Enter email",
                    isRequired = true,
                    onValueChange = { }
                )
            )
            
            AppFormDialog(
                title = "Create Category",
                fields = fields,
                onConfirm = { showDialog = false },
                onDismiss = { showDialog = false }
            )
        }
    }
}

@Preview(showBackground = true, name = "Confirmation Dialogs")
@Composable
fun ConfirmationDialogsPreview() {
    MemCloudApplicationTheme {
        var showDialog by remember { mutableStateOf(false) }
        
        if (showDialog) {
            AppConfirmationDialog(
                type = DialogType.WARNING,
                title = "Unsaved Changes",
                message = "You have unsaved changes. Are you sure you want to leave?",
                onConfirm = { showDialog = false },
                onDismiss = { showDialog = false },
                confirmText = "Leave",
                dismissText = "Stay"
            )
        }
    }
}

@Preview(showBackground = true, name = "Delete Dialog")
@Composable
fun DeleteDialogPreview() {
    MemCloudApplicationTheme {
        var showDialog by remember { mutableStateOf(false) }
        
        if (showDialog) {
            AppDeleteDialog(
                title = "Delete Note",
                message = "This action cannot be undone.",
                itemName = "My Important Note",
                onDelete = { showDialog = false },
                onDismiss = { showDialog = false }
            )
        }
    }
}

@Preview(showBackground = true, name = "Success Dialog")
@Composable
fun SuccessDialogPreview() {
    MemCloudApplicationTheme {
        var showDialog by remember { mutableStateOf(false) }
        
        if (showDialog) {
            AppSuccessDialog(
                title = "Note Saved!",
                message = "Your note has been successfully saved to the cloud.",
                onDismiss = { showDialog = false }
            )
        }
    }
}

