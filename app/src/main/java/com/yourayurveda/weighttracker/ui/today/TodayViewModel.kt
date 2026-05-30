package com.yourayurveda.weighttracker.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yourayurveda.weighttracker.AppContainer
import com.yourayurveda.weighttracker.data.datastore.UserSettings
import com.yourayurveda.weighttracker.data.db.DailyEntry
import com.yourayurveda.weighttracker.data.datastore.SettingsRepository
import com.yourayurveda.weighttracker.data.repository.EntryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class TodayViewModel(
    private val entryRepository: EntryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val today: String = LocalDate.now().toString()

    val todayEntry: StateFlow<DailyEntry?> = entryRepository.getEntryForDate(today)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), null)

    val settings: StateFlow<UserSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), UserSettings())

    val missingDays: StateFlow<List<String>> = entryRepository.getAllEntries()
        .map { entries ->
            (1..7).map { days -> LocalDate.now().minusDays(days.toLong()).toString() }
                .filter { date -> entries.none { it.date == date } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    private val _saveResult = MutableSharedFlow<Boolean>()
    val saveResult: SharedFlow<Boolean> = _saveResult.asSharedFlow()

    fun saveWeight(weight: Float?) {
        viewModelScope.launch {
            val existing = todayEntry.value
            entryRepository.upsertEntry(
                (existing ?: DailyEntry(date = today)).copy(
                    weight = weight,
                    updatedAt = System.currentTimeMillis()
                )
            )
            _saveResult.emit(true)
        }
    }

    fun saveLog(
        caloriesConsumed: Int,
        activeCaloriesBurnt: Int,
        steps: Int,
        hitMacros: Boolean,
        wentToGym: Boolean,
        notes: String
    ) {
        viewModelScope.launch {
            val existing = todayEntry.value
            entryRepository.upsertEntry(
                (existing ?: DailyEntry(date = today)).copy(
                    caloriesConsumed = caloriesConsumed,
                    activeCaloriesBurnt = activeCaloriesBurnt,
                    steps = steps,
                    hitMacros = hitMacros,
                    wentToGym = wentToGym,
                    notes = notes,
                    updatedAt = System.currentTimeMillis()
                )
            )
            _saveResult.emit(true)
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                TodayViewModel(container.entryRepository, container.settingsRepository)
            }
        }
    }
}
