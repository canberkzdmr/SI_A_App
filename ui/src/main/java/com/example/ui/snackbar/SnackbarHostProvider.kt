package com.example.ui.snackbar

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Provides a Scaffold with a global SnackbarHost that listens to SnackbarManager.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnackbarHostProvider(
    content: @Composable (PaddingValues) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Collect snackbar messages from SnackbarManager
    LaunchedEffect(Unit) {
        SnackbarManager.messages.collect { message ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = message.text,
                    withDismissAction = true,
                    duration = message.duration
                )
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData ->
                    CustomSnackbar(snackbarData)
                }
            )
        }
    ) { paddingValues ->
        content(paddingValues)
    }
}

@Composable
private fun CustomSnackbar(snackbarData: SnackbarData) {
    val rawMessage = snackbarData.visuals.message

    val (icon, backgroundColor, textColor, message) = when {
        rawMessage.startsWith("[SUCCESS]") -> {
            Quad(Icons.Default.CheckCircle, Color(0xFF2E7D32), Color.White, rawMessage.removePrefix("[SUCCESS]"))
        }
        rawMessage.startsWith("[ERROR]") -> {
            Quad(Icons.Default.Error, Color(0xFFC62828), Color.White, rawMessage.removePrefix("[ERROR]"))
        }
        rawMessage.startsWith("[INFO]") -> {
            Quad(Icons.Default.Info, MaterialTheme.colorScheme.primary, Color.White, rawMessage.removePrefix("[INFO]"))
        }
        else -> {
            Quad(Icons.Default.Info, MaterialTheme.colorScheme.primary, Color.White, rawMessage)
        }
    }

    Snackbar(
        modifier = Modifier.padding(8.dp),
        containerColor = backgroundColor,
        contentColor = textColor
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor
            )
            Text(text = message, color = textColor)
        }
    }
}

/**
 * Helper to return multiple values from when blocks.
 */
private data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)