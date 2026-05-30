package com.yourayurveda.weighttracker.ui.progress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.yourayurveda.weighttracker.data.db.DailyEntry
import com.yourayurveda.weighttracker.util.formatWeight
import java.time.format.DateTimeFormatter

@Composable
fun ProgressScreen(viewModel: ProgressViewModel) {
    val filteredEntries by viewModel.filteredEntries.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val selectedRange by viewModel.selectedRange.collectAsStateWithLifecycle()
    val weeklyTotals by viewModel.weeklyTotals.collectAsStateWithLifecycle()

    if (filteredEntries.isEmpty() && stats.daysLogged == 0) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "No data yet.\nLog some entries to see your progress!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { WeeklyTotalsCard(weeklyTotals) }

        item { StatsGrid(stats, settings.unit) }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(TimeRange.entries) { range ->
                    FilterChip(
                        selected = selectedRange == range,
                        onClick = { viewModel.setRange(range) },
                        label = { Text(range.label) }
                    )
                }
            }
        }

        item {
            ChartCard(title = "Weight Trend (${settings.unit})") {
                WeightChart(filteredEntries)
            }
        }

        item {
            ChartCard(title = "Calories: In vs Active Burn") {
                CaloriesChart(filteredEntries)
            }
        }

        item {
            ChartCard(title = "Daily Steps (goal: ${settings.dailyStepGoal})") {
                StepsChart(filteredEntries)
            }
        }
    }
}

@Composable
private fun WeeklyTotalsCard(totals: WeeklyTotals) {
    val weekLabel = totals.weekStart.format(DateTimeFormatter.ofPattern("d MMM"))
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "This week (from $weekLabel)",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(
                    label = "Calories",
                    value = "%,d kcal".format(totals.calories),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Steps",
                    value = "%,d".format(totals.steps),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatsGrid(stats: ProgressStats, unit: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatCard("Lost", "${stats.totalWeightLost.formatWeight()} $unit", Modifier.weight(1f))
        StatCard("To go", "${stats.weightRemaining.formatWeight()} $unit", Modifier.weight(1f))
        StatCard("Avg net", "${stats.avgDailyCalorieNet} kcal", Modifier.weight(1f))
        StatCard("Days", "${stats.daysLogged}", Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleSmall)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ChartCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun WeightChart(entries: List<DailyEntry>) {
    val weightEntries = entries.filter { it.weight != null }
    if (weightEntries.isEmpty()) { EmptyChart("No weight data"); return }

    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(weightEntries) {
        modelProducer.runTransaction {
            lineSeries { series(y = weightEntries.map { it.weight!! }) }
        }
    }
    CartesianChartHost(
        chart = rememberCartesianChart(rememberLineCartesianLayer()),
        modelProducer = modelProducer,
        modifier = Modifier.fillMaxWidth().height(180.dp)
    )
}

@Composable
private fun CaloriesChart(entries: List<DailyEntry>) {
    val valid = entries.filter { it.caloriesConsumed > 0 || it.activeCaloriesBurnt > 0 }
    if (valid.isEmpty()) { EmptyChart("No calorie data"); return }

    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(valid) {
        modelProducer.runTransaction {
            columnSeries {
                series(y = valid.map { it.caloriesConsumed })
                series(y = valid.map { it.activeCaloriesBurnt })
            }
        }
    }
    CartesianChartHost(
        chart = rememberCartesianChart(rememberColumnCartesianLayer()),
        modelProducer = modelProducer,
        modifier = Modifier.fillMaxWidth().height(180.dp)
    )
}

@Composable
private fun StepsChart(entries: List<DailyEntry>) {
    val valid = entries.filter { it.steps > 0 }
    if (valid.isEmpty()) { EmptyChart("No step data"); return }

    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(valid) {
        modelProducer.runTransaction {
            columnSeries { series(y = valid.map { it.steps }) }
        }
    }
    CartesianChartHost(
        chart = rememberCartesianChart(rememberColumnCartesianLayer()),
        modelProducer = modelProducer,
        modifier = Modifier.fillMaxWidth().height(180.dp)
    )
}

@Composable
private fun EmptyChart(message: String) {
    Box(
        modifier = Modifier.fillMaxWidth().height(80.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium)
    }
}
