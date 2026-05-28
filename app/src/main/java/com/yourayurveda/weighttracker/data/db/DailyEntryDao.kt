package com.yourayurveda.weighttracker.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyEntryDao {
    @Query("SELECT * FROM daily_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<DailyEntry>>

    @Query("SELECT * FROM daily_entries WHERE date = :date")
    fun getEntryForDate(date: String): Flow<DailyEntry?>

    @Query("SELECT * FROM daily_entries WHERE date = :date")
    suspend fun getEntryForDateOnce(date: String): DailyEntry?

    @Query("SELECT * FROM daily_entries WHERE date >= :fromDate ORDER BY date ASC")
    fun getEntriesFrom(fromDate: String): Flow<List<DailyEntry>>

    @Query("SELECT * FROM daily_entries ORDER BY date DESC")
    suspend fun getAllEntriesOnce(): List<DailyEntry>

    @Upsert
    suspend fun upsertEntry(entry: DailyEntry)

    @Delete
    suspend fun deleteEntry(entry: DailyEntry)
}
