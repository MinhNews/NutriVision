package com.example.nutrivision.domain.calc

import com.example.nutrivision.domain.text.AppLocale
import com.example.nutrivision.domain.text.tr
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Ngày lưu dạng "yyyy-MM-dd" theo giờ máy — index được, GROUP BY được. */
object DateUtils {

    // Tạo mới mỗi lần dùng: SimpleDateFormat không thread-safe, còn các hàm này
    // được gọi từ cả main thread lẫn coroutine.
    private fun keyFormat() = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private fun displayFormat() =
        if (AppLocale.isEnglish) SimpleDateFormat("MMM d, yyyy", Locale.US)
        else SimpleDateFormat("dd/MM/yyyy", Locale.US)

    private val englishMonths = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December",
    )

    fun todayKey(): String = keyFormat().format(Date())

    /**
     * Phát ngày hôm nay và tự phát lại khi sang ngày mới (hoặc khi đồng hồ /
     * múi giờ bị đổi). Kiểm tra mỗi 30 giây nên rất nhẹ, không đánh thức máy.
     */
    fun todayFlow(checkIntervalMs: Long = 30_000L): Flow<String> = flow {
        var last = todayKey()
        emit(last)
        while (true) {
            delay(checkIntervalMs)
            val now = todayKey()
            if (now != last) {
                last = now
                emit(now)
            }
        }
    }

    fun keyOf(date: Date): String = keyFormat().format(date)

    fun parseKey(dateKey: String): Date? = runCatching { keyFormat().parse(dateKey) }.getOrNull()

    fun daysAgoKey(daysAgo: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return keyFormat().format(cal.time)
    }

    fun addDays(dateKey: String, days: Int): String {
        val cal = Calendar.getInstance()
        parseKey(dateKey)?.let { cal.time = it } ?: return dateKey
        cal.add(Calendar.DAY_OF_YEAR, days)
        return keyFormat().format(cal.time)
    }

    /** 7 ngày gần nhất, cũ → mới. */
    fun lastNDays(n: Int, endDateKey: String = todayKey()): List<String> =
        (n - 1 downTo 0).map { addDays(endDateKey, -it) }

    fun display(dateKey: String): String =
        parseKey(dateKey)?.let { displayFormat().format(it) } ?: dateKey

    fun dayNumber(dateKey: String): String {
        val cal = Calendar.getInstance()
        parseKey(dateKey)?.let { cal.time = it } ?: return ""
        return cal.get(Calendar.DAY_OF_MONTH).toString()
    }

    fun weekdayShort(dateKey: String): String {
        val cal = Calendar.getInstance()
        parseKey(dateKey)?.let { cal.time = it } ?: return ""
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> tr("T2", "Mon")
            Calendar.TUESDAY -> tr("T3", "Tue")
            Calendar.WEDNESDAY -> tr("T4", "Wed")
            Calendar.THURSDAY -> tr("T5", "Thu")
            Calendar.FRIDAY -> tr("T6", "Fri")
            Calendar.SATURDAY -> tr("T7", "Sat")
            else -> tr("CN", "Sun")
        }
    }

    fun weekdayLong(dateKey: String): String {
        val cal = Calendar.getInstance()
        parseKey(dateKey)?.let { cal.time = it } ?: return ""
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> tr("Thứ Hai", "Monday")
            Calendar.TUESDAY -> tr("Thứ Ba", "Tuesday")
            Calendar.WEDNESDAY -> tr("Thứ Tư", "Wednesday")
            Calendar.THURSDAY -> tr("Thứ Năm", "Thursday")
            Calendar.FRIDAY -> tr("Thứ Sáu", "Friday")
            Calendar.SATURDAY -> tr("Thứ Bảy", "Saturday")
            else -> tr("Chủ Nhật", "Sunday")
        }
    }

    fun isToday(dateKey: String): Boolean = dateKey == todayKey()

    fun isFuture(dateKey: String): Boolean {
        val date = parseKey(dateKey) ?: return false
        return date.after(Date())
    }

    fun monthLabel(dateKey: String): String {
        val cal = Calendar.getInstance()
        parseKey(dateKey)?.let { cal.time = it } ?: return ""
        val month = cal.get(Calendar.MONTH)
        return if (AppLocale.isEnglish) englishMonths[month] else "Tháng ${month + 1}"
    }

    /** "Tháng 11, 2026" / "November 2026" — lịch tháng và thống kê tháng. */
    fun monthYearLabel(dateKey: String): String {
        val cal = Calendar.getInstance()
        parseKey(dateKey)?.let { cal.time = it } ?: return ""
        val month = cal.get(Calendar.MONTH)
        return if (AppLocale.isEnglish) {
            "${englishMonths[month]} ${cal.get(Calendar.YEAR)}"
        } else {
            "Tháng ${month + 1}, ${cal.get(Calendar.YEAR)}"
        }
    }

    fun firstOfMonth(dateKey: String): String {
        val cal = Calendar.getInstance()
        parseKey(dateKey)?.let { cal.time = it } ?: return dateKey
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return keyFormat().format(cal.time)
    }

    fun addMonths(monthKey: String, months: Int): String {
        val cal = Calendar.getInstance()
        parseKey(firstOfMonth(monthKey))?.let { cal.time = it } ?: return monthKey
        cal.add(Calendar.MONTH, months)
        return keyFormat().format(cal.time)
    }

    fun addYears(monthKey: String, years: Int): String = addMonths(monthKey, years * 12)

    fun daysInMonth(monthKey: String): Int {
        val cal = Calendar.getInstance()
        parseKey(firstOfMonth(monthKey))?.let { cal.time = it } ?: return 30
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    /** Toàn bộ ngày của tháng, cũ → mới. */
    fun daysOfMonth(monthKey: String): List<String> {
        val first = firstOfMonth(monthKey)
        return (0 until daysInMonth(first)).map { addDays(first, it) }
    }

    /** Thứ Hai = 0 … Chủ Nhật = 6, dùng để xếp lưới lịch. */
    fun dayOfWeekIndex(dateKey: String): Int {
        val cal = Calendar.getInstance()
        parseKey(dateKey)?.let { cal.time = it } ?: return 0
        return (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
    }

    fun dayOfMonth(dateKey: String): Int {
        val cal = Calendar.getInstance()
        parseKey(dateKey)?.let { cal.time = it } ?: return 0
        return cal.get(Calendar.DAY_OF_MONTH)
    }

    fun year(dateKey: String): Int {
        val cal = Calendar.getInstance()
        parseKey(dateKey)?.let { cal.time = it } ?: return 0
        return cal.get(Calendar.YEAR)
    }

    fun monthSpanLabel(monthKey: String): String {
        val days = daysOfMonth(monthKey)
        return "${display(days.first())} – ${display(days.last())}"
    }
}
