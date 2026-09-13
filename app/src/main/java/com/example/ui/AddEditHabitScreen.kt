package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DurationUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHabitScreen(
    viewModel: HabitViewModel,
    habitId: Long?,
    onNavigateBack: () -> Unit
) {
    val habitFlow = remember(viewModel, habitId) { 
        if (habitId != null) viewModel.getHabit(habitId) 
        else kotlinx.coroutines.flow.flowOf(null) 
    }
    val habit by habitFlow.collectAsStateWithLifecycle(initialValue = null)

    var name by remember(habit) { mutableStateOf(habit?.name ?: "") }
    var icon by remember(habit) { mutableStateOf(habit?.icon ?: "") }
    var amount by remember(habit) { mutableStateOf(habit?.resetAmount?.toString() ?: "36") }
    var unit by remember(habit) { mutableStateOf(habit?.resetUnit ?: DurationUnit.HOURS) }

    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (habitId == null) "Add Habit" else "Edit Habit") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = icon,
                    onValueChange = { 
                        if (it.length <= 2) {
                            icon = it
                        }
                    },
                    label = { Text("Emoji") },
                    modifier = Modifier.width(80.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Habit Name") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
            }

            Text("Reset Duration", style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { char -> char.isDigit() } },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.weight(1.5f)
                ) {
                    val displayUnit = unit.name.lowercase().replaceFirstChar { it.uppercase() }
                    OutlinedTextField(
                        value = displayUnit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DurationUnit.values().forEach { durationUnit ->
                            val itemDisplayUnit = durationUnit.name.lowercase().replaceFirstChar { it.uppercase() }
                            DropdownMenuItem(
                                text = { Text(itemDisplayUnit) },
                                onClick = {
                                    unit = durationUnit
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val parsedAmount = amount.toIntOrNull() ?: 0
                    if (name.isNotBlank() && parsedAmount > 0) {
                        if (habitId != null && habit?.lastCompletedAt != null) {
                            showDialog = true
                        } else {
                            viewModel.saveHabit(habitId ?: 0L, name, icon.takeIf { it.isNotBlank() }, parsedAmount, unit)
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && (amount.toIntOrNull() ?: 0) > 0
            ) {
                Text("Save")
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Update active timer?") },
                text = { Text("You changed the duration of an active habit. Should the new duration be calculated from the original completion time, or should we restart the timer right now?") },
                confirmButton = {
                    TextButton(onClick = {
                        val parsedAmount = amount.toIntOrNull() ?: 0
                        viewModel.saveHabit(habitId ?: 0L, name, icon.takeIf { it.isNotBlank() }, parsedAmount, unit, resetTimer = true)
                        showDialog = false
                        onNavigateBack()
                    }) {
                        Text("Restart Timer")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        val parsedAmount = amount.toIntOrNull() ?: 0
                        viewModel.saveHabit(habitId ?: 0L, name, icon.takeIf { it.isNotBlank() }, parsedAmount, unit, resetTimer = false)
                        showDialog = false
                        onNavigateBack()
                    }) {
                        Text("Keep Original Time")
                    }
                }
            )
        }
    }
}
