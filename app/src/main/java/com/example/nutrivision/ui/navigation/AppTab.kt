package com.example.nutrivision.ui.navigation

import com.example.nutrivision.domain.model.MealType
import com.example.nutrivision.domain.text.tr

enum class AppTab(private val labelVi: String, private val labelEn: String) {
    HOME("Trang chủ", "Home"),
    DIARY("Nhật ký", "Diary"),
    CAMERA("Chụp món", "Scan"),
    STATS("Thống kê", "Stats"),
    PROFILE("Cá nhân", "Profile");

    val label: String get() = tr(labelVi, labelEn)
}

sealed interface Route {
    data class Search(val mealType: MealType) : Route

    data class MealDetail(
        val foodId: String?,
        val itemId: String?,
        val mealType: MealType,
    ) : Route

    data object EditProfile : Route
}
