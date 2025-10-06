package com.cbo.core.domain.exception

sealed class UserSettingsException(
    message: String,
): Exception(message) {
    class UserSettingsNotFoundException: UserSettingsException("Failed to get user settings")
}