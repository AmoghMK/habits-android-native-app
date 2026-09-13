package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailsScreen(
    viewModel: HabitViewModel,
    habitId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    val habit by viewModel.getHabit(habitId).collectAsStateWithLifecycle()
    val completions by viewModel.getCompletions(habitId).collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (habit == null) {
        return // Wait for load or it was deleted
    }

    val currentHabit = habit!!
    val isPending = currentHabit.isActive && currentHabit.nextDueAt != null && currentHabit.nextDueAt!! <= System.currentTimeMillis()
    val timeText = rememberCountdownText(currentHabit.nextDueAt)

    val cardContentAlpha = if (currentHabit.isActive) 1f else 0.5f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentHabit.name) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(habitId) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (!currentHabit.isActive) {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        } else if (isPending) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isPending) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val iconPrefix = currentHabit.icon?.let { "$it " } ?: ""
                        Text(
                            text = "$iconPrefix${currentHabit.name}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isPending) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = cardContentAlpha)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isPending) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f * cardContentAlpha)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Repeats every ${currentHabit.resetAmount} ${currentHabit.resetUnit.getPluralLabel(currentHabit.resetAmount)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isPending) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f * cardContentAlpha)
                            )
                        }
                        
                        if (currentHabit.isActive) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = timeText,
                                style = MaterialTheme.typography.titleLarge,
                                color = if (isPending) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Paused",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        
                        if (currentHabit.isActive) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.completeHabit(currentHabit) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isPending) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = if (isPending) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isPending) "Mark Complete" else "Complete Early")
                            }
                        }
                    }
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Active Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (currentHabit.isActive) "Habit is active and tracking" else "Habit is paused",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = currentHabit.isActive,
                        onCheckedChange = { viewModel.toggleHabitActive(currentHabit) }
                    )
                }
            }
            
            item {
                Text("Statistics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatCard("Total Completions", "${completions.size}", Modifier.fillMaxWidth())
                }
                
                if (completions.size >= 2) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val sortedComps = completions.sortedBy { it.completedAt }
                    var totalInterval = 0L
                    for (i in 1 until sortedComps.size) {
                        totalInterval += (sortedComps[i].completedAt - sortedComps[i-1].completedAt)
                    }
                    val avgInterval = totalInterval / (sortedComps.size - 1)
                    StatCard("Average Time Between Completions", formatRemainingTimeForStats(avgInterval), Modifier.fillMaxWidth())
                }
            }

            item {
                Text("History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                if (completions.isEmpty()) {
                    Text("No completions yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            items(completions, key = { it.id }) { completion ->
                val dateString = com.example.util.TimeUtils.formatRelativeDate(completion.completedAt)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(dateString, style = MaterialTheme.typography.bodyLarge)
                }
                HorizontalDivider(alpha = 0.5f)
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Habit") },
                text = { Text("Are you sure you want to delete this habit? All history will be lost.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteHabit(habitId)
                        showDeleteDialog = false
                        onNavigateBack()
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

// Extension for divider compatibility
@Composable
fun HorizontalDivider(modifier: Modifier = Modifier, alpha: Float = 1f) {
    androidx.compose.material3.HorizontalDivider(
        modifier = modifier,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = alpha)
    )
}
