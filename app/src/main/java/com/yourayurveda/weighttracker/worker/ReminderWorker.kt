package com.yourayurveda.weighttracker.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yourayurveda.weighttracker.WeightTrackerApp
import com.yourayurveda.weighttracker.data.db.AppDatabase
import java.time.LocalDate

class ReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val type = inputData.getString(KEY_TYPE) ?: TYPE_FULL
        val today = LocalDate.now().toString()
        val entry = AppDatabase.getInstance(context).dailyEntryDao().getEntryForDateOnce(today)
        when (type) {
            TYPE_WEIGHT -> if (entry == null || entry.weight == null) showNotification(
                id = NOTIFICATION_ID_WEIGHT,
                title = "Log your weight",
                text = "You haven't recorded your weight today."
            )
            else -> if (entry == null || entry.caloriesConsumed == 0) showNotification(
                id = NOTIFICATION_ID_FULL,
                title = "Log today's progress",
                text = "You haven't logged your calories and steps yet."
            )
        }
        return Result.success()
    }

    private fun showNotification(id: Int, title: String, text: String) {
        val intent = Intent(Intent.ACTION_VIEW, "weighttracker://today".toUri()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, id, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, WeightTrackerApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(id, notification)
    }

    companion object {
        const val KEY_TYPE = "reminder_type"
        const val TYPE_WEIGHT = "weight"
        const val TYPE_FULL = "full"
        const val NOTIFICATION_ID_WEIGHT = 1002
        const val NOTIFICATION_ID_FULL = 1001
    }
}
