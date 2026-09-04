package com.cbo.core.logger.engine

import com.cbo.core.logger.AppLogger

/**
 * Utility to extract caller ClassName#methodName:lineNumber from the stack trace.
 */
object StackTraceHelper {

    private val IGNORED_CLASSES = setOf(
        AppLogger::class.java.name,
        AppLogger.TaggedLogger::class.java.name,
        StackTraceHelper::class.java.name,
        LoggerEngine::class.java.name
    )

    private val IGNORED_PACKAGE_PREFIXES = listOf(
        "java.lang.",
        "dalvik.system.",
        "android.util.Log",
        "kotlin."
    )

    fun resolveCallerTag(): String {
        val stackTrace = Throwable().stackTrace
        for (element in stackTrace) {
            val className = element.className
            if (className !in IGNORED_CLASSES && IGNORED_PACKAGE_PREFIXES.none { className.startsWith(it) }) {
                val simpleClassName = className.substringAfterLast('.').substringBefore('$')
                val methodName = element.methodName
                val lineNumber = element.lineNumber
                return "$simpleClassName#$methodName:$lineNumber"
            }
        }
        return "AppLogger"
    }
}
