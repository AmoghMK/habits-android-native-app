package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.data.Habit
import java.util.Date
import java.util.Locale

import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import com.example.data.ThemePreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HabitViewModel,
    onNavigateToAddHabit: () -> Unit,
    onNavigateToHabitDetails: (Long) -> Unit
) {
    val habits by viewModel.allHabits.collectAsStateWithLifecycle()
    val themePreference by viewModel.themePreference.collectAsStateWithLifecycle()
    val now = System.currentTimeMillis()
    
    var selectedTabIndex by remember { mutableStateOf(0) }
    var showThemeMenu by remember { mutableStateOf(false) }
    
    // Sort logic
    val activeHabits = habits.filter { it.isActive }
    val disabledHabits = habits.filter { !it.isActive }
    
    val sortedActiveHabits = remember(activeHabits, now) {
        activeHabits.sortedWith(compareBy({ it.nextDueAt != null && it.nextDueAt!! > now }, { it.nextDueAt }))
    }
    val sortedDisabledHabits = remember(disabledHabits) {
        disabledHabits.sortedByDescending { it.updatedAt }
    }

    // Permission launcher for notifications
    var hasNotificationPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    Box {
                        IconButton(onClick = { showThemeMenu = true }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Theme Settings")
                        }
                        DropdownMenu(
                            expanded = showThemeMenu,
                            onDismissRequest = { showThemeMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("System Theme") },
                                onClick = {
                                    viewModel.setThemePreference(ThemePreference.SYSTEM)
                                    showThemeMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Light Mode") },
                                onClick = {
                                    viewModel.setThemePreference(ThemePreference.LIGHT)
                                    showThemeMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Dark Mode") },
                                onClick = {
                                    viewModel.setThemePreference(ThemePreference.DARK)
                                    showThemeMenu = false
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTabIndex == 0) {
                FloatingActionButton(onClick = onNavigateToAddHabit) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Habit")
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Active") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Disabled") }
                )
            }
            
            val currentList = if (selectedTabIndex == 0) sortedActiveHabits else sortedDisabledHabits
            
            if (currentList.isEmpty()) {
                EmptyState(
                    modifier = Modifier.weight(1f),
                    onAddClick = onNavigateToAddHabit,
                    isActiveTab = selectedTabIndex == 0
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentList, key = { it.id }) { habit ->
                        HabitCard(
                            habit = habit,
                            onClick = { onNavigateToHabitDetails(habit.id) },
                            onComplete = { viewModel.completeHabit(habit) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HabitCard(
    habit: Habit,
    onClick: () -> Unit,
    onComplete: () -> Unit
) {
    val now = System.currentTimeMillis()
    val isPending = habit.isActive && habit.nextDueAt != null && habit.nextDueAt!! <= now
    val timeText = rememberCountdownText(habit.nextDueAt)

    val lastCompletedText = if (habit.lastCompletedAt != null) {
        com.example.util.TimeUtils.formatRelativeDate(habit.lastCompletedAt)
    } else {
        "Never"
    }

    val contentAlpha = if (habit.isActive) 1f else 0.5f

    val unitShort = when (habit.resetUnit) {
        com.example.data.DurationUnit.MINUTES -> "m"
        com.example.data.DurationUnit.HOURS -> "h"
        com.example.data.DurationUnit.DAYS -> "d"
        com.example.data.DurationUnit.WEEKS -> "w"
        com.example.data.DurationUnit.MONTHS -> "mo"
        com.example.data.DurationUnit.YEARS -> "y"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (!habit.isActive) {
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
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (!habit.isActive) 0.dp else 2.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isPending) {
                        Text("⚠️ ", style = MaterialTheme.typography.titleMedium)
                    }
                    if (habit.icon != null) {
                        Text(
                            text = "${habit.icon} ", 
                            style = MaterialTheme.typography.titleMedium,
                            color = LocalContentColor.current.copy(alpha = contentAlpha)
                        )
                    }
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isPending) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Filled.Refresh,
                        contentDescription = "Repetition Interval",
                        modifier = Modifier.size(16.dp),
                        tint = if (isPending) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f * contentAlpha)
                    )
                    Text(
                        text = "${habit.resetAmount}$unitShort",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isPending) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f * contentAlpha)
                    )
                }
            }

            if (habit.isActive) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isPending) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Last completed",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isPending) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f * contentAlpha)
                    )
                    Text(
                        text = lastCompletedText,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isPending) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f * contentAlpha)
                    )
                }
                
                if (habit.isActive) {
                    Button(
                        onClick = onComplete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPending) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primaryContainer,
                            contentColor = if (isPending) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(androidx.compose.material.icons.Icons.Filled.Check, contentDescription = "Complete", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isPending) "Complete" else "Complete Early")
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier, onAddClick: () -> Unit, isActiveTab: Boolean = true) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isActiveTab) {
            Text(
                text = "No habits yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add something you want to remember",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onAddClick) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Habit")
            }
        } else {
            Text(
                text = "No disabled habits",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Habits you pause will appear here.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
