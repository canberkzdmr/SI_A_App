package com.cbo.statistics.presentation.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cbo.notes.domain.model.NoteStatistics
import com.cbo.statistics.R
import com.cbo.statistics.presentation.component.charts.DonutChart
import com.cbo.statistics.presentation.component.charts.HeatmapChart
import com.cbo.statistics.presentation.component.charts.donutPalette
import com.cbo.statistics.presentation.viewmodel.StatisticsUiState
import com.cbo.statistics.presentation.viewmodel.StatisticsViewModel
import com.cbo.ui.components.AppBody
import com.cbo.ui.components.AppCaption
import com.cbo.ui.components.AppIconButton
import com.cbo.ui.components.AppLabel
import com.cbo.ui.components.AppScaffold
import com.cbo.ui.components.AppTitle
import com.cbo.ui.components.AppTitleMedium
import com.cbo.ui.components.display.AppCompactStatItem
import com.cbo.ui.components.display.AppInsightBanner
import com.cbo.ui.components.display.AppKpiCard
import com.cbo.ui.components.display.AppSectionCard
import com.cbo.ui.components.display.AppStreakCard
import com.cbo.ui.components.states.AppErrorState
import com.cbo.ui.components.states.AppLoadingScreen
import com.cbo.ui.theme.MemCloudApplicationTheme
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.cbo.ui.performance.TrackScreenPerformance

@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel(),
) {
    TrackScreenPerformance("statistics")
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StatisticsScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onRetry = viewModel::retry,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreenContent(
    uiState: StatisticsUiState,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    AppTitle(
                        text = stringResource(R.string.statistics_title),
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    AppIconButton(
                        onClick = onNavigateBack,
                        icon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back)) }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                ),
                actions = {
                    AppIconButton(
                        onClick = onRetry,
                        icon = { Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh)) }
                    )
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                AppLoadingScreen(
                    modifier = Modifier.padding(padding),
                    message = stringResource(R.string.calculating_statistics)
                )
            }
            uiState.error != null || uiState.errorRes != null -> {
                val errorMessage = uiState.errorRes?.let { stringResource(it) } ?: uiState.error ?: stringResource(R.string.unknown_error)
                AppErrorState(
                    modifier = Modifier.padding(padding),
                    error = errorMessage,
                    onRetry = onRetry,
                    retryText = stringResource(R.string.retry)
                )
            }
            uiState.statistics != null -> {
                StatisticsContent(
                    stats = uiState.statistics,
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
    AppSectionCard(title = stringResource(R.string.stats_overview), icon = Icons.AutoMirrored.Filled.Note) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppKpiCard(stringResource(R.string.stats_total_notes), stats.totalNotes.toString(), Icons.Default.Description, Color(0xFF6366F1), Modifier.weight(1f))
                AppKpiCard(stringResource(R.string.stats_words), formatNumber(stats.totalWordCount), Icons.Default.TextFields, Color(0xFF22D3EE), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppKpiCard(stringResource(R.string.stats_favorites), stats.favoriteNotes.toString(), Icons.Default.Favorite, Color(0xFFF43F5E), Modifier.weight(1f))
                AppKpiCard(stringResource(R.string.stats_archived), stats.archivedNotes.toString(), Icons.Default.Archive, Color(0xFFF59E0B), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppKpiCard(stringResource(R.string.stats_pinned), stats.pinnedNotes.toString(), Icons.Default.PushPin, Color(0xFF10B981), Modifier.weight(1f))
                AppKpiCard(stringResource(R.string.stats_reading_time), stringResource(R.string.stats_reading_minutes_format, stats.estimatedReadingMinutes), Icons.Default.Schedule, Color(0xFF8B5CF6), Modifier.weight(1f))
            }

            // Görev Tamamlanma Bar'ı
            if (stats.totalTodoItems > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                AppCaption(text = stringResource(R.string.stats_todo_completion))
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { stats.todoCompletionRate },
                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                    color = Color(0xFF10B981),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                AppCaption(
                    text = stringResource(
                        R.string.stats_todos_completed_format,
                        stats.completedTodoItems,
                        stats.totalTodoItems,
                        (stats.todoCompletionRate * 100).toInt()
                    )
                )
            }

            // Medya özeti
            if (stats.totalAttachments > 0) {
                HorizontalDivider()
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppCompactStatItem(value = stats.totalAttachments.toString(), label = stringResource(R.string.stats_total_attachments), icon = Icons.Default.Attachment)
                    AppCompactStatItem(value = stats.imageAttachments.toString(), label = stringResource(R.string.stats_images), icon = Icons.Default.Image)
                    AppCompactStatItem(value = stats.audioAttachments.toString(), label = stringResource(R.string.stats_audio), icon = Icons.Default.Mic)
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
    AppSectionCard(title = stringResource(R.string.stats_activity_map), icon = Icons.Default.GridOn) {
        if (stats.notesPerDay.isEmpty()) {
            AppBody(stringResource(R.string.stats_no_activity_yet), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
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
                AppCaption(stringResource(R.string.stats_less))
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
                AppCaption(stringResource(R.string.stats_more))
            }
        }
    }
}

// =============================================================================
// BÖLÜM 3: Seri & Verimlilik (Streak)
// =============================================================================
@Composable
private fun StreakSection(stats: NoteStatistics) {
    AppSectionCard(title = stringResource(R.string.stats_productivity), icon = Icons.Default.LocalFireDepartment) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AppStreakCard(
                value = stats.currentStreak,
                label = stringResource(R.string.stats_current_streak),
                emoji = if (stats.currentStreak >= 7) "🔥" else "📝",
                color = Color(0xFFFF6B35),
                unit = stringResource(R.string.stats_unit_days),
                modifier = Modifier.weight(1f)
            )
            AppStreakCard(
                value = stats.longestStreak,
                label = stringResource(R.string.stats_longest_streak),
                emoji = "🏆",
                color = Color(0xFFF59E0B),
                unit = stringResource(R.string.stats_unit_days),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        stats.peakHour?.let { hour ->
            val period = when {
                hour in 5..11 -> stringResource(R.string.stats_period_morning)
                hour in 12..17 -> stringResource(R.string.stats_period_afternoon)
                hour in 18..21 -> stringResource(R.string.stats_period_evening)
                else -> stringResource(R.string.stats_period_night)
            }
            AppInsightBanner(
                icon = Icons.Default.WbTwilight,
                text = stringResource(R.string.stats_peak_time_format, period, hour, hour + 1)
            )
        }
        stats.peakDayOfWeek?.let { dow ->
            val dayName = java.time.DayOfWeek.of(if (dow == 0) 7 else dow).getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault())
            Spacer(modifier = Modifier.height(6.dp))
            AppInsightBanner(
                icon = Icons.Default.CalendarToday,
                text = stringResource(R.string.stats_peak_day_format, dayName)
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

    AppSectionCard(title = stringResource(R.string.stats_monthly_trend), icon = Icons.AutoMirrored.Filled.TrendingUp) {
        val sortedMonths = stats.notesPerMonth.entries.sortedBy { it.key }.takeLast(6)
        if (sortedMonths.isEmpty()) {
            AppBody(stringResource(R.string.stats_not_enough_data), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
            return@AppSectionCard
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

    AppSectionCard(title = stringResource(R.string.stats_daily_distribution), icon = Icons.Default.QueryStats) {
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
        AppCaption(
            text = stringResource(R.string.stats_hourly_caption),
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

    AppSectionCard(title = stringResource(R.string.stats_category_distribution), icon = Icons.Default.Category) {
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
            centerSubLabel = stringResource(R.string.stats_most),
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

    AppSectionCard(title = stringResource(R.string.stats_popular_tags), icon = Icons.Default.Tag) {
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
                        AppLabel(
                            text = "#$name",
                            color = colors[i % colors.size],
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    AppCaption(text = stringResource(R.string.stats_tag_note_count, count))
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
    AppSectionCard(title = stringResource(R.string.stats_zettelkasten_title), icon = Icons.Default.AccountTree) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AppKpiCard(stringResource(R.string.stats_links), stats.totalNoteLinks.toString(), Icons.Default.Link, Color(0xFF6366F1), Modifier.weight(1f))
            AppKpiCard(stringResource(R.string.stats_unlinked), stats.orphanNoteCount.toString(), Icons.Default.LinkOff, Color(0xFFF59E0B), Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        AppInsightBanner(
            icon = Icons.Default.Hub,
            text = stringResource(R.string.stats_link_density_format, stats.linkDensity)
        )
        if (stats.orphanNoteCount > 0) {
            Spacer(modifier = Modifier.height(6.dp))
            AppInsightBanner(
                icon = Icons.Default.Warning,
                text = stringResource(R.string.stats_orphan_notes_warning, stats.orphanNoteCount),
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

    AppSectionCard(title = stringResource(R.string.stats_reminders), icon = Icons.Default.Alarm) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AppKpiCard(stringResource(R.string.stats_active), stats.activeReminderCount.toString(), Icons.Default.NotificationsActive, Color(0xFF10B981), Modifier.weight(1f))
            AppKpiCard(stringResource(R.string.stats_expired), stats.expiredReminderCount.toString(), Icons.Default.NotificationsOff, Color(0xFFF43F5E), Modifier.weight(1f))
        }
        if (stats.locationReminderCount > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            AppInsightBanner(
                icon = Icons.Default.LocationOn,
                text = stringResource(R.string.stats_location_reminders_active, stats.locationReminderCount)
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
                    AppTitleMedium(
                        text = stringResource(R.string.stats_weekly_summary),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                    AppCompactStatItem(value = "${stats.weeklyNoteCount}", label = stringResource(R.string.stats_weekly_new_notes), color = Color.White)
                    AppCompactStatItem(value = "${stats.weeklyCompletedTodoCount}", label = stringResource(R.string.stats_weekly_completed_todos), color = Color.White)
                    if (stats.mostUsedTagThisWeek != null) {
                        AppCompactStatItem(value = "#${stats.mostUsedTagThisWeek}", label = stringResource(R.string.stats_weekly_popular_tag), color = Color.White)
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

    AppSectionCard(title = stringResource(R.string.stats_note_hygiene), icon = Icons.Default.HealthAndSafety) {
        if (stats.staleNoteCount > 0) {
            AppInsightBanner(
                icon = Icons.Default.AccessTime,
                title = stringResource(R.string.stats_stale_notes_title, stats.staleNoteCount),
                text = stringResource(R.string.stats_stale_notes_desc),
                color = Color(0xFFF59E0B)
            )
        }
        if (stats.fullyCompletedTodoNoteCount > 0) {
            if (stats.staleNoteCount > 0) Spacer(modifier = Modifier.height(8.dp))
            AppInsightBanner(
                icon = Icons.Default.CheckCircle,
                title = stringResource(R.string.stats_ready_to_archive_title, stats.fullyCompletedTodoNoteCount),
                text = stringResource(R.string.stats_ready_to_archive_desc),
                color = Color(0xFF10B981)
            )
        }
    }
}

private fun formatNumber(n: Long): String = when {
    n >= 1_000_000 -> "%.1fM".format(n / 1_000_000.0)
    n >= 1_000 -> "%.1fK".format(n / 1_000.0)
    else -> n.toString()
}

// =============================================================================
// PREVIEWS & MOCK DATA
// =============================================================================

private fun sampleNoteStatistics(): NoteStatistics = NoteStatistics(
    totalNotes = 48,
    archivedNotes = 12,
    favoriteNotes = 14,
    pinnedNotes = 5,
    deletedNotes = 3,
    totalWordCount = 18450L,
    totalTodoItems = 32,
    completedTodoItems = 24,
    totalAttachments = 16,
    imageAttachments = 11,
    audioAttachments = 5,
    notesPerDay = mapOf(
        "2026-08-20" to 3,
        "2026-08-21" to 5,
        "2026-08-22" to 2,
        "2026-08-23" to 0,
        "2026-08-24" to 6,
        "2026-08-25" to 8,
        "2026-08-26" to 4,
        "2026-08-27" to 7,
    ),
    notesPerHour = mapOf(
        8 to 2,
        9 to 5,
        10 to 9,
        11 to 7,
        14 to 12,
        15 to 8,
        19 to 6,
        20 to 14,
        21 to 10,
        22 to 4,
    ),
    notesPerDayOfWeek = mapOf(
        0 to 3,
        1 to 10,
        2 to 14,
        3 to 8,
        4 to 12,
        5 to 7,
        6 to 4,
    ),
    notesPerMonth = mapOf(
        "2026-03" to 14,
        "2026-04" to 22,
        "2026-05" to 31,
        "2026-06" to 26,
        "2026-07" to 38,
        "2026-08" to 48,
    ),
    currentStreak = 7,
    longestStreak = 15,
    categoryDistribution = mapOf(
        "İş" to 18,
        "Kişisel" to 14,
        "Yazılım" to 10,
        "Fikirler" to 6,
    ),
    topTags = listOf(
        "android" to 16,
        "compose" to 12,
        "toplanti" to 9,
        "proje" to 8,
        "tasarim" to 6,
        "onemli" to 5,
    ),
    totalNoteLinks = 22,
    orphanNoteCount = 4,
    activeReminderCount = 6,
    expiredReminderCount = 1,
    locationReminderCount = 2,
    weeklyNoteCount = 11,
    weeklyCompletedTodoCount = 8,
    mostUsedTagThisWeek = "compose",
    staleNoteCount = 4,
    fullyCompletedTodoNoteCount = 3,
)

@Preview(name = "Statistics • Content (Light)", showBackground = true)
@Composable
private fun StatisticsScreenContentPreview() {
    MemCloudApplicationTheme(darkTheme = false) {
        StatisticsScreenContent(
            uiState = StatisticsUiState(
                isLoading = false,
                statistics = sampleNoteStatistics(),
            ),
            onNavigateBack = {},
            onRetry = {},
        )
    }
}

@Preview(
    name = "Statistics • Content (Dark)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun StatisticsScreenDarkPreview() {
    MemCloudApplicationTheme(darkTheme = true) {
        StatisticsScreenContent(
            uiState = StatisticsUiState(
                isLoading = false,
                statistics = sampleNoteStatistics(),
            ),
            onNavigateBack = {},
            onRetry = {},
        )
    }
}

@Preview(name = "Statistics • Loading", showBackground = true)
@Composable
private fun StatisticsScreenLoadingPreview() {
    MemCloudApplicationTheme {
        StatisticsScreenContent(
            uiState = StatisticsUiState(
                isLoading = true,
            ),
            onNavigateBack = {},
            onRetry = {},
        )
    }
}

@Preview(name = "Statistics • Error", showBackground = true)
@Composable
private fun StatisticsScreenErrorPreview() {
    MemCloudApplicationTheme {
        StatisticsScreenContent(
            uiState = StatisticsUiState(
                isLoading = false,
                error = "İstatistik verileri yüklenirken bir bağlantı hatası oluştu.",
            ),
            onNavigateBack = {},
            onRetry = {},
        )
    }
}

@Preview(name = "Statistics • Empty", showBackground = true)
@Composable
private fun StatisticsScreenEmptyPreview() {
    MemCloudApplicationTheme {
        StatisticsScreenContent(
            uiState = StatisticsUiState(
                isLoading = false,
                statistics = NoteStatistics(),
            ),
            onNavigateBack = {},
            onRetry = {},
        )
    }
}
