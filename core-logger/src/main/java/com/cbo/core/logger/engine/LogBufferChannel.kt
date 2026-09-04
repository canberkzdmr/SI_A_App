package com.cbo.core.logger.engine

import com.cbo.core.logger.api.LoggerConfigProvider
import com.cbo.core.logger.database.LogDao
import com.cbo.core.logger.database.LogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Producer-Consumer batch buffer for Room log entries.
 * Logs are accumulated in-memory and flushed to SQLite asynchronously.
 */
class LogBufferChannel(
    private val logDao: LogDao,
    private val configProvider: LoggerConfigProvider,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {

    private val channel = Channel<LogEntity>(capacity = 1000)
    private val batchMutex = Mutex()
    private val pendingBatch = mutableListOf<LogEntity>()
    private var consumerJob: Job? = null

    init {
        startConsumer()
    }

    private fun startConsumer() {
        consumerJob = scope.launch {
            var lastFlushTime = System.currentTimeMillis()

            while (isActive) {
                // Poll from channel with timeout to ensure interval flush
                val item = try {
                    channel.tryReceive().getOrNull()
                } catch (e: Exception) {
                    null
                }

                if (item != null) {
                    batchMutex.withLock {
                        pendingBatch.add(item)
                    }

                    // Urgent flush on ERROR
                    if (item.level.equals("ERROR", ignoreCase = true)) {
                        flushInternal()
                        lastFlushTime = System.currentTimeMillis()
                        continue
                    }
                }

                val now = System.currentTimeMillis()
                val batchSizeLimit = configProvider.batchSize.value
                val intervalLimit = configProvider.batchIntervalMs.value

                val shouldFlushBySize = pendingBatch.size >= batchSizeLimit
                val shouldFlushByTime = (now - lastFlushTime >= intervalLimit) && pendingBatch.isNotEmpty()

                if (shouldFlushBySize || shouldFlushByTime) {
                    flushInternal()
                    lastFlushTime = now
                }

                if (item == null) {
                    delay(100L)
                }
            }
        }
    }

    fun enqueue(log: LogEntity) {
        // Offer to channel, if full drop gracefully to prevent memory starvation
        channel.trySend(log)
    }

    fun flush() {
        scope.launch {
            flushInternal()
        }
    }

    private suspend fun flushInternal() {
        val itemsToWrite: List<LogEntity>
        batchMutex.withLock {
            if (pendingBatch.isEmpty()) return
            itemsToWrite = pendingBatch.toList()
            pendingBatch.clear()
        }

        try {
            logDao.insertBatch(itemsToWrite)
        } catch (e: Exception) {
            // Room write error should never crash host app
            android.util.Log.e("LogBufferChannel", "Failed to write batch logs to DB", e)
        }
    }
}
