package com.yourayurveda.weighttracker.ui.today

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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModel(
    private val entryRepository: EntryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val selectedEntry: StateFlow<DailyEntry?> = _selectedDate
        .flatMapLatest { date -> entryRepository.getEntryForDate(date.toString()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), null)

    val settings: StateFlow<UserSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), UserSettings())

    // Missing days always calculated relative to real today, not selectedDate
    val missingDays: StateFlow<List<String>> = entryRepository.getAllEntries()
        .map { entries ->
            (1..7).map { days -> LocalDate.now().minusDays(days.toLong()).toString() }
                .filter { date -> entries.none { it.date == date } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    private val _saveResult = MutableSharedFlow<Boolean>()
    val saveResult: SharedFlow<Boolean> = _saveResult.asSharedFlow()

    fun setDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun saveWeight(weight: Float?) {
        viewModelScope.launch {
            val date = _selectedDate.value.toString()
            val existing = selectedEntry.value
            entryRepository.upsertEntry(
                (existing ?: DailyEntry(date = date)).copy(
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
            val date = _selectedDate.value.toString()
            val existing = selectedEntry.value
            entryRepository.upsertEntry(
                (existing ?: DailyEntry(date = date)).copy(
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
