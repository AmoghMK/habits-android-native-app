package com.example.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromDurationUnit(value: DurationUnit): String {
        return value.name
    }

    @TypeConverter
    fun toDurationUnit(value: String): DurationUnit {
        return try {
            DurationUnit.valueOf(value)
        } catch (e: Exception) {
            DurationUnit.HOURS
        }
    }
}
