package com.cbo.splash

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cbo.core.data.biometric.BiometricUtils
import com.cbo.ui.theme.MemCloudApplicationTheme
import androidx.compose.ui.res.stringResource

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    val context = LocalContext.current as FragmentActivity
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isLoading, state.isLoggedIn) {
        Log.d("SplashScreen", "SplashScreen launched effect")
        if (!state.isLoading) {
            if (state.isLoggedIn) {
                Log.d("SplashScreen", "isBiometricEnabled -> ${state.isBiometricEnabled}")
                if (state.isBiometricEnabled) {
                    BiometricUtils.showBiometricPrompt(
                        activity = context,
                        onSuccess = {
                            Log.i("SplashScreen", "Biometric Prompt Success")
                            onNavigateToMain()
                        },
                        onFail = { message ->
                            Log.e("SplashScreen", "Biometric Prompt Fail")
                        },
                        onError = { message ->
                            Log.e("SplashScreen", "Biometric Prompt Error")
                            onNavigateToLogin()
                        },
                    )
                } else {
                    onNavigateToMain()
                }
            } else {
                Log.d("SplashScreen", "isLoggedIn -> ${state.isBiometricEnabled}")
                onNavigateToLogin()
            }
        }
    }

    SplashContent(isLoading = state.isLoading)
}

@Composable
private fun SplashContent(isLoading: Boolean) {
    // Create gradient background
    val gradientColors = listOf(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.secondaryContainer
    )

    // Animation for logo entrance
    val logoScale = remember { Animatable(0f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Animate logo entrance
        logoAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
        // Animate text after logo
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = gradientColors,
                    radius = 1200f,
                    center = Offset(0.5f, 0.3f)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Logo
            AnimatedMemCloudLogo(
                modifier = Modifier
                    .size(120.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value),
                isAnimating = isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))

            // App Name
            Text(
                text = stringResource(id = com.cbo.splash.R.string.app_name),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.alpha(textAlpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = stringResource(id = com.cbo.splash.R.string.tagline),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(textAlpha.value)
            )

            if (isLoading) {
                Spacer(modifier = Modifier.height(48.dp))

                // Modern loading indicator
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(32.dp)
                        .alpha(textAlpha.value),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp
                )
            }
        }
    }
}

@Composable
private fun AnimatedMemCloudLogo(
    modifier: Modifier = Modifier,
    isAnimating: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logo_animation")
    
    val cloudFloat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isAnimating) 8f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud_float"
    )

    val documentGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = if (isAnimating) 1f else 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "document_glow"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Glowing background circle
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        radius = 150f
                    ),
                    shape = CircleShape
                )
        )

        Canvas(modifier = Modifier.size(80.dp)) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            drawMemCloudIcon(
                drawScope = this,
                canvasWidth = canvasWidth,
                canvasHeight = canvasHeight,
                primaryColor = Color(0xFF2A6A47), // Green from theme
                secondaryColor = Color(0xFF93D5AA), // Lighter green
                documentAlpha = documentGlow,
                cloudOffset = cloudFloat
            )
        }
    }
}

private fun drawMemCloudIcon(
    drawScope: DrawScope,
    canvasWidth: Float,
    canvasHeight: Float,
    primaryColor: Color,
    secondaryColor: Color,
    documentAlpha: Float,
    cloudOffset: Float
) {
    with(drawScope) {
        val centerX = canvasWidth / 2
        val centerY = canvasHeight / 2

        // Draw cloud with floating animation
        val cloudPath = Path().apply {
            // Main cloud body
            addRoundRect(
                RoundRect(
                    rect = Rect(
                        offset = Offset(centerX - 25, centerY - 15 + cloudOffset),
                        size = Size(35f, 20f)
                    ),
                    radiusX = 10f,
                    radiusY = 10f
                )
            )
            
            // Cloud bumps for more realistic shape
            addOval(
                Rect(
                    offset = Offset(centerX - 20, centerY - 20 + cloudOffset),
                    size = Size(15f, 15f)
                )
            )
            
            addOval(
                Rect(
                    offset = Offset(centerX - 5, centerY - 18 + cloudOffset),
                    size = Size(18f, 18f)
                )
            )
            
            addOval(
                Rect(
                    offset = Offset(centerX + 8, centerY - 15 + cloudOffset),
                    size = Size(12f, 12f)
                )
            )
        }

        // Draw cloud with gradient
        drawPath(
            path = cloudPath,
            brush = Brush.radialGradient(
                colors = listOf(secondaryColor, primaryColor.copy(alpha = 0.8f)),
                center = Offset(centerX, centerY - 8 + cloudOffset),
                radius = 30f
            )
        )

        // Draw document with glow effect
        val documentRect = RoundRect(
            rect = Rect(
                offset = Offset(centerX + 8, centerY - 5),
                size = Size(20f, 26f)
            ),
            radiusX = 2f,
            radiusY = 2f
        )

        // Document glow
        drawRoundRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = documentAlpha * 0.3f),
                    Color.Transparent
                ),
                center = Offset(centerX + 18, centerY + 8),
                radius = 25f
            ),
            size = Size(24f, 30f),
            topLeft = Offset(centerX + 6, centerY - 7)
        )

        // Main document
        drawRoundRect(
            color = Color.White,
            size = Size(20f, 26f),
            topLeft = Offset(centerX + 8, centerY - 5),
            style = Stroke(width = 1.5f)
        )
        
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.White, Color.White.copy(alpha = 0.9f))
            ),
            size = Size(18f, 24f),
            topLeft = Offset(centerX + 9, centerY - 4)
        )

        // Document lines with animation
        val lineColor = primaryColor.copy(alpha = documentAlpha)
        drawLine(
            color = lineColor,
            start = Offset(centerX + 11, centerY - 1),
            end = Offset(centerX + 24, centerY - 1),
            strokeWidth = 1f
        )
        drawLine(
            color = lineColor,
            start = Offset(centerX + 11, centerY + 2),
            end = Offset(centerX + 24, centerY + 2),
            strokeWidth = 1f
        )
        drawLine(
            color = lineColor,
            start = Offset(centerX + 11, centerY + 5),
            end = Offset(centerX + 21, centerY + 5),
            strokeWidth = 1f
        )
        drawLine(
            color = lineColor,
            start = Offset(centerX + 11, centerY + 8),
            end = Offset(centerX + 23, centerY + 8),
            strokeWidth = 1f
        )
    }
}

@Preview
@Composable
fun SplashScreenPreview() {
    MemCloudApplicationTheme {
        SplashContent(isLoading = true)
    }
}