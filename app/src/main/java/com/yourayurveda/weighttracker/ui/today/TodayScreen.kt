package com.yourayurveda.weighttracker.ui.today

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourayurveda.weighttracker.util.toDisplayDate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(viewModel: TodayViewModel, snackbarHostState: SnackbarHostState) {
    val selectedEntry by viewModel.selectedEntry.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val missingDays by viewModel.missingDays.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val today = remember { LocalDate.now() }

    var weightText by remember { mutableStateOf("") }
    var caloriesText by remember { mutableStateOf("") }
    var activeCalText by remember { mutableStateOf("") }
    var stepsText by remember { mutableStateOf("") }
    var hitMacros by remember { mutableStateOf(false) }
    var wentToGym by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var prefilledFor by remember { mutableStateOf<String?>(null) }
    var bannerDismissed by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Reset fields when the selected date changes
    LaunchedEffect(selectedDate) {
        weightText = ""
        caloriesText = ""
        activeCalText = ""
        stepsText = ""
        hitMacros = false
        wentToGym = false
        notes = ""
        prefilledFor = null
    }

    // Fill fields when an entry is loaded for the selected date
    LaunchedEffect(selectedEntry) {
        val entry = selectedEntry
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

    if (showDatePicker) {
        DatePickerDialog(
            today = today,
            selectedDate = selectedDate,
            onDismiss = { showDatePicker = false },
            onConfirm = { date ->
                viewModel.setDate(date)
                showDatePicker = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (missingDays.isNotEmpty() && !bannerDismissed) {
            item {
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value != SwipeToDismissBoxValue.Settled) {
                            bannerDismissed = true
                            true
                        } else false
                    }
                )
                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {}
                ) {
                    MissingDaysBanner(
                        missingDays = missingDays,
                        onDateClick = { dateStr -> viewModel.setDate(LocalDate.parse(dateStr)) }
                    )
                }
            }
        }

        // Date navigation header
        item {
            DateHeader(
                selectedDate = selectedDate,
                today = today,
                onPrevious = { viewModel.setDate(selectedDate.minusDays(1)) },
                onNext = { viewModel.setDate(selectedDate.plusDays(1)) },
                onTapLabel = { showDatePicker = true }
            )
        }

        // Weight section — save independently in the morning
        item {
            Text("Weight", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = weightText,
                onValueChange = { weightText = it },
                label = { Text("Weight (${settings.unit})") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { viewModel.saveWeight(weightText.toFloatOrNull()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (selectedEntry?.weight != null) "Update weight" else "Save weight")
            }
        }

        item { HorizontalDivider() }

        // Daily log section — fill in later in the day
        item {
            Text("Daily Log", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
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
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = stepsText,
                onValueChange = { stepsText = it },
                label = { Text("Steps") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
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
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    viewModel.saveLog(
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
                Text(if ((selectedEntry?.caloriesConsumed ?: 0) != 0) "Update log" else "Save log")
            }
        }
    }
}

@Composable
private fun DateHeader(
    selectedDate: LocalDate,
    today: LocalDate,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onTapLabel: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous day")
        }
        Text(
            text = when (selectedDate) {
                today -> "Today — ${selectedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))}"
                today.minusDays(1) -> "Yesterday — ${selectedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))}"
                else -> selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"))
            },
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onTapLabel)
        )
        IconButton(
            onClick = onNext,
            enabled = selectedDate < today
        ) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next day")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    today: LocalDate,
    selectedDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.toEpochDay() * 86_400_000L,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val dayEpoch = utcTimeMillis / 86_400_000L
                return dayEpoch <= today.toEpochDay()
            }
        }
    )
    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { millis ->
                    onConfirm(LocalDate.ofEpochDay(millis / 86_400_000L))
                }
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    ) {
        DatePicker(state = state)
    }
}

@Composable
private fun MissingDaysBanner(
    missingDays: List<String>,
    onDateClick: (String) -> Unit
) {
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
                    "Missing entries — tap to fill in:",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Spacer(Modifier.height(4.dp))
            missingDays.forEach { date ->
                Text(
                    "• ${date.toDisplayDate()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDateClick(date) }
                        .padding(vertical = 2.dp)
                )
            }
        }
    }
}
