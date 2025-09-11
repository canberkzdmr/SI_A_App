package com.example.core.base.validation

data class FieldValidation(
    val isValid: Boolean,
    val errorMessage: String? = null
)
