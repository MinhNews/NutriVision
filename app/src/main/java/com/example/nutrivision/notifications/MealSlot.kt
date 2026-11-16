package com.example.nutrivision.notifications

import com.example.nutrivision.domain.text.tr
import java.util.Calendar

/** Ba bua chinh - gio chi la mac dinh, nguoi dung chinh duoc trong Cai dat. */
enum class MealSlot(val key: String, val defaultHour: Int, val defaultMinute: Int) {
    BREAKFAST("breakfast", 7, 0),
    LUNCH("lunch", 11, 30),
    DINNER("dinner", 18, 30);

    val label: String
        get() = when (this) {
            BREAKFAST -> tr("Bữa sáng", "Breakfast")
            LUNCH -> tr("Bữa trưa", "Lunch")
            DINNER -> tr("Bữa tối", "Dinner")
        }

    val title: String
        get() = when (this) {
            BREAKFAST -> tr("Đến giờ ăn sáng", "Breakfast time")
            LUNCH -> tr("Đến giờ ăn trưa", "Lunch time")
            DINNER -> tr("Đến giờ ăn tối", "Dinner time")
        }

    /**
     * Noi dung thong bao. [remainingKcal] la so calo con thieu so voi muc tieu hom nay
     * (null khi chua co ho so hoac khong doc duoc) - nho vay thong bao noi dung so that
     * thay vi mot cau chung chung.
     */
    fun body(remainingKcal: Int?): String {
        val base = when (this) {
            BREAKFAST -> tr(
                "Ghi bữa sáng mất 20 giây - mở NutriVision và thêm món.",
                "Logging breakfast takes 20 seconds - open NutriVision and add it.",
            )
            LUNCH -> tr(
                "Đừng để nhật ký trống: thêm bữa trưa ngay khi ăn xong.",
                "Don't leave the diary empty: add lunch right after you eat.",
            )
            DINNER -> tr(
                "Tổng kết hôm nay trước khi ngủ - thêm bữa tối vào nhật ký.",
                "Wrap up today before bed - add dinner to your diary.",
            )
        }
        val extra = when {
            remainingKcal == null -> null
            remainingKcal <= 0 -> tr(
                "Hôm nay bạn đã ăn đủ mục tiêu calo.",
                "You have already reached today's calorie goal.",
            )
            else -> tr(
                "Còn $remainingKcal kcal so với mục tiêu hôm nay.",
                "$remainingKcal kcal left against today's goal.",
            )
        }
        return listOfNotNull(base, extra).joinToString(" ")
    }

    companion object {
        fun from(key: String?): MealSlot =
            entries.firstOrNull { it.key == key } ?: BREAKFAST
    }
}

/** Mot moc nhac cu the: bua nao, luc nao. */
data class ReminderMoment(val slot: MealSlot, val at: Calendar)

/**
 * Tinh moc nhac ke tiep tu danh sach bua dang bat.
 * Tach rieng khoi WorkManager de test duoc bang JVM thuan (xem ReminderPlannerTest).
 */
object ReminderPlanner {

    /**
     * Moc gan nhat con o phia truoc, tinh ca hom nay lan ngay mai.
     * Tra ve null khi nguoi dung tat het cac bua.
     */
    fun next(now: Calendar, slots: List<com.example.nutrivision.data.prefs.ReminderSlotSetting>): ReminderMoment? =
        slots.asSequence()
            .filter { it.enabled }
            .map { setting ->
                val at = (now.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, setting.hour)
                    set(Calendar.MINUTE, setting.minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    // Da qua gio nay trong hom nay thi hen sang mai.
                    if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
                }
                ReminderMoment(setting.slot, at)
            }
            .minByOrNull { it.at.timeInMillis }
}
