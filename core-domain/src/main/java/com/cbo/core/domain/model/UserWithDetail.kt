package com.cbo.core.domain.model

/**
 * Domain model representing a user with their complete information.
 * This aggregates User, UserDetail, and UserSettings into a single cohesive model
 * for use in the presentation layer.
 */
data class UserWithDetail(
    val user: User,
    val userDetail: UserDetail?,
    val userSettings: UserSettings,
)


