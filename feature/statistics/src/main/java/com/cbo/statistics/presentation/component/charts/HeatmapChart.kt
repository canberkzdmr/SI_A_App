package com.cbo.statistics.presentation.component.charts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * GitHub benzeri aktivite ısı haritası (Heatmap).
 *
 * Son [weekCount] haftayı yatayda, haftanın günlerini dikeyde gösterir.
 * Her hücrenin rengi, o güne ait not sayısına göre [emptyColor]'dan [fullColor]'a ölçeklenir.
 *
 * @param notesPerDay "YYYY-MM-DD" → count eşlemesi
 * @param weekCount Gösterilecek hafta sayısı (varsayılan: 16)
 */
@Composable
fun HeatmapChart(
    notesPerDay: Map<String, Int>,
    modifier: Modifier = Modifier,
    weekCount: Int = 16,
    cellSize: Dp = 14.dp,
    cellSpacing: Dp = 3.dp,
    emptyColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    fullColor: Color = MaterialTheme.colorScheme.primary,
) {
    val maxCount = (notesPerDay.values.maxOrNull() ?: 1).coerceAtLeast(1)
    val today = LocalDate.now()
    val dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // Son weekCount haftanın her gününü oluştur (Pazartesi başlangıçlı)
    val weeks: List<List<LocalDate?>> = buildList {
        // Bugünün Pazartesi'sine hizala
        val endDate = today
        var startDate = today.minusWeeks(weekCount.toLong()).let { d ->
            // En yakın Pazartesi'ye ilerlet
            d.plusDays(((8 - d.dayOfWeek.value) % 7).toLong()).let { if (it > d) d else it }
        }

        var current = startDate
        var week = mutableListOf<LocalDate?>()
        // Pazartesi = 1
        val startDow = current.dayOfWeek.value // 1=Mon..7=Sun
        repeat(startDow - 1) { week.add(null) } // boş hücre dolgu

        while (!current.isAfter(endDate)) {
            week.add(current)
            if (week.size == 7) {
                add(week.toList())
                week = mutableListOf()
            }
            current = current.plusDays(1)
        }
        if (week.isNotEmpty()) {
            while (week.size < 7) week.add(null)
            add(week.toList())
        }
    }

    val dayLabels = listOf("P", "S", "Ç", "P", "C", "C", "P") // Pzt-Paz

    var animationProgress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(notesPerDay) { animationProgress = 1f }
    val animatedProgress by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = tween(durationMillis = 800),
        label = "heatmap_anim"
    )

    Column(modifier = modifier) {
        Row {
            // Gün etiketleri (dikey)
            Column(modifier = Modifier.padding(end = 4.dp)) {
                Spacer(modifier = Modifier.height(cellSize + cellSpacing))
                dayLabels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.height(cellSize + cellSpacing)
                    )
                }
            }

            // Hücre grid'i
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((cellSize + cellSpacing) * 8) // 7 gün + başlık
            ) {
                val cellPx = cellSize.toPx()
                val spacingPx = cellSpacing.toPx()
                val colWidth = cellPx + spacingPx

                weeks.takeLast(weekCount).forEachIndexed { weekIdx, week ->
                    week.forEachIndexed { dayIdx, date ->
                        val x = weekIdx * colWidth
                        val y = dayIdx * (cellPx + spacingPx)

                        val count = date?.let { notesPerDay[dayFormatter.format(it)] ?: 0 } ?: 0
                        val fraction = if (date != null && maxCount > 0)
                            (count.toFloat() / maxCount) * animatedProgress
                        else 0f

                        val color = if (date == null) Color.Transparent
                        else if (count == 0) emptyColor
                        else lerp(emptyColor, fullColor, fraction.coerceIn(0.15f, 1f))

                        drawRoundRect(
                            color = color,
                            topLeft = Offset(x, y),
                            size = Size(cellPx, cellPx),
                            cornerRadius = CornerRadius(3f, 3f)
                        )
                    }
                }
            }
        }
    }
}
