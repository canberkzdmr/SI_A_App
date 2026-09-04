package com.cbo.core.logger

import com.cbo.core.logger.database.LogEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LogEntityTest {

    @Test
    fun `LogEntity holds correct values and sensible defaults`() {
        val entity = LogEntity(
            id = 1L,
            timestamp = 1700000000000L,
            level = "DEBUG",
            tag = "TestTag",
            message = "Test message"
        )

        assertEquals(1L, entity.id)
        assertEquals(1700000000000L, entity.timestamp)
        assertEquals("DEBUG", entity.level)
        assertEquals("TestTag", entity.tag)
        assertEquals("Test message", entity.message)
        assertNull(entity.throwable)
        assertEquals("main", entity.threadName)
        assertNull(entity.metadata)
    }

    @Test
    fun `LogEntity copy and equality work properly`() {
        val original = LogEntity(
            id = 5L,
            timestamp = 1700000000000L,
            level = "ERROR",
            tag = "ErrorTag",
            message = "Failed",
            throwable = "Exception details",
            threadName = "IO-1",
            metadata = """{"key":"val"}"""
        )

        val copy = original.copy(id = 6L)

        assertEquals(6L, copy.id)
        assertEquals("ERROR", copy.level)
        assertEquals("Exception details", copy.throwable)
        assertEquals("IO-1", copy.threadName)
        assertEquals("""{"key":"val"}""", copy.metadata)
    }
}
