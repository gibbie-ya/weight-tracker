package com.yourayurveda.weighttracker.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class UserSettings(
    val unit: String = "kg",
    val startWeight: Float = 0f,
    val goalWeight: Float = 0f,
    val dailyCalorieTarget: Int = 2000,
    val dailyStepGoal: Int = 10000,
    val reminderEnabled: Boolean = true,
    val reminderHour: Int = 21,
    val weightReminderEnabled: Boolean = true,
    val weightReminderHour: Int = 8
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class SettingsRepository(private val context: Context) {

    companion object {
        val KEY_UNIT = stringPreferencesKey("unit")
        val KEY_START_WEIGHT = floatPreferencesKey("start_weight")
        val KEY_GOAL_WEIGHT = floatPreferencesKey("goal_weight")
        val KEY_CALORIE_TARGET = intPreferencesKey("calorie_target")
        val KEY_STEP_GOAL = intPreferencesKey("step_goal")
        val KEY_REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val KEY_REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val KEY_WEIGHT_REMINDER_ENABLED = booleanPreferencesKey("weight_reminder_enabled")
        val KEY_WEIGHT_REMINDER_HOUR = intPreferencesKey("weight_reminder_hour")
    }

    val settings: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            unit = prefs[KEY_UNIT] ?: "kg",
            startWeight = prefs[KEY_START_WEIGHT] ?: 0f,
            goalWeight = prefs[KEY_GOAL_WEIGHT] ?: 0f,
            dailyCalorieTarget = prefs[KEY_CALORIE_TARGET] ?: 2000,
            dailyStepGoal = prefs[KEY_STEP_GOAL] ?: 10000,
            reminderEnabled = prefs[KEY_REMINDER_ENABLED] ?: true,
            reminderHour = prefs[KEY_REMINDER_HOUR] ?: 21,
            weightReminderEnabled = prefs[KEY_WEIGHT_REMINDER_ENABLED] ?: true,
            weightReminderHour = prefs[KEY_WEIGHT_REMINDER_HOUR] ?: 8
        )
    }

    suspend fun updateUnit(unit: String) = context.dataStore.edit { it[KEY_UNIT] = unit }
    suspend fun updateStartWeight(w: Float) = context.dataStore.edit { it[KEY_START_WEIGHT] = w }
    suspend fun updateGoalWeight(w: Float) = context.dataStore.edit { it[KEY_GOAL_WEIGHT] = w }
    suspend fun updateCalorieTarget(t: Int) = context.dataStore.edit { it[KEY_CALORIE_TARGET] = t }
    suspend fun updateStepGoal(g: Int) = context.dataStore.edit { it[KEY_STEP_GOAL] = g }
    suspend fun updateReminderEnabled(e: Boolean) = context.dataStore.edit { it[KEY_REMINDER_ENABLED] = e }
    suspend fun updateReminderHour(h: Int) = context.dataStore.edit { it[KEY_REMINDER_HOUR] = h }
    suspend fun updateWeightReminderEnabled(e: Boolean) = context.dataStore.edit { it[KEY_WEIGHT_REMINDER_ENABLED] = e }
    suspend fun updateWeightReminderHour(h: Int) = context.dataStore.edit { it[KEY_WEIGHT_REMINDER_HOUR] = h }
}
