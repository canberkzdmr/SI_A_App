package com.example.ui.snackbar

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Global SnackbarManager to send snack messages from anywhere in the app.
 */
object SnackbarManager {

    private val _messages = Channel<SnackbarMessage>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    suspend fun showMessage(message: SnackbarMessage) {
        _messages.send(message)
    }
}