package com.yourayurveda.weighttracker.util

import android.content.Context
import androidx.work.*
import com.yourayurveda.weighttracker.worker.ReminderWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private const val WORK_NAME = "daily_reminder"

    // force = true when the user explicitly changes the reminder time/toggle; on app startup
    // we use KEEP so we don't reset the delay every time the app opens (which would prevent
    // the notification from ever firing).
    fun schedule(context: Context, hour: Int, enabled: Boolean, force: Boolean = false) {
        val wm = WorkManager.getInstance(context)
        if (!enabled) {
            wm.cancelUniqueWork(WORK_NAME)
            return
        }
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(calculateDelay(hour), TimeUnit.MILLISECONDS)
            .build()
        val policy = if (force) ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE
                     else ExistingPeriodicWorkPolicy.KEEP
        wm.enqueueUniquePeriodicWork(WORK_NAME, policy, request)
    }

    private fun calculateDelay(hour: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }
}
