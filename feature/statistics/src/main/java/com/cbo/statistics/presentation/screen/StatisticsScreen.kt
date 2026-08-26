package com.cbo.statistics.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cbo.notes.domain.model.NoteStatistics
import com.cbo.statistics.presentation.component.charts.DonutChart
import com.cbo.statistics.presentation.component.charts.HeatmapChart
import com.cbo.statistics.presentation.component.charts.donutPalette
import com.cbo.statistics.presentation.viewmodel.StatisticsViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "İstatistikler",
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                ),
                actions = {
                    IconButton(onClick = { viewModel.retry() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Yenile")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "İstatistikler hesaplanıyor…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Veriler yüklenemedi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.retry() }) { Text("Tekrar Dene") }
                    }
                }
            }
            uiState.statistics != null -> {
                val stats = uiState.statistics!!
                StatisticsContent(
                    stats = stats,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun StatisticsContent(
    stats: NoteStatistics,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { OverviewSection(stats) }
        item { HeatmapSection(stats) }
        item { StreakSection(stats) }
        item { TrendSection(stats) }
        item { HourlyDistributionSection(stats) }
        item { CategorySection(stats) }
        item { TagCloudSection(stats) }
        item { ZettelkastenSection(stats) }
        item { ReminderSection(stats) }
        item { WeeklyDigestSection(stats) }
        item { HygieneSection(stats) }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

// =============================================================================
// BÖLÜM 1: Genel Bakış (Overview)
// =============================================================================
@Composable
private fun OverviewSection(stats: NoteStatistics) {
    StatCard(title = "Genel Bakış", icon = Icons.AutoMirrored.Filled.Note) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard("Toplam Not", stats.totalNotes.toString(), Icons.Default.Description, Color(0xFF6366F1), Modifier.weight(1f))
                KpiCard("Kelime", formatNumber(stats.totalWordCount), Icons.Default.TextFields, Color(0xFF22D3EE), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard("Favori", stats.favoriteNotes.toString(), Icons.Default.Favorite, Color(0xFFF43F5E), Modifier.weight(1f))
                KpiCard("Arşivlenmiş", stats.archivedNotes.toString(), Icons.Default.Archive, Color(0xFFF59E0B), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard("Sabitlenen", stats.pinnedNotes.toString(), Icons.Default.PushPin, Color(0xFF10B981), Modifier.weight(1f))
                KpiCard("Okuma Süresi", "${stats.estimatedReadingMinutes} dk", Icons.Default.Schedule, Color(0xFF8B5CF6), Modifier.weight(1f))
            }

            // Görev Tamamlanma Bar'ı
            if (stats.totalTodoItems > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Görev Tamamlama", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { stats.todoCompletionRate },
                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                    color = Color(0xFF10B981),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${stats.completedTodoItems} / ${stats.totalTodoItems} görev tamamlandı (${(stats.todoCompletionRate * 100).toInt()}%)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Medya özeti
            if (stats.totalAttachments > 0) {
                HorizontalDivider()
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AttachmentStat("Toplam Ek", stats.totalAttachments.toString(), Icons.Default.Attachment)
                    AttachmentStat("Görsel", stats.imageAttachments.toString(), Icons.Default.Image)
                    AttachmentStat("Ses", stats.audioAttachments.toString(), Icons.Default.Mic)
                }
            }
        }
    }
}

// =============================================================================
// BÖLÜM 2: Isı Haritası (Heatmap)
// =============================================================================
@Composable
private fun HeatmapSection(stats: NoteStatistics) {
    StatCard(title = "Aktivite Haritası", icon = Icons.Default.GridOn) {
        if (stats.notesPerDay.isEmpty()) {
            EmptyState("Henüz aktivite yok")
        } else {
            HeatmapChart(
                notesPerDay = stats.notesPerDay,
                modifier = Modifier.fillMaxWidth(),
                weekCount = 16,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Az", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(4.dp))
                listOf(0.1f, 0.3f, 0.6f, 1.0f).forEach { alpha ->
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                }
                Text("Çok", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// =============================================================================
// BÖLÜM 3: Seri & Verimlilik (Streak)
// =============================================================================
@Composable
private fun StreakSection(stats: NoteStatistics) {
    StatCard(title = "Üretkenlik", icon = Icons.Default.LocalFireDepartment) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StreakCard(
                value = stats.currentStreak,
                label = "Güncel Seri",
                emoji = if (stats.currentStreak >= 7) "🔥" else "📝",
                color = Color(0xFFFF6B35),
                modifier = Modifier.weight(1f)
            )
            StreakCard(
                value = stats.longestStreak,
                label = "En Uzun Seri",
                emoji = "🏆",
                color = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        stats.peakHour?.let { hour ->
            val period = when {
                hour in 5..11 -> "Sabah"
                hour in 12..17 -> "Öğleden Sonra"
                hour in 18..21 -> "Akşam"
                else -> "Gece"
            }
            InsightChip(
                icon = Icons.Default.WbTwilight,
                text = "En verimli zaman: $period ($hour:00 - ${hour + 1}:00)"
            )
        }
        stats.peakDayOfWeek?.let { dow ->
            val dayName = listOf("Pazar", "Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi").getOrNull(dow) ?: ""
            Spacer(modifier = Modifier.height(6.dp))
            InsightChip(
                icon = Icons.Default.CalendarToday,
                text = "En aktif gün: $dayName"
            )
        }
    }
}

// =============================================================================
// BÖLÜM 4: Aylık Trend (Vico ColumnChart)
// =============================================================================
@Composable
private fun TrendSection(stats: NoteStatistics) {
    if (stats.notesPerMonth.isEmpty()) return

    StatCard(title = "Aylık Trend", icon = Icons.Default.TrendingUp) {
        val sortedMonths = stats.notesPerMonth.entries.sortedBy { it.key }.takeLast(6)
        if (sortedMonths.isEmpty()) {
            EmptyState("Yeterli veri yok")
            return@StatCard
        }
        val modelProducer = remember { CartesianChartModelProducer() }
        LaunchedEffect(stats.notesPerMonth) {
            modelProducer.runTransaction {
                columnSeries { series(sortedMonths.map { it.value }) }
            }
        }
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = { _, x, _ ->
                        sortedMonths.getOrNull(x.toInt())?.key?.takeLast(5) ?: ""
                    }
                ),
            ),
            modelProducer = modelProducer,
            modifier = Modifier.fillMaxWidth().height(160.dp),
        )
    }
}

// =============================================================================
// BÖLÜM 5: Saatlik Dağılım (Vico ColumnChart)
// =============================================================================
@Composable
private fun HourlyDistributionSection(stats: NoteStatistics) {
    if (stats.notesPerHour.isEmpty()) return

    StatCard(title = "Günlük Dağılım", icon = Icons.Default.QueryStats) {
        val hours = (0..23).map { stats.notesPerHour[it] ?: 0 }
        val modelProducer = remember { CartesianChartModelProducer() }
        LaunchedEffect(stats.notesPerHour) {
            modelProducer.runTransaction {
                lineSeries { series(hours.map { it }) }
            }
        }
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = { _, x, _ -> "${x.toInt()}:00" }
                ),
            ),
            modelProducer = modelProducer,
            modifier = Modifier.fillMaxWidth().height(140.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Saat dilimlerine göre not oluşturma sıklığı",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// =============================================================================
// BÖLÜM 6: Kategori Dağılımı (DonutChart)
// =============================================================================
@Composable
private fun CategorySection(stats: NoteStatistics) {
    if (stats.categoryDistribution.isEmpty()) return

    StatCard(title = "Kategori Dağılımı", icon = Icons.Default.Category) {
        val slices = stats.categoryDistribution.entries
            .sortedByDescending { it.value }
            .take(8)
            .mapIndexed { i, (name, count) ->
                Triple(name, count.toFloat(), donutPalette[i % donutPalette.size])
            }
        val topCategory = slices.firstOrNull()?.first ?: ""
        DonutChart(
            slices = slices,
            centerLabel = topCategory.take(8),
            centerSubLabel = "En fazla",
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// =============================================================================
// BÖLÜM 7: Etiket Bulutu
// =============================================================================
@Composable
private fun TagCloudSection(stats: NoteStatistics) {
    if (stats.topTags.isEmpty()) return

    StatCard(title = "Popüler Etiketler", icon = Icons.Default.Tag) {
        val colors = listOf(
            Color(0xFF6366F1), Color(0xFF22D3EE), Color(0xFFF59E0B),
            Color(0xFF10B981), Color(0xFFF43F5E), Color(0xFF8B5CF6)
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            stats.topTags.take(8).forEachIndexed { i, (name, count) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = colors[i % colors.size].copy(alpha = 0.15f),
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Text(
                            "#$name",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors[i % colors.size],
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        "$count not",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    LinearProgressIndicator(
                        progress = { count.toFloat() / (stats.topTags.firstOrNull()?.second ?: 1).toFloat() },
                        modifier = Modifier.width(60.dp).height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = colors[i % colors.size],
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }
        }
    }
}

// =============================================================================
// BÖLÜM 8: Zettelkasten Analizi
// =============================================================================
@Composable
private fun ZettelkastenSection(stats: NoteStatistics) {
    StatCard(title = "Zettelkasten & Bilgi Ağı", icon = Icons.Default.AccountTree) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KpiCard("Bağlantılar", stats.totalNoteLinks.toString(), Icons.Default.Link, Color(0xFF6366F1), Modifier.weight(1f))
            KpiCard("Bağlantısız", stats.orphanNoteCount.toString(), Icons.Default.LinkOff, Color(0xFFF59E0B), Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        InsightChip(
            icon = Icons.Default.Hub,
            text = "Bağlantı yoğunluğu: %.2f bağ/not".format(stats.linkDensity)
        )
        if (stats.orphanNoteCount > 0) {
            Spacer(modifier = Modifier.height(6.dp))
            InsightChip(
                icon = Icons.Default.Warning,
                text = "${stats.orphanNoteCount} not hiçbir şeye bağlı değil. Bağlantı eklemeyi deneyin!",
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

// =============================================================================
// BÖLÜM 9: Hatırlatıcı Analitiği
// =============================================================================
@Composable
private fun ReminderSection(stats: NoteStatistics) {
    if (stats.activeReminderCount == 0 && stats.expiredReminderCount == 0 && stats.locationReminderCount == 0) return

    StatCard(title = "Hatırlatıcılar", icon = Icons.Default.Alarm) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KpiCard("Aktif", stats.activeReminderCount.toString(), Icons.Default.NotificationsActive, Color(0xFF10B981), Modifier.weight(1f))
            KpiCard("Süresi Geçmiş", stats.expiredReminderCount.toString(), Icons.Default.NotificationsOff, Color(0xFFF43F5E), Modifier.weight(1f))
        }
        if (stats.locationReminderCount > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            InsightChip(
                icon = Icons.Default.LocationOn,
                text = "${stats.locationReminderCount} konum bazlı hatırlatıcı aktif"
            )
        }
    }
}

// =============================================================================
// BÖLÜM 10: Haftalık Özet
// =============================================================================
@Composable
private fun WeeklyDigestSection(stats: NoteStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bu Haftanın Özeti", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                    WeeklyStat("${stats.weeklyNoteCount}", "Yeni Not", Color.White)
                    WeeklyStat("${stats.weeklyCompletedTodoCount}", "Tamamlanan Görev", Color.White)
                    if (stats.mostUsedTagThisWeek != null) {
                        WeeklyStat("#${stats.mostUsedTagThisWeek}", "Popüler Etiket", Color.White)
                    }
                }
            }
        }
    }
}

// =============================================================================
// BÖLÜM 11: Not Sağlığı (Hygiene)
// =============================================================================
@Composable
private fun HygieneSection(stats: NoteStatistics) {
    if (stats.staleNoteCount == 0 && stats.fullyCompletedTodoNoteCount == 0) return

    StatCard(title = "Not Sağlığı", icon = Icons.Default.HealthAndSafety) {
        if (stats.staleNoteCount > 0) {
            HygieneAlert(
                icon = Icons.Default.AccessTime,
                title = "${stats.staleNoteCount} Eski Not",
                description = "90 günden uzun süredir güncellenmemiş. Gözden geçirmeyi veya arşivlemeyi düşünün.",
                color = Color(0xFFF59E0B)
            )
        }
        if (stats.fullyCompletedTodoNoteCount > 0) {
            if (stats.staleNoteCount > 0) Spacer(modifier = Modifier.height(8.dp))
            HygieneAlert(
                icon = Icons.Default.CheckCircle,
                title = "${stats.fullyCompletedTodoNoteCount} Not Arşivlemeye Hazır",
                description = "Tüm görevleri tamamlanmış notlar arşive taşınabilir.",
                color = Color(0xFF10B981)
            )
        }
    }
}

// =============================================================================
// YARDIMCI BİLEŞENLER
// =============================================================================

@Composable
private fun StatCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun KpiCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun StreakCard(
    value: Int,
    label: String,
    emoji: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text("$value gün", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun InsightChip(
    icon: ImageVector,
    text: String,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.08f),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun AttachmentStat(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun WeeklyStat(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.8f), textAlign = TextAlign.Center)
    }
}

@Composable
private fun HygieneAlert(icon: ImageVector, title: String, description: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.1f),
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = color)
                Spacer(modifier = Modifier.height(2.dp))
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatNumber(n: Long): String = when {
    n >= 1_000_000 -> "%.1fM".format(n / 1_000_000.0)
    n >= 1_000 -> "%.1fK".format(n / 1_000.0)
    else -> n.toString()
}
