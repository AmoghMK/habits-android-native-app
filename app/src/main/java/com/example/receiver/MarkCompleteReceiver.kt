package com.example.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.IntervalsApplication
import com.example.data.HabitCompletion
import com.example.util.AlarmScheduler
import com.example.util.TimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MarkCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1L)
        if (habitId != -1L) {
            val app = context.applicationContext as IntervalsApplication
            val repository = app.repository
            val alarmScheduler = AlarmScheduler(context)
            
            // Cancel the notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(habitId.toInt())
            
            CoroutineScope(Dispatchers.IO).launch {
                val habit = repository.getHabitByIdSync(habitId)
                if (habit != null) {
                    val now = System.currentTimeMillis()
                    val nextDueAt = TimeUtils.calculateNextDueAt(now, habit.resetAmount, habit.resetUnit)
                    
                    val updatedHabit = habit.copy(
                        lastCompletedAt = now,
                        nextDueAt = nextDueAt,
                        updatedAt = now
                    )
                    
                    repository.updateHabit(updatedHabit)
                    repository.insertCompletion(
                        HabitCompletion(habitId = habitId, completedAt = now)
                    )
                    
                    alarmScheduler.scheduleAlarm(habitId, nextDueAt)
                    
                    com.example.widget.IntervalsWidgetReceiver.update(context)
                }
            }
        }
    }

    companion object {
        const val EXTRA_HABIT_ID = "extra_habit_id"
    }
}
