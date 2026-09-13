package com.example.util

import com.example.data.DurationUnit
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

object TimeUtils {
    fun formatRelativeDate(timestamp: Long): String {
        val now = ZonedDateTime.now()
        val date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
        
        val nowDate = now.toLocalDate()
        val itemDate = date.toLocalDate()
        
        val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("h:mm a").withLocale(java.util.Locale.getDefault())
        val timeString = date.format(timeFormatter).lowercase()

        return when {
            nowDate.isEqual(itemDate) -> "Today · $timeString"
            nowDate.minusDays(1).isEqual(itemDate) -> "Yesterday · $timeString"
            else -> {
                val fullFormatter = java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a").withLocale(java.util.Locale.getDefault())
                date.format(fullFormatter)
            }
        }
    }

    fun calculateNextDueAt(
        lastCompletedAt: Long,
        amount: Int,
        unit: DurationUnit
    ): Long {
        val instant = Instant.ofEpochMilli(lastCompletedAt)
        return when (unit) {
            DurationUnit.MINUTES -> instant.plusSeconds(amount * 60L).toEpochMilli()
            DurationUnit.HOURS -> instant.plusSeconds(amount * 3600L).toEpochMilli()
            DurationUnit.DAYS -> instant.plusSeconds(amount * 86400L).toEpochMilli()
            DurationUnit.WEEKS -> instant.plusSeconds(amount * 604800L).toEpochMilli()
            // Months and years require calendar calculation
            DurationUnit.MONTHS -> {
                val zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
                zdt.plusMonths(amount.toLong()).toInstant().toEpochMilli()
            }
            DurationUnit.YEARS -> {
                val zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
                zdt.plusYears(amount.toLong()).toInstant().toEpochMilli()
            }
        }
    }
}
