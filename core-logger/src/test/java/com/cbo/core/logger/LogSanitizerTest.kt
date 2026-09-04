package com.cbo.core.logger

import com.cbo.core.logger.filter.LogSanitizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LogSanitizerTest {

    @Test
    fun `sanitize masks email address`() {
        val input = "User logged in with email: john.doe@example.com"
        val output = LogSanitizer.sanitize(input)

        assertFalse(output.contains("john.doe@example.com"))
        assertTrue(output.contains("jo***@example.com"))
    }

    @Test
    fun `sanitize masks bearer token`() {
        val input = "Header: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.xyz"
        val output = LogSanitizer.sanitize(input)

        assertFalse(output.contains("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.xyz"))
        assertTrue(output.contains("Bearer [MASKED_TOKEN]"))
    }

    @Test
    fun `sanitize masks json password`() {
        val input = """{"username": "admin", "password": "superSecret123!"}"""
        val output = LogSanitizer.sanitize(input)

        assertFalse(output.contains("superSecret123!"))
        assertTrue(output.contains(""""password": "***""""))
    }

    @Test
    fun `sanitize masks credit card number`() {
        val input = "Payment processed with card 4111 2222 3333 4567"
        val output = LogSanitizer.sanitize(input)

        assertFalse(output.contains("4111 2222 3333 4567"))
        assertTrue(output.contains("****-****-****-4567"))
    }

    @Test
    fun `sanitize preserves normal message`() {
        val input = "Notes loaded successfully: 15 items"
        val output = LogSanitizer.sanitize(input)

        assertEquals(input, output)
    }
}
