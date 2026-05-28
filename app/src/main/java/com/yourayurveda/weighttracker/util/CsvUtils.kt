package com.yourayurveda.weighttracker.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.yourayurveda.weighttracker.data.db.DailyEntry
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object CsvUtils {
    private const val HEADER =
        "date,weight,caloriesConsumed,activeCaloriesBurnt,steps,hitMacros,wentToGym,notes,updatedAt"

    fun toCsv(entries: List<DailyEntry>): String {
        val rows = entries.sortedBy { it.date }.joinToString("\n") { e ->
            val notes = e.notes.replace("\"", "\"\"")
            "${e.date},${e.weight ?: ""},${e.caloriesConsumed},${e.activeCaloriesBurnt}" +
                ",${e.steps},${e.hitMacros},${e.wentToGym},\"$notes\",${e.updatedAt}"
        }
        return if (rows.isEmpty()) HEADER else "$HEADER\n$rows"
    }

    fun shareCsv(context: Context, entries: List<DailyEntry>) {
        val csv = toCsv(entries)
        val file = File(context.cacheDir, "weight_tracker_export.csv")
        file.writeText(csv)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export data"))
    }

    fun fromCsv(context: Context, uri: Uri): List<DailyEntry> {
        val result = mutableListOf<DailyEntry>()
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BufferedReader(InputStreamReader(stream)).useLines { lines ->
                lines.drop(1).forEach { line -> parseLine(line)?.let { result.add(it) } }
            }
        }
        return result
    }

    private fun parseLine(line: String): DailyEntry? = try {
        val parts = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        for (ch in line) {
            when {
                ch == '"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> { parts.add(current.toString()); current.clear() }
                else -> current.append(ch)
            }
        }
        parts.add(current.toString())
        if (parts.size < 9) null
        else DailyEntry(
            date = parts[0],
            weight = parts[1].toFloatOrNull(),
            caloriesConsumed = parts[2].toIntOrNull() ?: 0,
            activeCaloriesBurnt = parts[3].toIntOrNull() ?: 0,
            steps = parts[4].toIntOrNull() ?: 0,
            hitMacros = parts[5].toBooleanStrictOrNull() ?: false,
            wentToGym = parts[6].toBooleanStrictOrNull() ?: false,
            notes = parts[7],
            updatedAt = parts[8].toLongOrNull() ?: System.currentTimeMillis()
        )
    } catch (e: Exception) {
        null
    }
}
