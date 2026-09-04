package com.cbo.memcloud.data.remote
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigManager @Inject constructor() {

    private val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig

    init {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 0 // For testing/immediate updates. In production, this should be higher (e.g., 3600).
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(
            mapOf(
                "min_app_version" to 1L,
                "logger_db_enabled" to true,
                "logger_min_db_level" to "DEBUG",
                "logger_retention_days" to 7L,
                "logger_max_db_size_mb" to 50L
            )
        )
    }

    suspend fun fetchAndActivate(): Boolean {
        return try {
            remoteConfig.fetchAndActivate().await()
        } catch (e: Exception) {
            com.cbo.core.logger.AppLogger.e("Fetch failed", throwable = e)
            false
        }
    }

    fun getMinAppVersion(): Long {
        return remoteConfig.getLong("min_app_version")
    }

    fun isLoggerDbEnabled(): Boolean = remoteConfig.getBoolean("logger_db_enabled")
    fun getLoggerMinDbLevel(): String = remoteConfig.getString("logger_min_db_level")
    fun getLoggerRetentionDays(): Int = remoteConfig.getLong("logger_retention_days").toInt()
    fun getLoggerMaxDbSizeMb(): Int = remoteConfig.getLong("logger_max_db_size_mb").toInt()

    fun getLoggerConfig(): com.cbo.core.logger.api.LoggerConfig {
        return com.cbo.core.logger.api.LoggerConfig(
            isDbLoggingEnabled = isLoggerDbEnabled(),
            minDbLogLevel = com.cbo.core.logger.api.LogLevel.fromString(getLoggerMinDbLevel(), com.cbo.core.logger.api.LogLevel.DEBUG),
            retentionDays = getLoggerRetentionDays(),
            maxDbSizeMb = getLoggerMaxDbSizeMb()
        )
    }
}
