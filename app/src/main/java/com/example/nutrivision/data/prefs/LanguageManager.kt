package com.example.nutrivision.data.prefs

import android.content.Context
import com.example.nutrivision.domain.text.tr
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Ngôn ngữ giao diện. Mặc định tiếng Việt, đổi được trong Cá nhân. */
enum class AppLanguage(val key: String, val labelVi: String, val labelEn: String) {
    VIETNAMESE("vi", "Tiếng Việt", "Vietnamese"),
    ENGLISH("en", "Tiếng Anh", "English"),
}

/** Tên ngôn ngữ hiển thị theo ngôn ngữ đang dùng. */
val AppLanguage.displayName: String get() = tr(labelVi, labelEn)

/**
 * Lưu lựa chọn ngôn ngữ trên máy. Không dùng resource Android vì toàn bộ
 * chuỗi của app nằm trong Kotlin (xem [com.example.nutrivision.domain.text.tr]).
 */
class LanguageManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private val _language = MutableStateFlow(readLanguage())
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.key).apply()
        _language.value = language
    }

    private fun readLanguage(): AppLanguage =
        AppLanguage.entries.firstOrNull { it.key == prefs.getString(KEY_LANGUAGE, null) }
            ?: AppLanguage.VIETNAMESE

    private companion object {
        const val PREFS = "nutrivision_language"
        const val KEY_LANGUAGE = "language"
    }
}
