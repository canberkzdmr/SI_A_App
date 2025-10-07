package com.cbo.core.domain.preferences

interface PreferencesRepository {
    // username
    fun setLastUserName(userName: String)
    fun getLastUserName(): String
}