package com.example.core.domain.model

data class PasswordVerifyModel(
    val passwordHash: ByteArray,
    val salt: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PasswordVerifyModel

        if (!passwordHash.contentEquals(other.passwordHash)) return false
        if (!salt.contentEquals(other.salt)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = passwordHash.contentHashCode()
        result = 31 * result + salt.contentHashCode()
        return result
    }
}
