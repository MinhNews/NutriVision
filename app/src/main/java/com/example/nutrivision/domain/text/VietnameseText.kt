package com.example.nutrivision.domain.text

import java.text.Normalizer

/** Chuẩn hoá tiếng Việt để tìm không dấu: "Phở bò tái nạm" → "pho bo tai nam". */
fun String.toSearchKey(): String {
    val withoutMarks = Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .replace('đ', 'd')
        .replace('Đ', 'D')
        .lowercase()
    return withoutMarks
        .replace(Regex("[^a-z0-9\\s]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}

private val drinkKeywords = listOf(
    "trà sữa", "trà đá", "trà chanh", "trà sen", "trà", "cà phê", "cafe", "sinh tố",
    "nước ép", "nước mía", "nước dừa", "sữa tươi", "sữa chua uống", "bia", "rượu",
    "nước ngọt", "nước tăng lực", "matcha", "latte", "cocacola", "coca",
)

/** Đoán nhóm món từ tên để bật/tắt bộ lọc đồ uống. Không phải dữ liệu dinh dưỡng. */
fun String.guessFoodKind(): String =
    if (drinkKeywords.any { lowercase().contains(it) }) "drink" else "dish"

fun String.displayOrNull(): String? = trim().ifBlank { null }
