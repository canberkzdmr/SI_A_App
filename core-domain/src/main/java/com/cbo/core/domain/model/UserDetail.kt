package com.cbo.core.domain.model

/**
 * Domain model representing user detail information.
 * This model abstracts away database-specific representations and provides
 * a clean interface for the presentation layer.
 */
data class UserDetail(
    val id: Int?,
    val userId: Int,
    val fullName: String?,
    val avatarUrl: String?,
    val phoneNumber: String?,
    val address: String?,
    val bio: String?,
    val dateOfBirth: Long?, // Stored as epoch millis for easy date picker integration
    val gender: Gender?,
)

