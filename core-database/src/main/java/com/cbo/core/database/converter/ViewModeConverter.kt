package com.cbo.core.database.converter

import androidx.room.TypeConverter
import com.cbo.core.domain.model.ViewMode

class ViewModeConverter {

    @TypeConverter
    fun fromViewMode(value: ViewMode): String = value.name

    @TypeConverter
    fun toViewMode(value: String): ViewMode = ViewMode.valueOf(value)
}