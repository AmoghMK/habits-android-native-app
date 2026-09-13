package com.example.ui

import kotlinx.serialization.Serializable

sealed class Screen {
    @Serializable
    object Home

    @Serializable
    data class HabitDetails(val habitId: Long)

    @Serializable
    data class AddEditHabit(val habitId: Long? = null)
}
