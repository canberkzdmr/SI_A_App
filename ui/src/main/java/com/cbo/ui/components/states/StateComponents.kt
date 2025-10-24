package com.cbo.ui.components.states

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cbo.ui.components.AppBody
import com.cbo.ui.components.AppTitle
import com.cbo.ui.components.PrimaryButton
import com.cbo.ui.components.SecondaryButton

/**
 * Empty state component for when there's no data to display
 */
@Composable
fun AppEmptyState(
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Inbox,
    title: String = "No items found",
    message: String = "There are no items to display at the moment.",
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    secondaryActionText: String? = null,
    onSecondaryAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AppTitle(
            text = title,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        AppBody(
            text = message,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PrimaryButton(
                    text = actionText,
                    onClick = onAction
                )
                
                if (secondaryActionText != null && onSecondaryAction != null) {
                    SecondaryButton(
                        text = secondaryActionText,
                        onClick = onSecondaryAction
                    )
                }
            }
        }
    }
}

/**
 * Error state component for when something goes wrong
 */
@Composable
fun AppErrorState(
    modifier: Modifier = Modifier,
    error: String,
    onRetry: (() -> Unit)? = null,
    retryText: String = "Try Again",
    showIcon: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (showIcon) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        AppTitle(
            text = "Something went wrong",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        AppBody(
            text = error,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(24.dp))
            
            PrimaryButton(
                text = retryText,
                onClick = onRetry
            )
        }
    }
}

/**
 * Success state component for successful operations
 */
@Composable
fun AppSuccessState(
    modifier: Modifier = Modifier,
    title: String = "Success!",
    message: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        androidx.compose.material3.Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AppTitle(
            text = title,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        AppBody(
            text = message,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            
            PrimaryButton(
                text = actionText,
                onClick = onAction
            )
        }
    }
}

/**
 * Offline state component for when there's no internet connection
 */
@Composable
fun AppOfflineState(
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        androidx.compose.material3.Icon(
            imageVector = Icons.Default.WifiOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AppTitle(
            text = "No Internet Connection",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        AppBody(
            text = "Please check your internet connection and try again.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(24.dp))
            
            PrimaryButton(
                text = "Retry",
                onClick = onRetry
            )
        }
    }
}
