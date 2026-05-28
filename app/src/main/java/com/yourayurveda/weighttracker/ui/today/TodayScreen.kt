package com.yourayurveda.weighttracker.ui.today

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourayurveda.weighttracker.util.toDisplayDate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TodayScreen(viewModel: TodayViewModel, snackbarHostState: SnackbarHostState) {
    val todayEntry by viewModel.todayEntry.collectAsStateWithLifecycle()
    val missingDays by viewModel.missingDays.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    var weightText by remember { mutableStateOf("") }
    var caloriesText by remember { mutableStateOf("") }
    var activeCalText by remember { mutableStateOf("") }
    var stepsText by remember { mutableStateOf("") }
    var hitMacros by remember { mutableStateOf(false) }
    var wentToGym by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var prefilledFor by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(todayEntry) {
        val entry = todayEntry
        if (entry != null && prefilledFor != entry.date) {
            weightText = entry.weight?.let { "%.1f".format(it) } ?: ""
            caloriesText = if (entry.caloriesConsumed > 0) entry.caloriesConsumed.toString() else ""
            activeCalText = if (entry.activeCaloriesBurnt > 0) entry.activeCaloriesBurnt.toString() else ""
            stepsText = if (entry.steps > 0) entry.steps.toString() else ""
            hitMacros = entry.hitMacros
            wentToGym = entry.wentToGym
            notes = entry.notes
            prefilledFor = entry.date
        }
    }

    LaunchedEffect(Unit) {
        viewModel.saveResult.collect { snackbarHostState.showSnackbar("Saved!") }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (missingDays.isNotEmpty()) {
            item { MissingDaysBanner(missingDays) }
        }

        item {
            Text(
                text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }

        item {
            OutlinedTextField(
                value = weightText,
                onValueChange = { weightText = it },
                label = { Text("Weight (${settings.unit})") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = caloriesText,
                    onValueChange = { caloriesText = it },
                    label = { Text("Calories In") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = activeCalText,
                    onValueChange = { activeCalText = it },
                    label = { Text("Active Cal") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            OutlinedTextField(
                value = stepsText,
                onValueChange = { stepsText = it },
                label = { Text("Steps") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = hitMacros,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        hitMacros = !hitMacros
                    },
                    label = { Text("Hit macros") },
                    leadingIcon = if (hitMacros) {
                        { Icon(Icons.Default.Check, null, Modifier.size(FilterChipDefaults.IconSize)) }
                    } else null
                )
                FilterChip(
                    selected = wentToGym,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        wentToGym = !wentToGym
                    },
                    label = { Text("Went to gym") },
                    leadingIcon = if (wentToGym) {
                        { Icon(Icons.Default.Check, null, Modifier.size(FilterChipDefaults.IconSize)) }
                    } else null
                )
            }
        }

        item {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Button(
                onClick = {
                    viewModel.saveEntry(
                        weight = weightText.toFloatOrNull(),
                        caloriesConsumed = caloriesText.toIntOrNull() ?: 0,
                        activeCaloriesBurnt = activeCalText.toIntOrNull() ?: 0,
                        steps = stepsText.toIntOrNull() ?: 0,
                        hitMacros = hitMacros,
                        wentToGym = wentToGym,
                        notes = notes
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (todayEntry != null) "Update" else "Save")
            }
        }
    }
}

@Composable
private fun MissingDaysBanner(missingDays: List<String>) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Missing entries:",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Spacer(Modifier.height(4.dp))
            missingDays.forEach { date ->
                Text(
                    "• ${date.toDisplayDate()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}
