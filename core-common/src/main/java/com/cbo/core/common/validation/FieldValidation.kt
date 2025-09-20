package com.cbo.core.common.validation

data class FieldValidation(
    val isValid: Boolean,
    val errorMessage: String? = null
)
