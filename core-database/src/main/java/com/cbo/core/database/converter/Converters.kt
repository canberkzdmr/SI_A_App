package com.cbo.core.database.converter

import android.util.Base64
import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromByteArray(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.DEFAULT)

    @TypeConverter
    fun toByteArray(encoded: String): ByteArray = Base64.decode(encoded, Base64.DEFAULT)

    @TypeConverter
    fun fromStringList(list: List<String>): String = list.joinToString("|||")

    @TypeConverter
    fun toStringList(data: String): List<String> = if (data.isBlank()) emptyList() else data.split("|||")
}
