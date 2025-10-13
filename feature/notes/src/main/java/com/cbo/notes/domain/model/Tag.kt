package com.cbo.notes.domain.model

import com.cbo.core.domain.FieldValidationRules

data class Tag(
    val id: Int = 0,
    val userId: Int,
    val name: String,
    val color: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val usageCount: Int = 0
)
