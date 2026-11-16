package com.example.nutrivision.data.prefs

import android.content.Context
import com.example.nutrivision.notifications.MealSlot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Cau hinh nhac nho cua mot bua: co bat khong, nhac luc may gio. */
data class ReminderSlotSetting(
    val slot: MealSlot,
    val enabled: Boolean,
    val hour: Int,
    val minute: Int,
) {
    val timeLabel: String get() = "${hour.twoDigits()}:${minute.twoDigits()}"

    private fun Int.twoDigits(): String = if (this < 10) "0$this" else toString()
}

data class ReminderState(val slots: List<ReminderSlotSetting>) {

    val anyEnabled: Boolean get() = slots.any { it.enabled }

    fun of(slot: MealSlot): ReminderSlotSetting =
        slots.firstOrNull { it.slot == slot } ?: slot.defaultSetting()

    /** "07:00 · 11:30 · 18:30" cho cac bua dang bat, hoac null khi tat het. */
    val enabledTimesLabel: String?
        get() = slots.filter { it.enabled }
            .sortedBy { it.hour * 60 + it.minute }
            .takeIf { it.isNotEmpty() }
            ?.joinToString(" · ") { it.timeLabel }

    companion object {
        fun defaults() = ReminderState(MealSlot.entries.map { it.defaultSetting() })
    }
}

fun MealSlot.defaultSetting() = ReminderSlotSetting(
    slot = this,
    enabled = true,
    hour = defaultHour,
    minute = defaultMinute,
)

/**
 * Gio nhac do nguoi dung tu chon cho tung bua (khong con co dinh 7:00/11:30/18:30).
 * Gia tri mac dinh chi dung cho lan dau cai app.
 */
class ReminderSettings(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Ban tuan 4 dau tien chi co mot cong tac chung - doc de giu nguyen lua chon cu. */
    private val legacyMasterEnabled: Boolean? =
        if (prefs.contains(KEY_LEGACY_ENABLED)) prefs.getBoolean(KEY_LEGACY_ENABLED, true) else null

    private val _state = MutableStateFlow(load())
    val state: StateFlow<ReminderState> = _state.asStateFlow()

    fun isEnabled(slot: MealSlot): Boolean = _state.value.of(slot).enabled

    fun setEnabled(slot: MealSlot, enabled: Boolean) {
        prefs.edit().putBoolean(keyEnabled(slot), enabled).apply()
        _state.value = load()
    }

    fun setTime(slot: MealSlot, hour: Int, minute: Int) {
        prefs.edit()
            .putInt(keyHour(slot), hour.coerceIn(0, 23))
            .putInt(keyMinute(slot), minute.coerceIn(0, 59))
            .apply()
        _state.value = load()
    }

    /** Doc lai tu SharedPreferences - dung cho worker chay o tien trinh khac. */
    fun reload(): ReminderState {
        _state.value = load()
        return _state.value
    }

    private fun load(): ReminderState = ReminderState(
        MealSlot.entries.map { slot ->
            ReminderSlotSetting(
                slot = slot,
                enabled = prefs.getBoolean(keyEnabled(slot), legacyMasterEnabled ?: true),
                hour = prefs.getInt(keyHour(slot), slot.defaultHour),
                minute = prefs.getInt(keyMinute(slot), slot.defaultMinute),
            )
        }
    )

    private fun keyEnabled(slot: MealSlot) = "enabled_${slot.key}"
    private fun keyHour(slot: MealSlot) = "hour_${slot.key}"
    private fun keyMinute(slot: MealSlot) = "minute_${slot.key}"

    companion object {
        private const val PREFS = "nutrivision_reminders"
        private const val KEY_LEGACY_ENABLED = "enabled"
    }
}
