package com.example.core.data.database

import android.util.Base64
import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromByteArray(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.DEFAULT)

    @TypeConverter
    fun toByteArray(encoded: String): ByteArray = Base64.decode(encoded, Base64.DEFAULT)
}