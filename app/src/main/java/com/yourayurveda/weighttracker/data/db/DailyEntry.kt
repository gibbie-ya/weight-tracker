package com.yourayurveda.weighttracker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_entries")
data class DailyEntry(
    @PrimaryKey val date: String,
    val weight: Float? = null,
    val caloriesConsumed: Int = 0,
    val activeCaloriesBurnt: Int = 0,
    val steps: Int = 0,
    val hitMacros: Boolean = false,
    val wentToGym: Boolean = false,
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
