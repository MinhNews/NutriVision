package com.example.nutrivision.ui.util

import com.example.nutrivision.domain.text.AppLocale
import com.example.nutrivision.domain.text.tr
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

private val viLocale = Locale("vi", "VN")

/** Dấu phân cách nghìn theo ngôn ngữ: 1.731 (vi) / 1,731 (en). */
private fun intFormat(): NumberFormat =
    NumberFormat.getIntegerInstance(if (AppLocale.isEnglish) Locale.US else viLocale)

private fun decimalLocale(): Locale = if (AppLocale.isEnglish) Locale.US else viLocale

fun Int.grouped(): String = intFormat().format(this)

fun Double.roundKcal(): Int = roundToInt()

fun Double.kcalText(): String = "${roundToInt().grouped()} kcal"

fun Double.gramText(): String {
    val rounded = roundToInt()
    return "$rounded g"
}

fun Double.macroText(): String = "${roundToInt()} g"

fun Double.oneDecimal(): String {
    val rounded = (this * 10).roundToInt() / 10.0
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString()
    else String.format(decimalLocale(), "%.1f", rounded)
}

fun Double.multiplierText(): String {
    val rounded = (this * 10).roundToInt() / 10.0
    return "x" + if (rounded % 1.0 == 0.0) rounded.toInt().toString()
    else String.format(decimalLocale(), "%.1f", rounded)
}

fun Int.plural(count: Int, unit: String): String = "$count $unit"

fun greetingForHour(hour: Int): String = when (hour) {
    in 0..4 -> tr("Khuya rồi, nghỉ ngơi thôi", "Late night — time to rest")
    in 5..10 -> tr("Chào buổi sáng", "Good morning")
    in 11..13 -> tr("Chào buổi trưa", "Good afternoon")
    in 14..17 -> tr("Chào buổi chiều", "Good afternoon")
    in 18..22 -> tr("Chào buổi tối", "Good evening")
    else -> tr("Chào buổi tối", "Good evening")
}
