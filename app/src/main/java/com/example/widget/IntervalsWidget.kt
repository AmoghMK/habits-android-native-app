package com.example.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.IntervalsApplication
import com.example.MainActivity
import com.example.data.Habit
import com.example.receiver.MarkCompleteReceiver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

import androidx.glance.appwidget.updateAll

import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.ButtonDefaults
import androidx.glance.ColorFilter

class IntervalsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = IntervalsWidget()
    
    companion object {
        suspend fun update(context: Context) {
            IntervalsWidget().updateAll(context)
        }
    }
}

class IntervalsWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as IntervalsApplication
        val repository = app.repository
        
        provideContent {
            val habits by repository.allHabits.collectAsState(initial = emptyList())
            
            GlanceTheme {
                WidgetContent(habits = habits, context = context)
            }
        }
    }

    @Composable
    private fun WidgetContent(habits: List<Habit>, context: Context) {
        val now = System.currentTimeMillis()
        
        val activeHabits = habits.filter { it.isActive }
        
        val pendingHabits = activeHabits.filter { it.nextDueAt != null && it.nextDueAt <= now }
            .sortedBy { it.nextDueAt }
        
        val upcomingHabits = activeHabits.filter { it.nextDueAt != null && it.nextDueAt > now }
            .sortedBy { it.nextDueAt }
            
        val displayHabits = pendingHabits + upcomingHabits

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .padding(16.dp)
                .clickable(actionStartActivity<MainActivity>()),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HABITS",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )
                Image(
                    provider = ImageProvider(com.example.R.drawable.ic_refresh),
                    contentDescription = "Refresh",
                    modifier = GlanceModifier
                        .width(20.dp)
                        .height(20.dp)
                        .clickable(actionRunCallback<RefreshWidgetAction>()),
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant)
                )
            }
            Spacer(modifier = GlanceModifier.height(12.dp))
            
            if (displayHabits.isNotEmpty()) {
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    items(displayHabits) { habit ->
                        val isPending = habit.nextDueAt != null && habit.nextDueAt <= now
                        HabitRow(habit = habit, isPending = isPending, context = context)
                        Spacer(modifier = GlanceModifier.height(50.dp))
                    }
                }
            } else {
                Text(
                    text = "No active habits",
                    style = TextStyle(color = GlanceTheme.colors.onSurface)
                )
            }
        }
    }

    @Composable
    private fun HabitRow(habit: Habit, isPending: Boolean, context: Context) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                val iconPrefix = habit.icon?.let { "$it " } ?: ""
                val statusPrefix = if (isPending) "⚠️ " else ""
                
                Text(
                    text = "$statusPrefix$iconPrefix${habit.name}",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                )
                
                val timeText = if (isPending) {
                    "Due now"
                } else {
                    val remainingMs = habit.nextDueAt!! - System.currentTimeMillis()
                    "Due in ${formatDuration(remainingMs)}"
                }
                
                Text(
                    text = timeText,
                    style = TextStyle(
                        color = if (isPending) GlanceTheme.colors.error else GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                )
            }
            
            Spacer(modifier = GlanceModifier.width(8.dp))
            Button(
                text = if (isPending) "✓ Done" else "✓ Early",
                onClick = actionRunCallback<CompleteHabitAction>(
                    actionParametersOf(ActionParameters.Key<Long>("habitId") to habit.id)
                ),
                colors = if (isPending) {
                    ButtonDefaults.buttonColors(
                        backgroundColor = GlanceTheme.colors.error,
                        contentColor = GlanceTheme.colors.onError
                    )
                } else {
                    ButtonDefaults.buttonColors(
                        backgroundColor = androidx.glance.color.ColorProvider(day = Color(0xFF10B981), night = Color(0xFF10B981)),
                        contentColor = androidx.glance.color.ColorProvider(day = Color.White, night = Color.White)
                    )
                }
            )
        }
    }
    
    private fun formatDuration(millis: Long): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        return when {
            days > 0 -> "${days}d ${hours % 24}h"
            hours > 0 -> "${hours}h ${minutes % 60}m"
            minutes > 0 -> "${minutes}m"
            else -> "< 1m"
        }
    }
}

class RefreshWidgetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        IntervalsWidgetReceiver.update(context)
    }
}

class CompleteHabitAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val habitId = parameters[ActionParameters.Key<Long>("habitId")] ?: return
        
        // Trigger the broadcast receiver which handles the actual completion logic
        val intent = Intent(context, MarkCompleteReceiver::class.java).apply {
            putExtra(MarkCompleteReceiver.EXTRA_HABIT_ID, habitId)
        }
        context.sendBroadcast(intent)
    }
}
