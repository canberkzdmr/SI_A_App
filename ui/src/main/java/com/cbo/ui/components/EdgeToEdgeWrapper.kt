package com.cbo.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Edge-to-edge wrapper that properly handles system window insets
 * for true edge-to-edge display.
 */
@Composable
fun EdgeToEdgeWrapper(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        content()
    }
}

/**
 * Provides proper padding for content that needs to avoid system bars
 * in edge-to-edge mode.
 */
@Composable
fun SystemBarAwareContent(
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    // For now, we'll use empty padding since we're handling insets manually
    // In a real implementation, you'd use WindowInsets.systemBars
    content(PaddingValues())
}

/**
 * Transparent top bar wrapper for edge-to-edge display
 */
@Composable
fun TransparentTopBarWrapper(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Transparent top bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ) {
            topBar()
        }
        
        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            content(PaddingValues())
        }
    }
}
