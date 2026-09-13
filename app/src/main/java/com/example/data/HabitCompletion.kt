package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "habit_completions")
data class HabitCompletion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val completedAt: Long = Instant.now().toEpochMilli()
)
