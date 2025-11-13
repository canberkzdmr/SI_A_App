package com.cbo.core.domain.model

/**
 * Domain model representing user gender.
 * This enum is used across the domain layer for type-safe gender representation.
 */
enum class Gender {
    MALE,
    FEMALE;

    companion object {
        /**
         * Converts a string representation to Gender enum.
         * Returns null if the string doesn't match any Gender value.
         */
        fun fromString(value: String?): Gender? {
            return value?.let {
                try {
                    valueOf(it.uppercase())
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
        }
    }
}

