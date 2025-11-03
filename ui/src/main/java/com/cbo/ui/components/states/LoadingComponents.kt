package com.cbo.ui.components.states

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cbo.ui.components.AppBody

/**
 * Loading screen component for full-screen loading states
 */
@Composable
fun AppLoadingScreen(
    modifier: Modifier = Modifier,
    message: String = "Loading...",
    showProgress: Boolean = true,
    progressSize: Int = 32
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (showProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(progressSize.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            AppBody(
                text = message,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Loading overlay for partial loading states
 */
@Composable
fun AppLoadingOverlay(
    modifier: Modifier = Modifier,
    message: String = "Loading...",
    showProgress: Boolean = true
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (showProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            AppBody(
                text = message,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * Loading button state
 */
@Composable
fun AppLoadingButton(
    modifier: Modifier = Modifier,
    size: Int = 20
) {
    CircularProgressIndicator(
        modifier = modifier.size(size.dp),
        color = MaterialTheme.colorScheme.onPrimary,
        strokeWidth = 2.dp
    )
}



