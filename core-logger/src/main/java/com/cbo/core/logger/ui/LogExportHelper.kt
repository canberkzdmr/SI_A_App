package com.cbo.core.logger.ui

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.cbo.core.logger.database.LogDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Helper to export database logs to a shareable text file via Android Share Sheet.
 */
object LogExportHelper {

    private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    private val FILE_NAME_DATE_FORMAT = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    suspend fun exportAndShareLogs(context: Context, database: LogDatabase) = withContext(Dispatchers.IO) {
        val logs = database.logDao().getLogsForExport(fromTimestamp = 0L)
        if (logs.isEmpty()) return@withContext

        val logsDir = File(context.cacheDir, "logs")
        if (!logsDir.exists()) logsDir.mkdirs()

        val fileName = "app_logs_${FILE_NAME_DATE_FORMAT.format(Date())}.txt"
        val exportFile = File(logsDir, fileName)

        FileWriter(exportFile).use { writer ->
            writer.write("=== Application Logs Export (CoreLogger v${com.cbo.core.logger.AppLogger.VERSION} • ${DATE_FORMAT.format(Date())}) ===\n\n")
            for (log in logs) {
                val timeStr = DATE_FORMAT.format(Date(log.timestamp))
                writer.write("[$timeStr] [${log.level}] [${log.threadName}] ${log.tag}: ${log.message}\n")
                if (!log.metadata.isNullOrEmpty()) {
                    writer.write("    Metadata: ${log.metadata}\n")
                }
                if (!log.throwable.isNullOrEmpty()) {
                    writer.write("    Stacktrace:\n${log.throwable}\n")
                }
            }
        }

        withContext(Dispatchers.Main) {
            shareFile(context, exportFile)
        }
    }

    private fun shareFile(context: Context, file: File) {
        try {
            val authority = "${context.packageName}.logger.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Application Logs - ${file.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Application Logs").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            android.util.Log.e("LogExportHelper", "Failed to share logs", e)
        }
    }
}
