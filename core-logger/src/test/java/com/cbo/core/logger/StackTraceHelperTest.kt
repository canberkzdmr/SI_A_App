package com.cbo.core.logger

import com.cbo.core.logger.engine.StackTraceHelper
import org.junit.Assert.assertTrue
import org.junit.Test

class StackTraceHelperTest {

    @Test
    fun `resolveCallerTag detects calling test class and method`() {
        val tag = StackTraceHelper.resolveCallerTag()

        // Should be formatted as: ClassName#methodName:lineNumber
        assertTrue(tag.contains("StackTraceHelperTest"))
        assertTrue(tag.contains("resolveCallerTag detects calling test class and method"))
        assertTrue(tag.contains(":"))
    }
}
