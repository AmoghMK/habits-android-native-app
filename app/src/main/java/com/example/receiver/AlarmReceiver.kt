package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.IntervalsApplication
import com.example.util.NotificationHelper
import com.example.data.Habit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1L)
        if (habitId != -1L) {
            val app = context.applicationContext as IntervalsApplication
            val repository = app.repository
            
            CoroutineScope(Dispatchers.IO).launch {
                val habit = repository.getHabitByIdSync(habitId)
                if (habit != null && habit.isActive) {
                    val now = System.currentTimeMillis()
                    // Check if it's actually due (with a 2-second grace period for clock tolerance)
                    if (habit.nextDueAt != null && habit.nextDueAt <= (now + 2000L)) {
                        NotificationHelper(context).showHabitDueNotification(habit)
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_HABIT_ID = "extra_habit_id"
    }
}
