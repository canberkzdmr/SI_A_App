package com.cbo.ui.snackbar

import androidx.compose.material3.SnackbarDuration

sealed class SnackbarMessage(
    val text: String,
    val duration: SnackbarDuration
) {
    class Success(message: String) : SnackbarMessage(
        text = "[SUCCESS]$message",
        duration = SnackbarDuration.Short
    )

    class Error(message: String) : SnackbarMessage(
        text = "[ERROR]$message",
        duration = SnackbarDuration.Long
    )

    class Info(message: String) : SnackbarMessage(
        text = "[INFO]$message",
        duration = SnackbarDuration.Short
    )
}