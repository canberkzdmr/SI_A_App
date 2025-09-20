package com.cbo.login.domain.model

data class User(
    val id: Int,
    val username: String,
    val password: String,
    val email: String,
    val lastPasswordChangeDate: String,
    val registerDate: String,
    val termsAndConditionsChecked: Boolean,
)