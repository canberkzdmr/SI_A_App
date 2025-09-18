package com.cbo.user.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class EditUserTest {

    @Test
    fun `EditUser creation with valid parameters should succeed`() {
        // Given
        val id = 1
        val username = "testuser"
        val password = "password123"
        val email = "test@example.com"
        val lastLoginDate = "2024-01-15"
        val registerDate = "2024-01-01"

        // When
        val editUser = EditUser(
            id = id,
            username = username,
            password = password,
            email = email,
            lastLoginDate = lastLoginDate,
            registerDate = registerDate
        )

        // Then
        assertEquals(id, editUser.id)
        assertEquals(username, editUser.username)
        assertEquals(password, editUser.password)
        assertEquals(email, editUser.email)
        assertEquals(lastLoginDate, editUser.lastLoginDate)
        assertEquals(registerDate, editUser.registerDate)
    }

    @Test
    fun `EditUser equality should work correctly`() {
        // Given
        val editUser1 = EditUser(
            id = 1,
            username = "testuser",
            password = "password123",
            email = "test@example.com",
            lastLoginDate = "2024-01-15",
            registerDate = "2024-01-01"
        )
        val editUser2 = EditUser(
            id = 1,
            username = "testuser",
            password = "password123",
            email = "test@example.com",
            lastLoginDate = "2024-01-15",
            registerDate = "2024-01-01"
        )
        val editUser3 = EditUser(
            id = 2,
            username = "testuser2",
            password = "password456",
            email = "test2@example.com",
            lastLoginDate = "2024-01-16",
            registerDate = "2024-01-02"
        )

        // Then
        assertEquals(editUser1, editUser2)
        assertNotEquals(editUser1, editUser3)
        assertEquals(editUser1.hashCode(), editUser2.hashCode())
        assertNotEquals(editUser1.hashCode(), editUser3.hashCode())
    }

    @Test
    fun `EditUser copy should work correctly`() {
        // Given
        val originalEditUser = EditUser(
            id = 1,
            username = "testuser",
            password = "password123",
            email = "test@example.com",
            lastLoginDate = "2024-01-15",
            registerDate = "2024-01-01"
        )

        // When
        val copiedEditUser = originalEditUser.copy(
            username = "newusername",
            lastLoginDate = "2024-01-20"
        )

        // Then
        assertEquals(originalEditUser.id, copiedEditUser.id)
        assertEquals("newusername", copiedEditUser.username)
        assertEquals(originalEditUser.password, copiedEditUser.password)
        assertEquals(originalEditUser.email, copiedEditUser.email)
        assertEquals("2024-01-20", copiedEditUser.lastLoginDate)
        assertEquals(originalEditUser.registerDate, copiedEditUser.registerDate)
        assertNotEquals(originalEditUser, copiedEditUser)
    }

    @Test
    fun `EditUser toString should work correctly`() {
        // Given
        val editUser = EditUser(
            id = 1,
            username = "testuser",
            password = "password123",
            email = "test@example.com",
            lastLoginDate = "2024-01-15",
            registerDate = "2024-01-01"
        )

        // When
        val editUserString = editUser.toString()

        // Then
        assert(editUserString.contains("EditUser"))
        assert(editUserString.contains("id=1"))
        assert(editUserString.contains("username=testuser"))
        assert(editUserString.contains("password=password123"))
        assert(editUserString.contains("email=test@example.com"))
        assert(editUserString.contains("lastLoginDate=2024-01-15"))
        assert(editUserString.contains("registerDate=2024-01-01"))
    }
}
