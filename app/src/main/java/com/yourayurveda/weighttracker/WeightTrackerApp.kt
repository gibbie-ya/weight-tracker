package com.yourayurveda.weighttracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.yourayurveda.weighttracker.util.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WeightTrackerApp : Application() {

    val container by lazy { AppContainer(this) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        scheduleInitialReminder()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Daily Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Daily reminder to log your progress" }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun scheduleInitialReminder() {
        CoroutineScope(Dispatchers.IO).launch {
            val settings = container.settingsRepository.settings.first()
            ReminderScheduler.schedule(this@WeightTrackerApp, settings.reminderHour, settings.reminderEnabled)
        }
    }

    companion object {
        const val CHANNEL_ID = "daily_reminders"
    }
}
