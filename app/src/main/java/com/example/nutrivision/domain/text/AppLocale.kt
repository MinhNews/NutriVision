package com.example.nutrivision.domain.text

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.nutrivision.data.prefs.AppLanguage

/**
 * Ngôn ngữ đang hiển thị, giữ trong snapshot state nên composable nào đọc chuỗi
 * qua [tr] sẽ tự vẽ lại khi người dùng đổi ngôn ngữ. Các hàm format ngày/giờ
 * cũng đọc giá trị này để chọn nhãn tiếng Việt hoặc tiếng Anh.
 */
object AppLocale {
    var language by mutableStateOf(AppLanguage.VIETNAMESE)
    val isEnglish: Boolean get() = language == AppLanguage.ENGLISH
}

/** Chọn chuỗi Việt/Anh theo ngôn ngữ đang đặt. */
fun tr(vi: String, en: String): String = if (AppLocale.isEnglish) en else vi
