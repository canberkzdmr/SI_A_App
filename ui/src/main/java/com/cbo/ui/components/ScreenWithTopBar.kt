package com.cbo.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A composable that provides a top bar for individual screens.
 * This allows screens to have their own top bars without using Scaffold.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenWithTopBar(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Top bar
        topBar()
        
        // Content with proper padding
        Box(modifier = Modifier.fillMaxSize()) {
            content(PaddingValues())
        }
    }
}

/**
 * A composable that provides a top bar with proper edge-to-edge handling.
 */
@Composable
fun ScreenWithTopBarAndInsets(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Top bar with transparent background for edge-to-edge
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f) // Semi-transparent
        ) {
            topBar()
        }
        
        // Content with proper background and padding
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                content(PaddingValues())
                
                // Floating action button positioned at bottom right
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    floatingActionButton()
                }
            }
        }
    }
}
