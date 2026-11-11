package com.example.nutrivision.ui.text

import androidx.compose.runtime.staticCompositionLocalOf
import com.example.nutrivision.data.prefs.AppLanguage

/** Ngôn ngữ hiện tại + hàm đổi, để Cá nhân mở hộp chọn ngôn ngữ. */
data class LanguageController(
    val language: AppLanguage,
    val onChange: (AppLanguage) -> Unit,
)

val LocalLanguageController = staticCompositionLocalOf<LanguageController?> { null }
