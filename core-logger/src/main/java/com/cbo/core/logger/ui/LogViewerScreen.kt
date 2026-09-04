package com.cbo.core.logger.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cbo.core.logger.AppLogger
import com.cbo.core.logger.database.LogDatabase
import com.cbo.core.logger.database.LogEntity
import com.cbo.core.logger.maintenance.LogPruningManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogViewerScreen(
    database: LogDatabase,
    pruningManager: LogPruningManager? = null,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf<String?>(null) }
    var selectedLogForDetail by remember { mutableStateOf<LogEntity?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }
    var dbSizeBytes by remember { mutableStateOf(0L) }
    var isExporting by remember { mutableStateOf(false) }

    // Update DB size
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            dbSizeBytes = pruningManager?.getDatabaseSizeBytes() ?: 0L
        }
    }

    val logsFlow = remember(searchQuery, selectedLevel) {
        val queryParam = searchQuery.trim().ifEmpty { null }
        val levelParam = selectedLevel
        database.logDao().getFilteredLogs(query = queryParam, level = levelParam, limit = 500)
    }
    val logs by logsFlow.collectAsState(initial = emptyList())

    LogViewerContent(
        logs = logs,
        dbSizeBytes = dbSizeBytes,
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        selectedLevel = selectedLevel,
        onLevelSelect = { selectedLevel = it },
        isExporting = isExporting,
        onExportClick = {
            isExporting = true
            coroutineScope.launch {
                LogExportHelper.exportAndShareLogs(context, database)
                isExporting = false
            }
        },
        onClearClick = { showClearDialog = true },
        onLogClick = { selectedLogForDetail = it },
        onClose = onClose
    )

    // Detail Bottom Sheet
    selectedLogForDetail?.let { log ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { selectedLogForDetail = null },
            sheetState = sheetState
        ) {
            LogDetailSheet(
                log = log,
                onCopy = { text ->
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Log Detail", text))
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // Clear Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Logs?") },
            text = { Text("This will delete all saved logs from the local database.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            database.logDao().clearAll()
                            dbSizeBytes = pruningManager?.getDatabaseSizeBytes() ?: 0L
                            showClearDialog = false
                        }
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogViewerContent(
    logs: List<LogEntity>,
    dbSizeBytes: Long,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedLevel: String?,
    onLevelSelect: (String?) -> Unit,
    isExporting: Boolean,
    onExportClick: () -> Unit,
    onClearClick: () -> Unit,
    onLogClick: (LogEntity) -> Unit,
    onClose: () -> Unit
) {
    val filterLevels = listOf("ALL", "VERBOSE", "DEBUG", "INFO", "WARN", "ERROR")

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Close"
                        )
                    }
                },
                title = {
                    Column {
                        Text(
                            text = "Application Logs",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        val sizeMb = dbSizeBytes / (1024.0 * 1024.0)
                        Text(
                            text = "${logs.size} logs | ${String.format(Locale.US, "%.2f", sizeMb)} MB • v${AppLogger.VERSION}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    if (isExporting) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = onExportClick) {
                            Icon(Icons.Default.Share, contentDescription = "Export & Share")
                        }
                    }
                    IconButton(onClick = onClearClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear Logs")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                placeholder = { Text("Search tag, message, or error...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // Level Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                filterLevels.forEach { level ->
                    val isSelected = (level == "ALL" && selectedLevel == null) || (selectedLevel == level)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            onLevelSelect(if (level == "ALL") null else level)
                        },
                        label = { Text(level, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = getLevelColor(level).copy(alpha = 0.2f),
                            selectedLabelColor = getLevelColor(level)
                        )
                    )
                }
            }

            // Log List
            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No logs found",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(logs, key = { it.id }) { log ->
                        LogItemCard(
                            log = log,
                            onClick = { onLogClick(log) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LogItemCard(log: LogEntity, onClick: () -> Unit) {
    val levelColor = getLevelColor(log.level)
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()) }
    val formattedTime = remember(log.timestamp) { timeFormat.format(Date(log.timestamp)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(levelColor)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = log.level.take(1),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = log.tag,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = formattedTime,
                    maxLines = 1,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = log.message,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (!log.throwable.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "⚠️ Has Stacktrace",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun LogDetailSheet(log: LogEntity, onCopy: (String) -> Unit) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()) }
    val formattedDate = remember(log.timestamp) { dateFormat.format(Date(log.timestamp)) }
    val levelColor = getLevelColor(log.level)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(levelColor)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = log.level,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            TextButton(
                onClick = {
                    val fullText = "[$formattedDate] [${log.level}] [${log.threadName}] ${log.tag}\n${log.message}\n${log.throwable.orEmpty()}"
                    onCopy(fullText)
                }
            ) {
                Text("Copy Full Log")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text("Tag: ${log.tag}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text("Time: $formattedDate", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Thread: ${log.threadName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(12.dp))

        Text("Message:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = log.message,
                modifier = Modifier.padding(8.dp),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        if (!log.metadata.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Metadata:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = log.metadata,
                    modifier = Modifier.padding(8.dp),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        if (!log.throwable.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Stacktrace:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = log.throwable,
                    modifier = Modifier.padding(8.dp),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

fun getLevelColor(level: String): Color {
    return when (level.uppercase()) {
        "VERBOSE" -> Color(0xFF9E9E9E)
        "DEBUG" -> Color(0xFF2196F3)
        "INFO" -> Color(0xFF4CAF50)
        "WARN" -> Color(0xFFFF9800)
        "ERROR" -> Color(0xFFF44336)
        else -> Color(0xFF607D8B)
    }
}

// ==========================================
// Previews & Sample Mock Data
// ==========================================

private val PreviewSampleLogs = listOf(
    LogEntity(
        id = 1,
        timestamp = 1725488400000L,
        level = "ERROR",
        tag = "NotesViewModel#loadNotes:124",
        message = "Failed to sync remote notes: Network timeout after 15000ms",
        throwable = "java.net.SocketTimeoutException: timeout\n\tat okhttp3.internal.http2.Http2Stream.waitForIo(Http2Stream.kt:605)\n\tat okhttp3.internal.http2.Http2Stream.takeHeaders(Http2Stream.kt:143)",
        threadName = "DefaultDispatcher-worker-1",
        metadata = "{\"retryCount\": 3, \"userId\": \"user_9921\"}"
    ),
    LogEntity(
        id = 2,
        timestamp = 1725488340000L,
        level = "WARN",
        tag = "LogPruningManager#checkDatabaseSize:52",
        message = "Log database size is approaching threshold: 42.8 MB / 50.0 MB",
        threadName = "IO-worker-2"
    ),
    LogEntity(
        id = 3,
        timestamp = 1725488280000L,
        level = "INFO",
        tag = "RemoteConfigManager#fetchAndActivate:77",
        message = "Remote config fetched successfully: logger_db_enabled=true, max_size=50",
        threadName = "main"
    ),
    LogEntity(
        id = 4,
        timestamp = 1725488220000L,
        level = "DEBUG",
        tag = "NotesRepositoryImpl#getNotes:38",
        message = "Loaded 42 active notes from Room database in 12ms",
        threadName = "DefaultDispatcher-worker-2"
    ),
    LogEntity(
        id = 5,
        timestamp = 1725488160000L,
        level = "VERBOSE",
        tag = "LogBufferChannel#emit:64",
        message = "Enqueued log entity into memory channel buffer",
        threadName = "main"
    )
)

@Preview(name = "Log Viewer Screen • Light Mode", showBackground = true)
@Composable
private fun LogViewerScreenPreviewLight() {
    MaterialTheme {
        LogViewerContent(
            logs = PreviewSampleLogs,
            dbSizeBytes = 1024L * 1024L * 12L,
            searchQuery = "",
            onSearchQueryChange = {},
            selectedLevel = null,
            onLevelSelect = {},
            isExporting = false,
            onExportClick = {},
            onClearClick = {},
            onLogClick = {},
            onClose = {}
        )
    }
}

@Preview(
    name = "Log Viewer Screen • Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun LogViewerScreenPreviewDark() {
    MaterialTheme {
        LogViewerContent(
            logs = PreviewSampleLogs,
            dbSizeBytes = 1024L * 1024L * 12L,
            searchQuery = "",
            onSearchQueryChange = {},
            selectedLevel = null,
            onLevelSelect = {},
            isExporting = false,
            onExportClick = {},
            onClearClick = {},
            onLogClick = {},
            onClose = {}
        )
    }
}

@Preview(name = "Log Viewer Screen • Empty State", showBackground = true)
@Composable
private fun LogViewerScreenPreviewEmpty() {
    MaterialTheme {
        LogViewerContent(
            logs = emptyList(),
            dbSizeBytes = 0L,
            searchQuery = "",
            onSearchQueryChange = {},
            selectedLevel = null,
            onLevelSelect = {},
            isExporting = false,
            onExportClick = {},
            onClearClick = {},
            onLogClick = {},
            onClose = {}
        )
    }
}

@Preview(name = "Log Item Card • Preview", showBackground = true)
@Composable
private fun LogItemCardPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LogItemCard(log = PreviewSampleLogs[0], onClick = {})
            LogItemCard(log = PreviewSampleLogs[3], onClick = {})
        }
    }
}

@Preview(name = "Log Detail Sheet • Preview", showBackground = true)
@Composable
private fun LogDetailSheetPreview() {
    MaterialTheme {
        Surface {
            LogDetailSheet(
                log = PreviewSampleLogs[0],
                onCopy = {}
            )
        }
    }
}
