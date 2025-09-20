package com.cbo.ui.snackbar

import androidx.compose.material3.SnackbarDuration

sealed class SnackbarMessage(
    val text: String,
    val duration: SnackbarDuration,
    val type: SnackbarType
) {
    class Success(message: String) : SnackbarMessage(
        text = message,
        duration = SnackbarDuration.Short,
        type = SnackbarType.SUCCESS
    )

    class Error(message: String) : SnackbarMessage(
        text = message,
        duration = SnackbarDuration.Long,
        type = SnackbarType.ERROR
    )

    class Info(message: String) : SnackbarMessage(
        text = message,
        duration = SnackbarDuration.Short,
        type = SnackbarType.INFO
    )

    class Warning(message: String) : SnackbarMessage(
        text = message,
        duration = SnackbarDuration.Short,
        type = SnackbarType.WARNING
    )
}

enum class SnackbarType {
    SUCCESS, ERROR, INFO, WARNING
}