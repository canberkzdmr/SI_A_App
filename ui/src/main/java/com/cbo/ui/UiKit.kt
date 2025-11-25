package com.cbo.ui

import androidx.compose.ui.graphics.toArgb

fun androidx.compose.ui.graphics.Color.toHexString(): String {
    val argb = this.toArgb()
    return "#%08x".format(argb).uppercase() // or .lowercase() if you prefer
    // Result: "#FF123456" (always 8 digits with alpha)
}