package com.example.nutrivision.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.nutrivision.MainActivity
import com.example.nutrivision.R
import com.example.nutrivision.data.prefs.ReminderSettings
import com.example.nutrivision.domain.text.tr
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Hẹn một lần nhắc kế tiếp rồi tự hẹn tiếp sau khi chạy.
 *
 * Cách này chính xác tới phút và không đánh thức máy mỗi 15 phút như
 * PeriodicWorkRequest, đổi lại phải tự lên lịch lại trong worker.
 *
 * [settings] phải là instance dùng chung với UI (AppContainer) - nếu tạo instance
 * riêng thì đọc phải giá trị cũ và lịch sẽ không bao giờ được đặt.
 */
class ReminderScheduler(
    private val context: Context,
    private val settings: ReminderSettings = ReminderSettings(context),
) {

    /** Gọi khi người dùng đổi giờ/bật tắt và mỗi lần mở app để chắc chắn lịch còn đúng. */
    fun sync() {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(WORK_NAME)
        val state = settings.reload()
        if (!state.anyEnabled) return
        ensureChannel(context)
        scheduleNext(workManager, state)
    }

    fun scheduleNext(
        workManager: WorkManager = WorkManager.getInstance(context),
        state: com.example.nutrivision.data.prefs.ReminderState = settings.reload(),
    ) {
        val now = Calendar.getInstance()
        val moment = ReminderPlanner.next(now, state.slots) ?: return
        val delay = (moment.at.timeInMillis - now.timeInMillis).coerceAtLeast(1_000L)
        val request = OneTimeWorkRequestBuilder<MealReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(Data.Builder().putString(MealReminderWorker.KEY_SLOT, moment.slot.key).build())
            .build()
        workManager.enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    companion object {
        private const val WORK_NAME = "meal_reminder"
        const val CHANNEL_ID = "meal_reminders"
        private const val NOTIFICATION_ID = 4100

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            if (manager.getNotificationChannel(CHANNEL_ID) != null) return
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    tr("Nhắc nhở giờ ăn", "Meal reminders"),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = tr(
                        "Nhắc ghi nhật ký vào các bữa bạn đã chọn giờ.",
                        "Reminds you to log the meals you scheduled.",
                    )
                }
            )
        }

        /** Hiển thị thông báo; trả về false nếu người dùng chưa cho phép thông báo. */
        fun notify(context: Context, slot: MealSlot, remainingKcal: Int?): Boolean {
            if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
            ensureChannel(context)
            val open = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            val body = slot.body(remainingKcal)
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_meal_reminder)
                .setContentTitle(slot.title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setContentIntent(open)
                .setAutoCancel(true)
                .build()
            return runCatching {
                NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
                true
            }.getOrDefault(false)
        }
    }
}
