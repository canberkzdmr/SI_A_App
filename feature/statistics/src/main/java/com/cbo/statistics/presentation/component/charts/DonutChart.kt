package com.cbo.statistics.presentation.component.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Animasyonlu donut (halka) grafiği.
 *
 * @param slices (label, value, color) üçlüleri listesi
 * @param centerLabel Halkanın ortasındaki büyük yazı
 * @param centerSubLabel Halkanın ortasındaki alt yazı
 */
@Composable
fun DonutChart(
    slices: List<Triple<String, Float, Color>>,
    modifier: Modifier = Modifier,
    centerLabel: String = "",
    centerSubLabel: String = "",
    strokeWidth: Dp = 28.dp,
    showLegend: Boolean = true,
) {
    val total = slices.sumOf { it.second.toDouble() }.toFloat().coerceAtLeast(1f)
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(slices) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(180.dp)) {
                val strokePx = strokeWidth.toPx()
                val diameter = size.minDimension - strokePx
                val topLeft = Offset(strokePx / 2, strokePx / 2)
                val arcSize = Size(diameter, diameter)

                var startAngle = -90f
                slices.forEach { (_, value, color) ->
                    val sweep = (value / total) * 360f * animProgress.value
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Butt)
                    )
                    startAngle += sweep
                }

                // Boş durum için gri halka
                if (slices.isEmpty()) {
                    drawArc(
                        color = Color.Gray.copy(alpha = 0.2f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx)
                    )
                }
            }

            // Ortadaki yazı
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (centerLabel.isNotBlank()) {
                    Text(
                        text = centerLabel,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (centerSubLabel.isNotBlank()) {
                    Text(
                        text = centerSubLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Lejant
        if (showLegend && slices.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            slices.chunked(2).forEach { row ->
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                    row.forEach { (label, value, color) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f).padding(vertical = 2.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(10.dp),
                                shape = CircleShape,
                                color = color
                            ) {}
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$label (${value.toInt()})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Material 3 temasıyla uyumlu donut grafik renk paleti */
val donutPalette = listOf(
    Color(0xFF6366F1), // indigo
    Color(0xFF22D3EE), // cyan
    Color(0xFFF59E0B), // amber
    Color(0xFF10B981), // emerald
    Color(0xFFF43F5E), // rose
    Color(0xFF8B5CF6), // violet
    Color(0xFF06B6D4), // sky
    Color(0xFFEC4899), // pink
)
