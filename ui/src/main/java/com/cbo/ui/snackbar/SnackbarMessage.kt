package com.cbo.ui.snackbar

import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarDuration

sealed class SnackbarMessage(
    val text: String? = null,
    @StringRes val textRes: Int? = null,
    val duration: SnackbarDuration,
    val type: SnackbarType
) {
    class Success(
        message: String? = null,
        @StringRes messageRes: Int? = null
    ) : SnackbarMessage(
        text = message,
        textRes = messageRes,
        duration = SnackbarDuration.Short,
        type = SnackbarType.SUCCESS
    )

    class Error(
        message: String? = null,
        @StringRes messageRes: Int? = null
    ) : SnackbarMessage(
        text = message,
        textRes = messageRes,
        duration = SnackbarDuration.Long,
        type = SnackbarType.ERROR
    )

    class Info(
        message: String? = null,
        @StringRes messageRes: Int? = null
    ) : SnackbarMessage(
        text = message,
        textRes = messageRes,
        duration = SnackbarDuration.Short,
        type = SnackbarType.INFO
    )

    class Warning(
        message: String? = null,
        @StringRes messageRes: Int? = null
    ) : SnackbarMessage(
        text = message,
        textRes = messageRes,
        duration = SnackbarDuration.Short,
        type = SnackbarType.WARNING
    )
}

enum class SnackbarType {
    SUCCESS, ERROR, INFO, WARNING
}