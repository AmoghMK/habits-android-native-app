package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay

@Composable
fun rememberCountdownText(nextDueAt: Long?): String {
    if (nextDueAt == null) return "Not started"
    
    val timeText = remember { mutableStateOf(formatRemainingTime(nextDueAt)) }
    
    LaunchedEffect(nextDueAt) {
        while (true) {
            timeText.value = formatRemainingTime(nextDueAt)
            delay(1000)
        }
    }
    
    return timeText.value
}

fun formatRemainingTime(nextDueAt: Long): String {
    val now = System.currentTimeMillis()
    if (nextDueAt <= now) return "Due now"
    
    val diffMillis = nextDueAt - now
    val seconds = diffMillis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    
    return when {
        days > 0 -> "Due in ${days}d ${hours % 24}h"
        hours > 0 -> "Due in ${hours}h ${minutes % 60}m"
        minutes > 0 -> "Due in ${minutes}m"
        else -> "Due in < 1m"
    }
}

fun formatRemainingTimeForStats(diffMillis: Long): String {
    val seconds = diffMillis / 1000
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
