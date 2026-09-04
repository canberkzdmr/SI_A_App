package com.cbo.core.logger

import android.content.Context
import com.cbo.core.logger.api.DefaultLoggerConfigProvider
import com.cbo.core.logger.api.LogLevel
import com.cbo.core.logger.api.LogSink
import com.cbo.core.logger.api.LoggerConfig
import com.cbo.core.logger.api.LoggerConfigProvider
import com.cbo.core.logger.database.LogDatabase
import com.cbo.core.logger.engine.LogBufferChannel
import com.cbo.core.logger.engine.LoggerEngine
import com.cbo.core.logger.maintenance.LogPruningManager
import com.cbo.core.logger.shortcut.LogShortcutHelper
import com.cbo.core.logger.sinks.LogcatSink
import com.cbo.core.logger.sinks.RoomDatabaseSink

/**
 * Public Facade for the logging module.
 * Accessible from any module without boilerplate injection.
 */
object AppLogger {

    /**
     * Semantic version name of the Core-Logger library module.
     */
    val VERSION: String = BuildConfig.LOGGER_VERSION_NAME

    /**
     * Numeric version code of the Core-Logger library module.
     */
    val VERSION_CODE: Int = BuildConfig.LOGGER_VERSION_CODE

    @Volatile
    private var engine: LoggerEngine? = null

    @Volatile
    private var pruningManager: LogPruningManager? = null

    @Volatile
    private var configProvider: LoggerConfigProvider? = null

    @Volatile
    private var isInitialized = false

    /**
     * Initializes the logging module with dynamic config provider.
     */
    @Synchronized
    fun init(
        context: Context,
        configProvider: LoggerConfigProvider = DefaultLoggerConfigProvider(),
        customSinks: List<LogSink>? = null
    ) {
        if (isInitialized) return

        this.configProvider = configProvider
        val appContext = context.applicationContext
        val logDatabase = LogDatabase.getInstance(appContext)
        val bufferChannel = LogBufferChannel(
            logDao = logDatabase.logDao(),
            configProvider = configProvider
        )

        val activeSinks = customSinks ?: listOf(
            LogcatSink(),
            RoomDatabaseSink(bufferChannel, configProvider)
        )

        val newPruningManager = LogPruningManager(
            context = appContext,
            database = logDatabase,
            configProvider = configProvider
        )
        pruningManager = newPruningManager
        newPruningManager.performMaintenanceAsync()

        engine = LoggerEngine(
            sinks = activeSinks,
            configProvider = configProvider
        )

        if (configProvider.enableShortcutOnDebug.value) {
            LogShortcutHelper.setupLogShortcut(appContext)
        }

        isInitialized = true
    }

    /**
     * Convenience initialization with static or default configuration.
     */
    fun init(context: Context, config: LoggerConfig) {
        init(context, DefaultLoggerConfigProvider(config))
    }

    /**
     * Dynamically updates runtime configuration (e.g. after Firebase Remote Config fetch).
     */
    fun updateConfig(config: LoggerConfig) {
        val provider = configProvider
        if (provider is DefaultLoggerConfigProvider) {
            provider.updateConfig(config)
        }
    }

    fun getPruningManager(): LogPruningManager? = pruningManager

    // ==========================================
    // Core Lazy Logging Functions (Lambda based)
    // ==========================================

    fun v(
        tag: String? = null,
        throwable: Throwable? = null,
        metadata: Map<String, Any?>? = null,
        message: () -> String
    ) = log(LogLevel.VERBOSE, tag, message, throwable, metadata)

    fun d(
        tag: String? = null,
        throwable: Throwable? = null,
        metadata: Map<String, Any?>? = null,
        message: () -> String
    ) = log(LogLevel.DEBUG, tag, message, throwable, metadata)

    fun i(
        tag: String? = null,
        throwable: Throwable? = null,
        metadata: Map<String, Any?>? = null,
        message: () -> String
    ) = log(LogLevel.INFO, tag, message, throwable, metadata)

    fun w(
        tag: String? = null,
        throwable: Throwable? = null,
        metadata: Map<String, Any?>? = null,
        message: () -> String
    ) = log(LogLevel.WARN, tag, message, throwable, metadata)

    fun e(
        tag: String? = null,
        throwable: Throwable? = null,
        metadata: Map<String, Any?>? = null,
        message: () -> String
    ) = log(LogLevel.ERROR, tag, message, throwable, metadata)

    // ==========================================
    // Direct String Overloads
    // ==========================================

    fun v(message: String, tag: String? = null, throwable: Throwable? = null) {
        log(LogLevel.VERBOSE, tag, { message }, throwable, null)
    }

    fun d(message: String, tag: String? = null, throwable: Throwable? = null) {
        log(LogLevel.DEBUG, tag, { message }, throwable, null)
    }

    fun i(message: String, tag: String? = null, throwable: Throwable? = null) {
        log(LogLevel.INFO, tag, { message }, throwable, null)
    }

    fun w(message: String, throwable: Throwable? = null, tag: String? = null) {
        log(LogLevel.WARN, tag, { message }, throwable, null)
    }

    fun e(message: String, throwable: Throwable? = null, tag: String? = null) {
        log(LogLevel.ERROR, tag, { message }, throwable, null)
    }

    fun e(throwable: Throwable, tag: String? = null, message: String? = null) {
        val msg = message ?: throwable.message ?: "An exception occurred"
        log(LogLevel.ERROR, tag, { msg }, throwable, null)
    }

    // ==========================================
    // Tagged Builder Pattern
    // ==========================================

    fun tag(customTag: String): TaggedLogger = TaggedLogger(customTag)

    class TaggedLogger(private val customTag: String) {
        fun v(throwable: Throwable? = null, message: () -> String) =
            AppLogger.log(LogLevel.VERBOSE, customTag, message, throwable, null)

        fun d(throwable: Throwable? = null, message: () -> String) =
            AppLogger.log(LogLevel.DEBUG, customTag, message, throwable, null)

        fun i(throwable: Throwable? = null, message: () -> String) =
            AppLogger.log(LogLevel.INFO, customTag, message, throwable, null)

        fun w(throwable: Throwable? = null, message: () -> String) =
            AppLogger.log(LogLevel.WARN, customTag, message, throwable, null)

        fun e(throwable: Throwable? = null, message: () -> String) =
            AppLogger.log(LogLevel.ERROR, customTag, message, throwable, null)

        fun d(message: String, throwable: Throwable? = null) =
            AppLogger.d(message, customTag, throwable)

        fun e(message: String, throwable: Throwable? = null) =
            AppLogger.e(message, throwable, customTag)
    }

    // ==========================================
    // Internal Dispatcher
    // ==========================================

    fun log(
        level: LogLevel,
        tag: String?,
        messageProducer: () -> String,
        throwable: Throwable?,
        metadata: Map<String, Any?>?
    ) {
        val activeEngine = engine
        if (activeEngine != null) {
            activeEngine.log(level, tag, messageProducer, throwable, metadata)
        } else {
            // Fallback to Logcat if called before AppLogger.init()
            val fallbackTag = tag ?: "AppLogger"
            val msg = messageProducer()
            when (level) {
                LogLevel.VERBOSE -> android.util.Log.v(fallbackTag, msg, throwable)
                LogLevel.DEBUG -> android.util.Log.d(fallbackTag, msg, throwable)
                LogLevel.INFO -> android.util.Log.i(fallbackTag, msg, throwable)
                LogLevel.WARN -> android.util.Log.w(fallbackTag, msg, throwable)
                LogLevel.ERROR -> android.util.Log.e(fallbackTag, msg, throwable)
                LogLevel.NONE -> {}
            }
        }
    }

    fun flush() {
        engine?.flush()
    }
}
