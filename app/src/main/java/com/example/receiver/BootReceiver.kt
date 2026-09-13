package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.IntervalsApplication
import com.example.util.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            val app = context.applicationContext as IntervalsApplication
            val repository = app.repository
            val alarmScheduler = AlarmScheduler(context)
            
            CoroutineScope(Dispatchers.IO).launch {
                val habits = repository.getAllHabitsSync()
                val now = System.currentTimeMillis()
                
                habits.forEach { habit ->
                    if (habit.nextDueAt != null) {
                        if (habit.nextDueAt <= now) {
                            // Already due while phone was off
                            // We can trigger the alarm receiver manually by sending a broadcast
                            alarmScheduler.triggerMissedAlarm(habit.id)
                        } else {
                            // Still active, reschedule
                            alarmScheduler.scheduleAlarm(habit.id, habit.nextDueAt)
                        }
                    }
                }
            }
        }
    }
}
