package com.yourayurveda.weighttracker.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yourayurveda.weighttracker.AppContainer
import com.yourayurveda.weighttracker.data.db.DailyEntry
import com.yourayurveda.weighttracker.data.repository.EntryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val entryRepository: EntryRepository
) : ViewModel() {

    val entries: StateFlow<List<DailyEntry>> = entryRepository.getAllEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    fun deleteEntry(entry: DailyEntry) {
        viewModelScope.launch { entryRepository.deleteEntry(entry) }
    }

    fun updateEntry(entry: DailyEntry) {
        viewModelScope.launch {
            entryRepository.upsertEntry(entry.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { HistoryViewModel(container.entryRepository) }
        }
    }
}
