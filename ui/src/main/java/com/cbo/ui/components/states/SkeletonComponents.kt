package com.cbo.ui.components.states

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Skeleton loading animation
 */
@Composable
private fun SkeletonBox(
    modifier: Modifier = Modifier,
    width: Int? = null,
    height: Int = 20
) {
    var alpha by remember { mutableFloatStateOf(0.3f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            alpha = 0.3f
            delay(600)
            alpha = 0.7f
            delay(600)
        }
    }
    
    Box(
        modifier = modifier
            .let { if (width != null) it.width(width.dp) else it }
            .height(height.dp)
            .background(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                shape = RoundedCornerShape(4.dp)
            )
    )
}

/**
 * Skeleton card component
 */
@Composable
fun AppSkeletonCard(
    modifier: Modifier = Modifier,
    showAvatar: Boolean = true,
    showActions: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showAvatar) {
                SkeletonBox(
                    modifier = Modifier.size(40.dp),
                    width = 40,
                    height = 40
                )
                
                Spacer(modifier = Modifier.width(12.dp))
            }
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                SkeletonBox(
                    modifier = Modifier.fillMaxWidth(0.7f),
                    height = 16
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                SkeletonBox(
                    modifier = Modifier.fillMaxWidth(0.5f),
                    height = 14
                )
            }
            
            if (showActions) {
                SkeletonBox(
                    modifier = Modifier.size(24.dp),
                    width = 24,
                    height = 24
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        SkeletonBox(
            modifier = Modifier.fillMaxWidth(),
            height = 16
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        SkeletonBox(
            modifier = Modifier.fillMaxWidth(0.8f),
            height = 16
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        SkeletonBox(
            modifier = Modifier.fillMaxWidth(0.6f),
            height = 16
        )
    }
}

/**
 * Skeleton list component
 */
@Composable
fun AppSkeletonList(
    modifier: Modifier = Modifier,
    count: Int = 3,
    showAvatar: Boolean = true,
    showActions: Boolean = true
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(count) {
            AppSkeletonCard(
                showAvatar = showAvatar,
                showActions = showActions
            )
        }
    }
}

/**
 * Skeleton grid component
 */
@Composable
fun AppSkeletonGrid(
    modifier: Modifier = Modifier,
    count: Int = 6,
    columns: Int = 2
) {
    val rows = (count + columns - 1) / columns
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(rows) { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(columns) { col ->
                    val index = row * columns + col
                    if (index < count) {
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            AppSkeletonCard(
                                showAvatar = false,
                                showActions = false
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Skeleton compact list component
 */
@Composable
fun AppSkeletonCompactList(
    modifier: Modifier = Modifier,
    count: Int = 5
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(count) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonBox(
                    modifier = Modifier.size(24.dp),
                    width = 24,
                    height = 24
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                SkeletonBox(
                    modifier = Modifier.weight(1f),
                    height = 16
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                SkeletonBox(
                    modifier = Modifier.size(20.dp),
                    width = 20,
                    height = 20
                )
            }
        }
    }
}
