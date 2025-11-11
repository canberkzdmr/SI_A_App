package com.cbo.core.domain.model

data class UserSettings(
    val userId: Int,
    val isFirstLoginDone: Boolean = false,
    val isBiometricsEnabled: Boolean = false,
    val preferredLanguage: String = "en",
)