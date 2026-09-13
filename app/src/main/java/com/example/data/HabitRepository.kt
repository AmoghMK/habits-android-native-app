package com.example.data

import kotlinx.coroutines.flow.Flow

class HabitRepository(private val habitDao: HabitDao) {
    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()

    suspend fun getAllHabitsSync(): List<Habit> = habitDao.getAllHabitsSync()

    fun getHabitById(id: Long): Flow<Habit?> = habitDao.getHabitById(id)
    
    suspend fun getHabitByIdSync(id: Long): Habit? = habitDao.getHabitByIdSync(id)

    suspend fun insertHabit(habit: Habit): Long = habitDao.insertHabit(habit)

    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)

    suspend fun deleteHabitById(id: Long) {
        habitDao.deleteHabitById(id)
        habitDao.deleteCompletionsForHabit(id)
    }

    suspend fun insertCompletion(completion: HabitCompletion) = habitDao.insertCompletion(completion)

    fun getCompletionsForHabit(habitId: Long): Flow<List<HabitCompletion>> = habitDao.getCompletionsForHabit(habitId)
}
