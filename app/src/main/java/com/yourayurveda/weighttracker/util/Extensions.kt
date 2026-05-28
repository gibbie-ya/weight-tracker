package com.yourayurveda.weighttracker.util

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun Float.formatWeight(): String = "%.1f".format(this)

fun Int.formatSteps(): String =
    NumberFormat.getNumberInstance(Locale.getDefault()).format(this)

fun String.toDisplayDate(): String = try {
    LocalDate.parse(this).format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy"))
} catch (e: Exception) {
    this
}

fun String.toShortDate(): String = try {
    LocalDate.parse(this).format(DateTimeFormatter.ofPattern("d MMM"))
} catch (e: Exception) {
    this
}
