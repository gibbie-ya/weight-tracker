package com.yourayurveda.weighttracker.ui.goals

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yourayurveda.weighttracker.AppContainer
import com.yourayurveda.weighttracker.data.datastore.SettingsRepository
import com.yourayurveda.weighttracker.data.datastore.UserSettings
import com.yourayurveda.weighttracker.data.repository.EntryRepository
import com.yourayurveda.weighttracker.util.CsvUtils
import com.yourayurveda.weighttracker.util.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GoalsViewModel(
    private val entryRepository: EntryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<UserSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), UserSettings())

    fun updateUnit(newUnit: String) {
        viewModelScope.launch {
            val current = settings.value
            if (current.unit == newUnit) return@launch
            val factor = if (newUnit == "lbs") 2.20462f else 0.453592f
            settingsRepository.updateUnit(newUnit)
            if (current.startWeight > 0f) settingsRepository.updateStartWeight(current.startWeight * factor)
            if (current.goalWeight > 0f) settingsRepository.updateGoalWeight(current.goalWeight * factor)
            val entries = entryRepository.getAllEntriesOnce()
            entries.forEach { e ->
                e.weight?.let { w ->
                    entryRepository.upsertEntry(e.copy(weight = w * factor, updatedAt = System.currentTimeMillis()))
                }
            }
        }
    }

    fun updateStartWeight(w: Float) { viewModelScope.launch { settingsRepository.updateStartWeight(w) } }
    fun updateGoalWeight(w: Float) { viewModelScope.launch { settingsRepository.updateGoalWeight(w) } }
    fun updateCalorieTarget(t: Int) { viewModelScope.launch { settingsRepository.updateCalorieTarget(t) } }
    fun updateStepGoal(g: Int) { viewModelScope.launch { settingsRepository.updateStepGoal(g) } }

    fun updateReminderEnabled(enabled: Boolean, context: Context) {
        viewModelScope.launch {
            settingsRepository.updateReminderEnabled(enabled)
            ReminderScheduler.schedule(context, settings.value.reminderHour, enabled, force = true)
        }
    }

    fun updateReminderHour(hour: Int, context: Context) {
        viewModelScope.launch {
            settingsRepository.updateReminderHour(hour)
            ReminderScheduler.schedule(context, hour, settings.value.reminderEnabled, force = true)
        }
    }

    fun exportCsv(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val entries = entryRepository.getAllEntriesOnce()
            CsvUtils.shareCsv(context, entries)
        }
    }

    fun importCsv(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val entries = CsvUtils.fromCsv(context, uri)
            entries.forEach { entryRepository.upsertEntry(it) }
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { GoalsViewModel(container.entryRepository, container.settingsRepository) }
        }
    }
}
