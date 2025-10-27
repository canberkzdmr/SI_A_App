package com.cbo.ui.snackbar

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Provides a global SnackbarHost that listens to SnackbarManager.
 * This component no longer uses Scaffold to avoid nesting issues.
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
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = "${message.type.name}:${message.text}",
                    withDismissAction = true,
                    duration = message.duration
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main content
        content(PaddingValues())
        
        // Snackbar host positioned at the bottom
        SnackbarHost(
            hostState = snackbarHostState,
            snackbar = { snackbarData ->
                CustomSnackbar(snackbarData)
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun CustomSnackbar(snackbarData: SnackbarData) {
    val rawMessage = snackbarData.visuals.message
    
    // Parse type and message from the format "TYPE:message"
    val (type, message) = if (rawMessage.contains(":")) {
        val parts = rawMessage.split(":", limit = 2)
        try {
            SnackbarType.valueOf(parts[0]) to parts[1]
        } catch (e: IllegalArgumentException) {
            SnackbarType.INFO to rawMessage
        }
    } else {
        SnackbarType.INFO to rawMessage
    }

    val (icon, backgroundColor, contentColor) = when (type) {
        SnackbarType.SUCCESS -> Triple(
            Icons.Default.CheckCircle, 
            Color(0xFF1B5E20), // Dark green
            Color.White
        )
        SnackbarType.ERROR -> Triple(
            Icons.Default.Error, 
            Color(0xFFB71C1C), // Dark red
            Color.White
        )
        SnackbarType.WARNING -> Triple(
            Icons.Default.Warning, 
            Color(0xFFE65100), // Dark orange
            Color.White
        )
        SnackbarType.INFO -> Triple(
            Icons.Default.Info, 
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.onPrimary
        )
    }
    
    // Animation for smooth appearance
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 300),
        label = "snackbar_scale"
    )

    Snackbar(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp),
        containerColor = backgroundColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(12.dp),
        dismissAction = {
            IconButton(
                onClick = { snackbarData.dismiss() },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = message,
                color = contentColor,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
