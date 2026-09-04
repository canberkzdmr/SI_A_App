package com.cbo.core.logger.api

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Immutable configuration snapshot for the logger module.
 */
data class LoggerConfig(
    val isDbLoggingEnabled: Boolean = true,
    val minDbLogLevel: LogLevel = LogLevel.DEBUG,
    val retentionDays: Int = 7,
    val maxDbSizeMb: Int = 50,
    val batchIntervalMs: Long = 2000L,
    val batchSize: Int = 50,
    val isSanitizationEnabled: Boolean = true,
    val enableShortcutOnDebug: Boolean = true
)

/**
 * Provider interface to dynamically supply configuration updates (e.g. from Firebase Remote Config).
 * Using StateFlow allows reactive updates without restarting the application.
 */
interface LoggerConfigProvider {
    val isDbLoggingEnabled: StateFlow<Boolean>
    val minDbLogLevel: StateFlow<LogLevel>
    val retentionDays: StateFlow<Int>
    val maxDbSizeMb: StateFlow<Int>
    val batchIntervalMs: StateFlow<Long>
    val batchSize: StateFlow<Int>
    val isSanitizationEnabled: StateFlow<Boolean>
    val enableShortcutOnDebug: StateFlow<Boolean>
}

/**
 * Default implementation of LoggerConfigProvider with fixed or programmatically updatable values.
 */
class DefaultLoggerConfigProvider(
    initialConfig: LoggerConfig = LoggerConfig()
) : LoggerConfigProvider {

    private val _isDbLoggingEnabled = MutableStateFlow(initialConfig.isDbLoggingEnabled)
    override val isDbLoggingEnabled: StateFlow<Boolean> = _isDbLoggingEnabled.asStateFlow()

    private val _minDbLogLevel = MutableStateFlow(initialConfig.minDbLogLevel)
    override val minDbLogLevel: StateFlow<LogLevel> = _minDbLogLevel.asStateFlow()

    private val _retentionDays = MutableStateFlow(initialConfig.retentionDays)
    override val retentionDays: StateFlow<Int> = _retentionDays.asStateFlow()

    private val _maxDbSizeMb = MutableStateFlow(initialConfig.maxDbSizeMb)
    override val maxDbSizeMb: StateFlow<Int> = _maxDbSizeMb.asStateFlow()

    private val _batchIntervalMs = MutableStateFlow(initialConfig.batchIntervalMs)
    override val batchIntervalMs: StateFlow<Long> = _batchIntervalMs.asStateFlow()

    private val _batchSize = MutableStateFlow(initialConfig.batchSize)
    override val batchSize: StateFlow<Int> = _batchSize.asStateFlow()

    private val _isSanitizationEnabled = MutableStateFlow(initialConfig.isSanitizationEnabled)
    override val isSanitizationEnabled: StateFlow<Boolean> = _isSanitizationEnabled.asStateFlow()

    private val _enableShortcutOnDebug = MutableStateFlow(initialConfig.enableShortcutOnDebug)
    override val enableShortcutOnDebug: StateFlow<Boolean> = _enableShortcutOnDebug.asStateFlow()

    fun updateConfig(config: LoggerConfig) {
        _isDbLoggingEnabled.value = config.isDbLoggingEnabled
        _minDbLogLevel.value = config.minDbLogLevel
        _retentionDays.value = config.retentionDays
        _maxDbSizeMb.value = config.maxDbSizeMb
        _batchIntervalMs.value = config.batchIntervalMs
        _batchSize.value = config.batchSize
        _isSanitizationEnabled.value = config.isSanitizationEnabled
        _enableShortcutOnDebug.value = config.enableShortcutOnDebug
    }
}
