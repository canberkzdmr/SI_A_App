package com.cbo.core.logger

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoggerVersionTest {

    @Test
    fun `AppLogger version constants are properly defined and non-empty`() {
        assertTrue(AppLogger.VERSION.isNotBlank())
        assertTrue(AppLogger.VERSION_CODE > 0)
    }

    @Test
    fun `AppLogger version follows semantic versioning format`() {
        val semverRegex = Regex("""^\d+\.\d+\.\d+(-[a-zA-Z0-9.]+)?$""")
        assertTrue(
            "Version '${AppLogger.VERSION}' does not match Semantic Versioning format (e.g. 1.0.0 or 1.0.0-alpha)",
            semverRegex.matches(AppLogger.VERSION)
        )
    }

    @Test
    fun `AppLogger versionCode matches major minor patch calculation`() {
        val baseVersion = AppLogger.VERSION.substringBefore("-")
        val parts = baseVersion.split(".").map { it.toInt() }
        assertEquals(3, parts.size)

        val expectedCode = parts[0] * 10000 + parts[1] * 100 + parts[2]
        assertEquals(expectedCode, AppLogger.VERSION_CODE)
    }
}
