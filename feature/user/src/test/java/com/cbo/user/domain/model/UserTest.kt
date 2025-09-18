package com.cbo.user.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class UserTest {

    @Test
    fun `User creation with valid parameters should succeed`() {
        // Given
        val id = 1
        val username = "testuser"
        val password = "password123"
        val email = "test@example.com"

        // When
        val user = User(
            id = id,
            username = username,
            password = password,
            email = email
        )

        // Then
        assertEquals(id, user.id)
        assertEquals(username, user.username)
        assertEquals(password, user.password)
        assertEquals(email, user.email)
    }

    @Test
    fun `User equality should work correctly`() {
        // Given
        val user1 = User(
            id = 1,
            username = "testuser",
            password = "password123",
            email = "test@example.com"
        )
        val user2 = User(
            id = 1,
            username = "testuser",
            password = "password123",
            email = "test@example.com"
        )
        val user3 = User(
            id = 2,
            username = "testuser2",
            password = "password456",
            email = "test2@example.com"
        )

        // Then
        assertEquals(user1, user2)
        assertNotEquals(user1, user3)
        assertEquals(user1.hashCode(), user2.hashCode())
        assertNotEquals(user1.hashCode(), user3.hashCode())
    }

    @Test
    fun `User copy should work correctly`() {
        // Given
        val originalUser = User(
            id = 1,
            username = "testuser",
            password = "password123",
            email = "test@example.com"
        )

        // When
        val copiedUser = originalUser.copy(username = "newusername")

        // Then
        assertEquals(originalUser.id, copiedUser.id)
        assertEquals("newusername", copiedUser.username)
        assertEquals(originalUser.password, copiedUser.password)
        assertEquals(originalUser.email, copiedUser.email)
        assertNotEquals(originalUser, copiedUser)
    }

    @Test
    fun `User toString should work correctly`() {
        // Given
        val user = User(
            id = 1,
            username = "testuser",
            password = "password123",
            email = "test@example.com"
        )

        // When
        val userString = user.toString()

        // Then
        assert(userString.contains("User"))
        assert(userString.contains("id=1"))
        assert(userString.contains("username=testuser"))
        assert(userString.contains("password=password123"))
        assert(userString.contains("email=test@example.com"))
    }
}
