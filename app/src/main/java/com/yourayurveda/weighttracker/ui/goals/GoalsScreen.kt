package com.yourayurveda.weighttracker.ui.goals

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(viewModel: GoalsViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var startWeightText by remember(settings.startWeight) {
        mutableStateOf(if (settings.startWeight > 0f) "%.1f".format(settings.startWeight) else "")
    }
    var goalWeightText by remember(settings.goalWeight) {
        mutableStateOf(if (settings.goalWeight > 0f) "%.1f".format(settings.goalWeight) else "")
    }
    var calorieTargetText by remember(settings.dailyCalorieTarget) {
        mutableStateOf(if (settings.dailyCalorieTarget > 0) settings.dailyCalorieTarget.toString() else "")
    }
    var stepGoalText by remember(settings.dailyStepGoal) {
        mutableStateOf(if (settings.dailyStepGoal > 0) settings.dailyStepGoal.toString() else "")
    }
    var showTimePicker by remember { mutableStateOf(false) }
    var showWeightTimePicker by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.importCsv(context, it) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Units", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf("kg", "lbs").forEachIndexed { idx, unit ->
                    SegmentedButton(
                        selected = settings.unit == unit,
                        onClick = { if (settings.unit != unit) viewModel.updateUnit(unit) },
                        shape = SegmentedButtonDefaults.itemShape(index = idx, count = 2)
                    ) { Text(unit) }
                }
            }
        }

        item {
            Text("Weights (${settings.unit})", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = startWeightText,
                    onValueChange = { startWeightText = it },
                    label = { Text("Starting") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = goalWeightText,
                    onValueChange = { goalWeightText = it },
                    label = { Text("Goal") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    startWeightText.toFloatOrNull()?.let { viewModel.updateStartWeight(it) }
                    goalWeightText.toFloatOrNull()?.let { viewModel.updateGoalWeight(it) }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save weights") }
        }

        item {
            Text("Daily Targets", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = calorieTargetText,
                    onValueChange = { calorieTargetText = it },
                    label = { Text("Calorie target") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = stepGoalText,
                    onValueChange = { stepGoalText = it },
                    label = { Text("Step goal") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    calorieTargetText.toIntOrNull()?.let { viewModel.updateCalorieTarget(it) }
                    stepGoalText.toIntOrNull()?.let { viewModel.updateStepGoal(it) }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save targets") }
        }

        item {
            Text("Reminders", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("Weight reminder", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Enable weight reminder")
                Switch(
                    checked = settings.weightReminderEnabled,
                    onCheckedChange = { viewModel.updateWeightReminderEnabled(it, context) }
                )
            }
            if (settings.weightReminderEnabled) {
                Spacer(Modifier.height(4.dp))
                OutlinedButton(
                    onClick = { showWeightTimePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Weight reminder time: %02d:00".format(settings.weightReminderHour))
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("Daily log reminder", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Enable log reminder")
                Switch(
                    checked = settings.reminderEnabled,
                    onCheckedChange = { viewModel.updateReminderEnabled(it, context) }
                )
            }
            if (settings.reminderEnabled) {
                Spacer(Modifier.height(4.dp))
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Log reminder time: %02d:00".format(settings.reminderHour))
                }
            }
        }

        item {
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            Text("Data", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { viewModel.exportCsv(context) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Upload, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Export CSV")
                }
                OutlinedButton(
                    onClick = { importLauncher.launch("text/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Download, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Import CSV")
                }
            }
        }
    }

    if (showTimePicker) {
        ReminderTimeDialog(
            initialHour = settings.reminderHour,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour ->
                viewModel.updateReminderHour(hour, context)
                showTimePicker = false
            }
        )
    }

    if (showWeightTimePicker) {
        ReminderTimeDialog(
            initialHour = settings.weightReminderHour,
            onDismiss = { showWeightTimePicker = false },
            onConfirm = { hour ->
                viewModel.updateWeightReminderHour(hour, context)
                showWeightTimePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimeDialog(
    initialHour: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val state = rememberTimePickerState(initialHour = initialHour, initialMinute = 0, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set reminder time") },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour) }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
