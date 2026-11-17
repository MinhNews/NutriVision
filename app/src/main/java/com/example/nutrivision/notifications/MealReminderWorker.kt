package com.example.nutrivision.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nutrivision.NutriVisionApp
import com.example.nutrivision.data.prefs.ReminderSettings
import com.example.nutrivision.domain.calc.DateUtils
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.roundToInt

/**
 * Bắn thông báo cho đúng bữa đã hẹn, rồi tự hẹn bữa kế tiếp.
 *
 * "Thông minh" ở chỗ:
 * - người dùng đã tắt bữa đó hoặc tắt hết thì không làm gì;
 * - hôm nay bữa đó đã có món thì bỏ qua, không nhắc lại việc đã làm;
 * - nội dung thông báo kèm số kcal còn thiếu so với mục tiêu (dữ liệu thật trong DB).
 */
class MealReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val settings = ReminderSettings(applicationContext)
        val slot = MealSlot.from(inputData.getString(KEY_SLOT))
        val state = settings.reload()
        val scheduler = ReminderScheduler(applicationContext, settings)

        // Tắt trong lúc chờ (hoặc tắt đúng bữa này) thì chỉ hẹn mốc kế tiếp.
        if (!state.of(slot).enabled) {
            scheduler.scheduleNext(state = state)
            return Result.success()
        }

        val app = applicationContext as? NutriVisionApp
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        var remainingKcal: Int? = null

        if (app != null && userId != null) {
            val today = DateUtils.todayKey()
            val mealDao = app.container.database.mealDao()
            val logged = runCatching {
                mealDao.countMealTypeItems(userId, today, slot.key) > 0
            }.getOrDefault(false)
            if (logged) {
                // Bữa này đã ghi rồi - không nhắc nữa, chỉ hẹn bữa kế.
                scheduler.scheduleNext(state = state)
                return Result.success()
            }
            remainingKcal = runCatching {
                val target = app.container.database.userDao().getUser(userId)?.targetCalories
                if (target == null || target <= 0) {
                    null
                } else {
                    (target - mealDao.totalCalories(userId, today)).roundToInt().coerceAtLeast(0)
                }
            }.getOrNull()
        }

        ReminderScheduler.notify(applicationContext, slot, remainingKcal)
        scheduler.scheduleNext(state = state)
        return Result.success()
    }

    companion object {
        const val KEY_SLOT = "slot"
    }
}
