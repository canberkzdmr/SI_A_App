package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username", "email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val passwordHash: ByteArray,
    val salt: ByteArray,
    val email: String,
    val registrationDate: String,
    val lastPasswordChangeDate: String,
    val isActive: Boolean,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserEntity

        if (id != other.id) return false
        if (username != other.username) return false
        if (!passwordHash.contentEquals(other.passwordHash)) return false
        if (!salt.contentEquals(other.salt)) return false
        if (email != other.email) return false
        if (registrationDate != other.registrationDate) return false
        if (lastPasswordChangeDate != other.lastPasswordChangeDate) return false
        if (isActive != other.isActive) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + username.hashCode()
        result = 31 * result + passwordHash.contentHashCode()
        result = 31 * result + salt.contentHashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + registrationDate.hashCode()
        result = 31 * result + lastPasswordChangeDate.hashCode()
        result = 31 * result + isActive.hashCode()
        return result
    }
}
