package com.cbo.core.common.util

/**
 * Centralized tag color palette and helpers.
 * Colors are chosen to complement the app theme (greens/teals/blues with accent warms)
 * and provided as hex strings suitable for persistence/display without UI deps.
 */
object TagColorPalette {
    /** Immutable list of hex colors (e.g., "#2A6A47"). */
    val colors: List<String> = listOf(
        // Greens / Teals (align with primary/secondary tones)
        "#2A6A47", // primaryLight
        "#3A7954",
        "#4E6354",
        "#5D7263",
        "#0B5130",
        "#38A3A5",
        "#2F8F83",
        "#45B7D1",
        // Blues (tertiary family)
        "#3B6470",
        "#224C58",
        "#118AB2",
        "#1982C4",
        // Neutral accents
        "#8D99AE",
        "#6D597A",
        // Warm accents for differentiation
        "#FFD93D",
        "#FF9F1C",
        "#FFA07A",
        "#EF476F",
        "#FF6B6B",
    )

    /**
     * Deterministically pick a color from the palette based on tag name.
     * Ensures the same uncolored tag consistently gets the same color.
     */
    fun pickForTagName(tagName: String): String {
        if (colors.isEmpty()) return "#2A6A47"
        val index = (tagName.trim().lowercase().hashCode().toLong() and 0x7FFFFFFF).toInt() % colors.size
        return colors[index]
    }

    /** Random-ish pick with an optional seed (falls back to name-based if name provided). */
    fun random(seed: Long? = null): String {
        if (colors.isEmpty()) return "#2A6A47"
        val idx = ((seed ?: System.nanoTime()) and 0x7FFFFFFF).toInt() % colors.size
        return colors[idx]
    }
}


