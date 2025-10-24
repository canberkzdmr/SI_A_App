package com.cbo.ui.components.states

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cbo.ui.theme.MemCloudApplicationTheme

@Preview(showBackground = true, name = "Loading States")
@Composable
fun LoadingStatesPreview() {
    MemCloudApplicationTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Loading screen
            AppLoadingScreen(
                message = "Loading your notes...",
                showProgress = true
            )
        }
    }
}

@Preview(showBackground = true, name = "Empty States")
@Composable
fun EmptyStatesPreview() {
    MemCloudApplicationTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Empty state with action
            AppEmptyState(
                icon = Icons.AutoMirrored.Filled.Note,
                title = "No notes yet",
                message = "Start creating your first note to get organized.",
                actionText = "Create Note",
                onAction = { },
                secondaryActionText = "Learn More",
                onSecondaryAction = { }
            )
        }
    }
}

@Preview(showBackground = true, name = "Error States")
@Composable
fun ErrorStatesPreview() {
    MemCloudApplicationTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error state
            AppErrorState(
                error = "Failed to load notes. Please check your connection and try again.",
                onRetry = { }
            )
        }
    }
}

@Preview(showBackground = true, name = "Success States")
@Composable
fun SuccessStatesPreview() {
    MemCloudApplicationTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Success state
            AppSuccessState(
                title = "Note Saved!",
                message = "Your note has been successfully saved to the cloud.",
                actionText = "View Note",
                onAction = { }
            )
        }
    }
}

@Preview(showBackground = true, name = "Offline States")
@Composable
fun OfflineStatesPreview() {
    MemCloudApplicationTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Offline state
            AppOfflineState(
                onRetry = { }
            )
        }
    }
}

@Preview(showBackground = true, name = "Skeleton Loading")
@Composable
fun SkeletonLoadingPreview() {
    MemCloudApplicationTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Skeleton card
            AppSkeletonCard()
            
            // Skeleton list
            AppSkeletonList(count = 3)
            
            // Skeleton grid
            AppSkeletonGrid(count = 4, columns = 2)
            
            // Skeleton compact list
            AppSkeletonCompactList(count = 5)
        }
    }
}
