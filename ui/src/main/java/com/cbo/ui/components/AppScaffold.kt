package com.cbo.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Unified AppScaffold component that handles the main app structure.
 * This replaces individual Scaffolds in screens to avoid nesting issues.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        contentWindowInsets = WindowInsets(0.dp) // Handle insets manually for edge-to-edge
    ) { paddingValues ->
        content(paddingValues)
    }
}

/**
 * AppScaffold variant for screens that need system window insets handling
 * for edge-to-edge display support.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffoldWithInsets(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        contentWindowInsets = WindowInsets(0.dp) // Handle insets manually for edge-to-edge
    ) { paddingValues ->
        content(paddingValues)
    }
}
