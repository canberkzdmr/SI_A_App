package com.cbo.notes.domain.model

import com.cbo.notes.domain.model.TodoItem

data class Note(
    val id: Int = 0,
    val userId: Int,
    val title: String,
    val content: String,
    val category: Category? = null,
    val tags: List<Tag> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isFavorite: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val reminderTime: Long? = null,
    val reminderRepeat: ReminderRepeat = ReminderRepeat.NONE,
    val reminderPriority: ReminderPriority = ReminderPriority.DEFAULT,
    val reminderLatitude: Double? = null,
    val reminderLongitude: Double? = null,
    val reminderLocationName: String? = null,
    val reminderRadius: Float? = null,
    val isLocationReminderEnabled: Boolean = false,
    val zettelId: String? = null,
    val attachments: List<String> = emptyList(),
    val color: String? = null,
    val todos: List<TodoItem> = emptyList()
) {
    /** Returns true if this note has an active reminder set for the future or an active location reminder */
    fun hasActiveReminder(): Boolean = (reminderTime != null && reminderTime > System.currentTimeMillis()) || isLocationReminderEnabled

    /** Returns true if this note has an active location reminder */
    fun hasActiveLocationReminder(): Boolean = isLocationReminderEnabled

    /** Returns true if the reminder time has passed */
    fun isReminderExpired(): Boolean = reminderTime != null && reminderTime <= System.currentTimeMillis()

    companion object {
        /** Duration in milliseconds after which soft-deleted notes are permanently removed (7 days) */
        const val DELETION_RETENTION_PERIOD_MS = 7L * 24 * 60 * 60 * 1000

        /** Calculates the timestamp before which deleted notes should be permanently removed */
        fun getExpirationTimestamp(): Long = System.currentTimeMillis() - DELETION_RETENTION_PERIOD_MS
    }

    /** Returns the remaining time in milliseconds before this note is permanently deleted */
    fun getRemainingDeletionTime(): Long? {
        return deletedAt?.let { deleted ->
            val expirationTime = deleted + DELETION_RETENTION_PERIOD_MS
            val remaining = expirationTime - System.currentTimeMillis()
            if (remaining > 0) remaining else 0
        }
    }

    /** Returns the number of days remaining before permanent deletion */
    fun getDaysUntilPermanentDeletion(): Int? {
        return getRemainingDeletionTime()?.let { remaining ->
            (remaining / (24 * 60 * 60 * 1000)).toInt()
        }
    }
}
