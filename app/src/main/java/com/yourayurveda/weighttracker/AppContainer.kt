package com.yourayurveda.weighttracker

import android.content.Context
import com.yourayurveda.weighttracker.data.datastore.SettingsRepository
import com.yourayurveda.weighttracker.data.db.AppDatabase
import com.yourayurveda.weighttracker.data.repository.EntryRepository

class AppContainer(context: Context) {
    val database = AppDatabase.getInstance(context)
    val settingsRepository = SettingsRepository(context)
    val entryRepository = EntryRepository(database.dailyEntryDao())
}
