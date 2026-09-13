package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.Habit
import com.example.receiver.MarkCompleteReceiver

class NotificationHelper(private val context: Context) {
    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Habit Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for habits that are due"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showHabitDueNotification(habit: Habit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            habit.id.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val markCompleteIntent = Intent(context, MarkCompleteReceiver::class.java).apply {
            putExtra(MarkCompleteReceiver.EXTRA_HABIT_ID, habit.id)
        }
        val markCompletePendingIntent = PendingIntent.getBroadcast(
            context,
            habit.id.toInt(),
            markCompleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (habit.icon != null) "${habit.icon} ${habit.name} are due" else "${habit.name} is due"
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Better to use a specific notification icon
            .setContentTitle(title)
            .setContentText("It's time to complete this habit.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(0, "Mark Complete", markCompletePendingIntent)
            .build()

        notificationManager.notify(habit.id.toInt(), notification)
    }

    fun cancelNotification(habitId: Long) {
        notificationManager.cancel(habitId.toInt())
    }

    companion object {
        const val CHANNEL_ID = "habit_reminders"
    }
}
