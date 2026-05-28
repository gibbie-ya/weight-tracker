package com.yourayurveda.weighttracker.ui.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourayurveda.weighttracker.data.db.DailyEntry
import com.yourayurveda.weighttracker.util.formatSteps
import com.yourayurveda.weighttracker.util.formatWeight
import com.yourayurveda.weighttracker.util.toDisplayDate

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    var entryToEdit by remember { mutableStateOf<DailyEntry?>(null) }
    var entryToDelete by remember { mutableStateOf<DailyEntry?>(null) }
    val haptic = LocalHapticFeedback.current

    if (entries.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "No entries yet.\nStart logging from Today!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(entries, key = { it.date }) { entry ->
            EntryRow(
                entry = entry,
                onClick = { entryToEdit = entry },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    entryToDelete = entry
                }
            )
        }
    }

    entryToEdit?.let { entry ->
        EditEntryBottomSheet(
            entry = entry,
            onDismiss = { entryToEdit = null },
            onSave = { updated ->
                viewModel.updateEntry(updated)
                entryToEdit = null
            }
        )
    }

    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Delete entry?") },
            text = { Text("Remove ${entry.date.toDisplayDate()}? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteEntry(entry)
                    entryToDelete = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EntryRow(entry: DailyEntry, onClick: () -> Unit, onLongClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.date.toDisplayDate(), style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    entry.weight?.let {
                        Text("⚖ ${it.formatWeight()}", style = MaterialTheme.typography.bodySmall)
                    }
                    if (entry.caloriesConsumed > 0)
                        Text("🔥 ${entry.caloriesConsumed} kcal", style = MaterialTheme.typography.bodySmall)
                    if (entry.steps > 0)
                        Text("👟 ${entry.steps.formatSteps()}", style = MaterialTheme.typography.bodySmall)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (entry.wentToGym) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = "Gym",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                if (entry.hitMacros) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Macros",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditEntryBottomSheet(
    entry: DailyEntry,
    onDismiss: () -> Unit,
    onSave: (DailyEntry) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val haptic = LocalHapticFeedback.current

    var weightText by remember { mutableStateOf(entry.weight?.let { "%.1f".format(it) } ?: "") }
    var caloriesText by remember { mutableStateOf(if (entry.caloriesConsumed > 0) entry.caloriesConsumed.toString() else "") }
    var activeCalText by remember { mutableStateOf(if (entry.activeCaloriesBurnt > 0) entry.activeCaloriesBurnt.toString() else "") }
    var stepsText by remember { mutableStateOf(if (entry.steps > 0) entry.steps.toString() else "") }
    var hitMacros by remember { mutableStateOf(entry.hitMacros) }
    var wentToGym by remember { mutableStateOf(entry.wentToGym) }
    var notes by remember { mutableStateOf(entry.notes) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(entry.date.toDisplayDate(), style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = weightText, onValueChange = { weightText = it },
                label = { Text("Weight") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = caloriesText, onValueChange = { caloriesText = it },
                    label = { Text("Calories In") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = activeCalText, onValueChange = { activeCalText = it },
                    label = { Text("Active Cal") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = stepsText, onValueChange = { stepsText = it },
                label = { Text("Steps") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = hitMacros,
                    onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); hitMacros = !hitMacros },
                    label = { Text("Hit macros") },
                    leadingIcon = if (hitMacros) {
                        { Icon(Icons.Default.Check, null, Modifier.size(FilterChipDefaults.IconSize)) }
                    } else null
                )
                FilterChip(
                    selected = wentToGym,
                    onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); wentToGym = !wentToGym },
                    label = { Text("Went to gym") },
                    leadingIcon = if (wentToGym) {
                        { Icon(Icons.Default.Check, null, Modifier.size(FilterChipDefaults.IconSize)) }
                    } else null
                )
            }
            OutlinedTextField(
                value = notes, onValueChange = { notes = it },
                label = { Text("Notes") }, minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    onSave(entry.copy(
                        weight = weightText.toFloatOrNull(),
                        caloriesConsumed = caloriesText.toIntOrNull() ?: 0,
                        activeCaloriesBurnt = activeCalText.toIntOrNull() ?: 0,
                        steps = stepsText.toIntOrNull() ?: 0,
                        hitMacros = hitMacros,
                        wentToGym = wentToGym,
                        notes = notes,
                        updatedAt = System.currentTimeMillis()
                    ))
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save changes") }

            Spacer(Modifier.height(8.dp))
        }
    }
}
