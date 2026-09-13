package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

enum class DurationUnit {
    MINUTES, HOURS, DAYS, WEEKS, MONTHS, YEARS;
    
    fun getPluralLabel(amount: Int): String {
        return if (amount == 1) {
            this.name.lowercase().dropLast(1)
        } else {
            this.name.lowercase()
        }
    }
}

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String?,
    val resetAmount: Int,
    val resetUnit: DurationUnit,
    val createdAt: Long = Instant.now().toEpochMilli(),
    val updatedAt: Long = Instant.now().toEpochMilli(),
    val lastCompletedAt: Long? = null,
    val nextDueAt: Long? = null,
    @ColumnInfo(defaultValue = "1") val isActive: Boolean = true
)
