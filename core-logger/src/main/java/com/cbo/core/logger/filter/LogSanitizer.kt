package com.cbo.core.logger.filter

/**
 * High-performance PII (Personally Identifiable Information) and Secret data sanitizer.
 * Uses fast O(1) substring checks prior to executing regexes to prevent CPU throttling.
 */
object LogSanitizer {

    // 1. Email pattern
    private val EMAIL_REGEX = Regex("""[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+""")

    // 2. Bearer / JWT Token pattern
    private val BEARER_REGEX = Regex("""(?i)\b(bearer\s+)[a-zA-Z0-9_\-\.=]+""")

    // 3. JSON Sensitive keys pattern (password, secret, cvv, pin, etc.)
    private val JSON_SENSITIVE_REGEX = Regex(
        """(?i)("?(password|token|accessToken|refreshToken|secret|cvv|pin|client_secret)"?\s*:\s*)"([^"]+)""""
    )

    // 4. Credit card patterns (13 to 19 digits)
    private val CREDIT_CARD_REGEX = Regex("""\b(?:\d[ -]*?){13,19}\b""")

    fun sanitize(message: String): String {
        if (message.isEmpty()) return message

        var result = message

        // 1. Fast-check: Email
        if (result.contains('@')) {
            result = EMAIL_REGEX.replace(result) { match ->
                val email = match.value
                val atIndex = email.indexOf('@')
                if (atIndex > 0) {
                    val name = email.substring(0, atIndex)
                    val domain = email.substring(atIndex)
                    val maskedName = if (name.length > 2) "${name.take(2)}***" else "***"
                    "$maskedName$domain"
                } else email
            }
        }

        // 2. Fast-check: Bearer / JWT token
        if (result.contains("bearer", ignoreCase = true) || result.contains("ey")) {
            result = BEARER_REGEX.replace(result, "$1[MASKED_TOKEN]")
        }

        // 3. Fast-check: JSON Sensitive Fields
        if (result.contains("pass", ignoreCase = true) ||
            result.contains("secret", ignoreCase = true) ||
            result.contains("token", ignoreCase = true) ||
            result.contains("cvv", ignoreCase = true)
        ) {
            result = JSON_SENSITIVE_REGEX.replace(result, """$1"***"""")
        }

        // 4. Fast-check: Credit Card Numbers
        if (result.any { it.isDigit() }) {
            result = CREDIT_CARD_REGEX.replace(result) { match ->
                val raw = match.value
                val digitsOnly = raw.filter { it.isDigit() }
                if (digitsOnly.length in 13..19) {
                    "****-****-****-${digitsOnly.takeLast(4)}"
                } else raw
            }
        }

        return result
    }
}
