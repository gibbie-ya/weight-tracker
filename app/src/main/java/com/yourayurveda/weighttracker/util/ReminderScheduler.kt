package com.yourayurveda.weighttracker.util

import android.content.Context
import androidx.work.*
import com.yourayurveda.weighttracker.worker.ReminderWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    private const val WORK_NAME_FULL = "daily_reminder"
    private const val WORK_NAME_WEIGHT = "weight_reminder"

    // force = true when the user explicitly changes a setting; on app startup use false (KEEP)
    // so we don't reset the delay on every launch (which would prevent notifications firing).
    fun schedule(context: Context, hour: Int, enabled: Boolean, force: Boolean = false) {
        scheduleJob(context, WORK_NAME_FULL, ReminderWorker.TYPE_FULL, hour, enabled, force)
    }

    fun scheduleWeight(context: Context, hour: Int, enabled: Boolean, force: Boolean = false) {
        scheduleJob(context, WORK_NAME_WEIGHT, ReminderWorker.TYPE_WEIGHT, hour, enabled, force)
    }

    private fun scheduleJob(
        context: Context,
        workName: String,
        type: String,
        hour: Int,
        enabled: Boolean,
        force: Boolean
    ) {
        val wm = WorkManager.getInstance(context)
        if (!enabled) {
            wm.cancelUniqueWork(workName)
            return
        }
        val inputData = workDataOf(ReminderWorker.KEY_TYPE to type)
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(calculateDelay(hour), TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .build()
        val policy = if (force) ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE
                     else ExistingPeriodicWorkPolicy.KEEP
        wm.enqueueUniquePeriodicWork(workName, policy, request)
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
