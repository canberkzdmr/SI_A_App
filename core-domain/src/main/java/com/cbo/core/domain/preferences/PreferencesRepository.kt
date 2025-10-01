package com.cbo.core.domain.preferences

interface PreferencesRepository {
    fun setBiometricEnabled(enabled: Boolean)
    fun isBiometricEnabled(): Boolean
}