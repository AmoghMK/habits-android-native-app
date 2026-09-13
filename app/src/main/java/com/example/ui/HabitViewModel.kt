package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.IntervalsApplication
import com.example.data.DurationUnit
import com.example.data.Habit
import com.example.data.HabitCompletion
import com.example.util.AlarmScheduler
import com.example.util.NotificationHelper
import com.example.util.TimeUtils
import com.example.widget.IntervalsWidgetReceiver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.data.ThemePreference

class HabitViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as IntervalsApplication).repository
    private val userPreferencesRepository = (application as IntervalsApplication).userPreferencesRepository
    private val alarmScheduler = AlarmScheduler(application)
    private val notificationHelper = NotificationHelper(application)

    val allHabits = repository.allHabits.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val themePreference = userPreferencesRepository.themePreference.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ThemePreference.SYSTEM
    )

    fun setThemePreference(theme: ThemePreference) {
        viewModelScope.launch {
            userPreferencesRepository.setThemePreference(theme)
        }
    }

    fun toggleHabitActive(habit: Habit) {
        viewModelScope.launch {
            val isActiveNow = !habit.isActive
            var nextDueAt = habit.nextDueAt
            
            if (isActiveNow) {
                if (habit.lastCompletedAt == null) {
                    nextDueAt = System.currentTimeMillis()
                } else {
                    nextDueAt = TimeUtils.calculateNextDueAt(habit.lastCompletedAt, habit.resetAmount, habit.resetUnit)
                }
            }
            
            val updatedHabit = habit.copy(
                isActive = isActiveNow, 
                nextDueAt = nextDueAt,
                updatedAt = System.currentTimeMillis()
            )
            
            repository.updateHabit(updatedHabit)
            
            if (!updatedHabit.isActive) {
                alarmScheduler.cancelAlarm(habit.id)
                notificationHelper.cancelNotification(habit.id)
            } else if (updatedHabit.nextDueAt != null && updatedHabit.nextDueAt > System.currentTimeMillis()) {
                alarmScheduler.scheduleAlarm(habit.id, updatedHabit.nextDueAt)
            } else {
                alarmScheduler.cancelAlarm(habit.id)
            }
            IntervalsWidgetReceiver.update(getApplication())
        }
    }

    fun completeHabit(habit: Habit) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val nextDueAt = TimeUtils.calculateNextDueAt(now, habit.resetAmount, habit.resetUnit)
            
            val updatedHabit = habit.copy(
                lastCompletedAt = now,
                nextDueAt = nextDueAt,
                updatedAt = now
            )
            
            repository.updateHabit(updatedHabit)
            repository.insertCompletion(HabitCompletion(habitId = habit.id, completedAt = now))
            
            // Cancel notification if it's currently showing
            notificationHelper.cancelNotification(habit.id)
            
            // Schedule next reminder
            alarmScheduler.scheduleAlarm(habit.id, nextDueAt)
            
            IntervalsWidgetReceiver.update(getApplication())
        }
    }

    fun saveHabit(
        id: Long,
        name: String,
        icon: String?,
        amount: Int,
        unit: DurationUnit,
        resetTimer: Boolean = false // Only true if the user explicitly wants to restart an active timer
    ) {
        viewModelScope.launch {
            if (id == 0L) {
                // New habit
                val habit = Habit(
                    name = name,
                    icon = icon,
                    resetAmount = amount,
                    resetUnit = unit,
                    nextDueAt = System.currentTimeMillis()
                )
                repository.insertHabit(habit)
                IntervalsWidgetReceiver.update(getApplication())
            } else {
                // Update habit
                val existingHabit = repository.getHabitByIdSync(id)
                if (existingHabit != null) {
                    val updatedHabit = if (resetTimer) {
                        val now = System.currentTimeMillis()
                        val nextDueAt = TimeUtils.calculateNextDueAt(now, amount, unit)
                        alarmScheduler.scheduleAlarm(existingHabit.id, nextDueAt)
                        existingHabit.copy(
                            name = name,
                            icon = icon,
                            resetAmount = amount,
                            resetUnit = unit,
                            lastCompletedAt = now,
                            nextDueAt = nextDueAt,
                            updatedAt = now
                        )
                    } else if (existingHabit.lastCompletedAt != null) {
                        // Recalculate next due based on original completion but new duration
                        val nextDueAt = TimeUtils.calculateNextDueAt(existingHabit.lastCompletedAt, amount, unit)
                        alarmScheduler.scheduleAlarm(existingHabit.id, nextDueAt)
                        existingHabit.copy(
                            name = name,
                            icon = icon,
                            resetAmount = amount,
                            resetUnit = unit,
                            nextDueAt = nextDueAt,
                            updatedAt = System.currentTimeMillis()
                        )
                    } else {
                        existingHabit.copy(
                            name = name,
                            icon = icon,
                            resetAmount = amount,
                            resetUnit = unit,
                            updatedAt = System.currentTimeMillis()
                        )
                    }
                    repository.updateHabit(updatedHabit)
                    IntervalsWidgetReceiver.update(getApplication())
                }
            }
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            repository.deleteHabitById(habitId)
            alarmScheduler.cancelAlarm(habitId)
            notificationHelper.cancelNotification(habitId)
            IntervalsWidgetReceiver.update(getApplication())
        }
    }
    
    fun getHabit(id: Long): Flow<Habit?> {
        return repository.getHabitById(id)
    }

    fun getCompletions(habitId: Long): Flow<List<HabitCompletion>> {
        return repository.getCompletionsForHabit(habitId)
    }
    
    init {
        viewModelScope.launch {
            val prefs = getApplication<IntervalsApplication>().getSharedPreferences("prefs", Context.MODE_PRIVATE)
            val isFirstRun = prefs.getBoolean("is_first_run", true)
            if (isFirstRun) {
                prefs.edit().putBoolean("is_first_run", false).apply()
            }
        }
    }
}
