package com.cbo.ui.components.states

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cbo.ui.R
import com.cbo.ui.theme.Dimens
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
    title: String = stringResource(R.string.state_empty_title),
    message: String = stringResource(R.string.state_empty_message),
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    secondaryActionText: String? = null,
    onSecondaryAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimens.Padding.xxLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(Dimens.Icon.avatar),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
        
        Spacer(modifier = Modifier.height(Dimens.Spacing.default))
        
        AppTitle(
            text = title,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(Dimens.Spacing.small))
        
        AppBody(
            text = message,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(Dimens.Spacing.extraLarge))
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.Spacing.small)
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
    retryText: String = stringResource(R.string.state_try_again),
    showIcon: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimens.Padding.xxLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (showIcon) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(Dimens.Icon.avatar),
                tint = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(Dimens.Spacing.default))
        }
        
        AppTitle(
            text = stringResource(R.string.state_error_title),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(Dimens.Spacing.small))
        
        AppBody(
            text = error,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(Dimens.Spacing.extraLarge))
            
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
    title: String = stringResource(R.string.state_success_title),
    message: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimens.Padding.xxLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        androidx.compose.material3.Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(Dimens.Icon.avatar),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(Dimens.Spacing.default))
        
        AppTitle(
            text = title,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(Dimens.Spacing.small))
        
        AppBody(
            text = message,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(Dimens.Spacing.extraLarge))
            
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
            .padding(Dimens.Padding.xxLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        androidx.compose.material3.Icon(
            imageVector = Icons.Default.WifiOff,
            contentDescription = null,
            modifier = Modifier.size(Dimens.Icon.avatar),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
        
        Spacer(modifier = Modifier.height(Dimens.Spacing.default))
        
        AppTitle(
            text = stringResource(R.string.state_offline_title),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(Dimens.Spacing.small))
        
        AppBody(
            text = stringResource(R.string.state_offline_message),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(Dimens.Spacing.extraLarge))
            
            PrimaryButton(
                text = stringResource(R.string.btn_retry),
                onClick = onRetry
            )
        }
    }
}




