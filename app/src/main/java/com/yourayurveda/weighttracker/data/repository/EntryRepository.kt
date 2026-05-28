package com.yourayurveda.weighttracker.data.repository

import com.yourayurveda.weighttracker.data.db.DailyEntry
import com.yourayurveda.weighttracker.data.db.DailyEntryDao
import kotlinx.coroutines.flow.Flow

class EntryRepository(private val dao: DailyEntryDao) {
    fun getAllEntries(): Flow<List<DailyEntry>> = dao.getAllEntries()
    fun getEntryForDate(date: String): Flow<DailyEntry?> = dao.getEntryForDate(date)
    fun getEntriesFrom(fromDate: String): Flow<List<DailyEntry>> = dao.getEntriesFrom(fromDate)
    suspend fun getEntryForDateOnce(date: String): DailyEntry? = dao.getEntryForDateOnce(date)
    suspend fun getAllEntriesOnce(): List<DailyEntry> = dao.getAllEntriesOnce()
    suspend fun upsertEntry(entry: DailyEntry) = dao.upsertEntry(entry)
    suspend fun deleteEntry(entry: DailyEntry) = dao.deleteEntry(entry)
}
