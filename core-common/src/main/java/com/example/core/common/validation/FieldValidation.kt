package com.example.core.common.validation

data class FieldValidation(
    val isValid: Boolean,
    val errorMessage: String? = null
)
