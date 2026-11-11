package com.example.nutrivision.data.prefs

import android.content.Context
import com.example.nutrivision.domain.text.tr
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Chế độ hiển thị: theo điện thoại, luôn sáng hoặc luôn tối. */
enum class ThemeMode(val key: String, val labelVi: String, val labelEn: String) {
    SYSTEM("system", "Theo hệ thống", "System"),
    LIGHT("light", "Sáng", "Light"),
    DARK("dark", "Tối", "Dark");

    val label: String get() = tr(labelVi, labelEn)
}

/**
 * Lưu lựa chọn giao diện trên máy. Mặc định THEO HỆ THỐNG để app đổi theo
 * cài đặt sáng/tối của điện thoại; người dùng vẫn đổi tay được trong app.
 */
class ThemeManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private val _mode = MutableStateFlow(readMode())
    val mode: StateFlow<ThemeMode> = _mode.asStateFlow()

    fun setMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_MODE, mode.key).apply()
        _mode.value = mode
    }

    private fun readMode(): ThemeMode =
        ThemeMode.entries.firstOrNull { it.key == prefs.getString(KEY_MODE, null) }
            ?: ThemeMode.SYSTEM

    private companion object {
        const val PREFS = "nutrivision_theme"
        const val KEY_MODE = "theme_mode"
    }
}
