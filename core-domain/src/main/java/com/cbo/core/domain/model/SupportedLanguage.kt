package com.cbo.core.domain.model

data class SupportedLanguage(
    val id: Int,
    val code: String,
    val displayName: String,
    val nativeName: String,
    val isEnabled: Boolean = true,
    val sortOrder: Int = 0,
)
