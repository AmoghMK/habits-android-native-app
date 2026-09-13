package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.HabitRepository
import com.example.data.UserPreferencesRepository

class IntervalsApplication : Application() {
    lateinit var database: AppDatabase
        private set
    lateinit var repository: HabitRepository
        private set
    lateinit var userPreferencesRepository: UserPreferencesRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "intervals_database"
        )
        .addMigrations(com.example.data.MIGRATION_1_2)
        .fallbackToDestructiveMigration()
        .build()
        
        repository = HabitRepository(database.habitDao())
        userPreferencesRepository = UserPreferencesRepository(this)
    }
}
