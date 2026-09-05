package com.cbo.core.common.validation

import androidx.annotation.StringRes

data class FieldValidation(
    val isValid: Boolean,
    val errorMessage: String? = null,
    @StringRes val errorMessageRes: Int? = null,
)
