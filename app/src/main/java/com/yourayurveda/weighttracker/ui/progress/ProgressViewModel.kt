package com.yourayurveda.weighttracker.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yourayurveda.weighttracker.AppContainer
import com.yourayurveda.weighttracker.data.datastore.SettingsRepository
import com.yourayurveda.weighttracker.data.datastore.UserSettings
import com.yourayurveda.weighttracker.data.db.DailyEntry
import com.yourayurveda.weighttracker.data.repository.EntryRepository
import kotlinx.coroutines.flow.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

enum class TimeRange(val label: String, val days: Long?) {
    SEVEN("7d", 7),
    THIRTY("30d", 30),
    NINETY("90d", 90),
    ALL("All", null)
}

data class ProgressStats(
    val totalWeightLost: Float = 0f,
    val weightRemaining: Float = 0f,
    val avgDailyCalorieNet: Int = 0,
    val daysLogged: Int = 0
)

data class WeeklyTotals(
    val calories: Int = 0,
    val steps: Int = 0,
    val weekStart: LocalDate = LocalDate.now()
)

class ProgressViewModel(
    private val entryRepository: EntryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _selectedRange = MutableStateFlow(TimeRange.THIRTY)
    val selectedRange: StateFlow<TimeRange> = _selectedRange.asStateFlow()

    val settings: StateFlow<UserSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), UserSettings())

    private val allEntries: StateFlow<List<DailyEntry>> = entryRepository.getAllEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    val filteredEntries: StateFlow<List<DailyEntry>> = combine(allEntries, _selectedRange) { entries, range ->
        val sorted = entries.sortedBy { it.date }
        if (range.days == null) sorted
        else {
            val cutoff = LocalDate.now().minusDays(range.days).toString()
            sorted.filter { it.date >= cutoff }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    val stats: StateFlow<ProgressStats> = combine(allEntries, settings) { entries, s ->
        val latestWeight = entries.sortedByDescending { it.date }
            .firstOrNull { it.weight != null }?.weight ?: 0f
        val totalLost = if (s.startWeight > 0f && latestWeight > 0f)
            (s.startWeight - latestWeight).coerceAtLeast(0f) else 0f
        val remaining = if (s.goalWeight > 0f && latestWeight > 0f)
            (latestWeight - s.goalWeight).coerceAtLeast(0f) else 0f
        val nets = entries.map { it.caloriesConsumed - it.activeCaloriesBurnt }
        val avgNet = if (nets.isNotEmpty()) nets.average().toInt() else 0
        val daysLogged = entries.count { it.weight != null }
        ProgressStats(totalLost, remaining, avgNet, daysLogged)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), ProgressStats())

    val weeklyTotals: StateFlow<WeeklyTotals> = allEntries.map { entries ->
        val today = LocalDate.now()
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekEntries = entries.filter { it.date >= weekStart.toString() && it.date <= today.toString() }
        WeeklyTotals(
            calories = weekEntries.sumOf { it.caloriesConsumed },
            steps = weekEntries.sumOf { it.steps },
            weekStart = weekStart
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), WeeklyTotals())

    fun setRange(range: TimeRange) { _selectedRange.value = range }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { ProgressViewModel(container.entryRepository, container.settingsRepository) }
        }
    }
}
