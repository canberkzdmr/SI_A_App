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

    private val gson = com.google.gson.Gson()

    @TypeConverter
    fun fromTodoItemEntityList(list: List<com.cbo.core.database.entity.TodoItemEntity>?): String {
        return if (list == null) "[]" else gson.toJson(list)
    }

    @TypeConverter
    fun toTodoItemEntityList(data: String?): List<com.cbo.core.database.entity.TodoItemEntity> {
        if (data.isNullOrBlank()) return emptyList()
        val listType = object : com.google.gson.reflect.TypeToken<List<com.cbo.core.database.entity.TodoItemEntity>>() {}.type
        return gson.fromJson(data, listType) ?: emptyList()
    }
}
