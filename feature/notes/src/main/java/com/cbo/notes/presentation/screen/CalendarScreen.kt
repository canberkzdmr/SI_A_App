package com.cbo.notes.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cbo.notes.domain.model.Note
import com.cbo.notes.presentation.viewmodel.CalendarViewModel
import com.cbo.ui.theme.MemCloudApplicationTheme
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    onNavigateToEditNote: (noteId: Int) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    CalendarScreenContent(
        uiState = uiState,
        onNavigateToEditNote = onNavigateToEditNote,
        onSelectDate = { viewModel.selectDate(it) }
    )
}

@Composable
fun CalendarScreenContent(
    uiState: com.cbo.notes.presentation.viewmodel.CalendarUiState,
    onNavigateToEditNote: (noteId: Int) -> Unit,
    onSelectDate: (LocalDate) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    val daysOfWeek = remember { daysOfWeek() }
    
    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first()
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    coroutineScope.launch {
                        state.animateScrollToMonth(YearMonth.now())
                    }
                    onSelectDate(LocalDate.now())
                },
                modifier = Modifier.padding(bottom = 70.dp) // Bottom Navigation payı
            ) {
                Icon(Icons.Default.Today, contentDescription = "Bugün")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Takvim Bölümü
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    CalendarTitle(
                        month = state.firstVisibleMonth,
                        goToPrevious = {
                            coroutineScope.launch {
                                state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.previousMonth)
                            }
                        },
                        goToNext = {
                            coroutineScope.launch {
                                state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.nextMonth)
                            }
                        }
                    )
                    
                    DaysOfWeekTitle(daysOfWeek = daysOfWeek)
                    
                    HorizontalCalendar(
                        state = state,
                        dayContent = { day ->
                            Day(
                                day = day,
                                isSelected = uiState.selectedDate == day.date,
                                notesForDay = uiState.notesByDate[day.date] ?: emptyList(),
                                onClick = { clickedDay ->
                                    if (clickedDay.position == DayPosition.MonthDate) {
                                        onSelectDate(clickedDay.date)
                                    }
                                }
                            )
                        }
                    )
                }
            }

            // Alt Kısım: Seçili Günün Notları (Zaman Tüneli / Timeline)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = uiState.selectedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (uiState.selectedDateNotes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Bu tarihte hatırlatıcı bulunmuyor.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.selectedDateNotes.sortedBy { it.reminderTime },
                            key = { it.id }
                        ) { note ->
                            NoteTimelineCard(
                                note = note,
                                onClick = { onNavigateToEditNote(note.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarTitle(
    month: CalendarMonth,
    goToPrevious: () -> Unit,
    goToNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = goToPrevious) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Önceki Ay")
        }
        
        Text(
            text = month.yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())).uppercase(Locale.getDefault()),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        IconButton(onClick = goToNext) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Sonraki Ay")
        }
    }
}

@Composable
private fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Day(
    day: CalendarDay,
    isSelected: Boolean,
    notesForDay: List<Note>,
    onClick: (CalendarDay) -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(
                enabled = day.position == DayPosition.MonthDate,
                onClick = { onClick(day) }
            )
            .padding(4.dp)
            .clip(CircleShape)
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
            ),
        contentAlignment = Alignment.Center
    ) {
        val textColor = when {
            isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
            day.position == DayPosition.MonthDate -> MaterialTheme.colorScheme.onSurface
            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = textColor,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            
            // Eğer not varsa, nokta (indicator) gösterelim (maksimum 3 adet yan yana veya üst üste)
            if (notesForDay.isNotEmpty() && day.position == DayPosition.MonthDate) {
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    notesForDay.take(3).forEach { note ->
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(getColorFromHex(note.category?.color, MaterialTheme.colorScheme.primary))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteTimelineCard(
    note: Note,
    onClick: () -> Unit
) {
    val timeFormatted = remember(note.reminderTime) {
        if (note.reminderTime != null) {
            val date = java.util.Date(note.reminderTime)
            val formatter = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())
            formatter.format(date)
        } else {
            ""
        }
    }

    val categoryColor = getColorFromHex(note.category?.color, MaterialTheme.colorScheme.primary)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Renkli Zaman İşareti
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(60.dp)
            ) {
                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = categoryColor
                )
            }
            
            // Ayıraç (Dikey Çizgi)
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .width(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(categoryColor.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))

            // Not Detayı
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (note.content.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun getColorFromHex(hexString: String?, defaultColor: Color): Color {
    if (hexString.isNullOrBlank()) return defaultColor
    return try {
        Color(android.graphics.Color.parseColor(if (!hexString.startsWith("#")) "#$hexString" else hexString))
    } catch (e: Exception) {
        defaultColor
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun NoteTimelineCardPreview() {
    MemCloudApplicationTheme {
        NoteTimelineCard(
            note = Note(
                id = 1,
                userId = 1,
                title = "Görüşme Notları",
                content = "Yeni özellikleri ekiple tartış.",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                reminderTime = System.currentTimeMillis() + 3600000,
                category = com.cbo.notes.domain.model.Category(
                    id = 1,
                    userId = 1,
                    name = "İş",
                    color = "#FF0000"
                )
            ),
            onClick = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun CalendarScreenPreview() {
    MemCloudApplicationTheme {
        CalendarScreenContent(
            uiState = com.cbo.notes.presentation.viewmodel.CalendarUiState(
                isLoading = false,
                selectedDate = LocalDate.now(),
                selectedDateNotes = listOf(
                    Note(
                        id = 1,
                        userId = 1,
                        title = "Test Notu",
                        content = "Bu bir test notudur.",
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                        reminderTime = System.currentTimeMillis() + 3600000,
                        category = com.cbo.notes.domain.model.Category(id = 1, userId = 1, name = "Kişisel", color = "#0000FF")
                    )
                )
            ),
            onNavigateToEditNote = {},
            onSelectDate = {}
        )
    }
}
