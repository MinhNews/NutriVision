package com.example.nutrivision.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * Hai bảng màu tĩnh. Token bên dưới chọn bảng theo [ThemePaletteState] nên mọi
 * chỗ đang dùng `TextPrimary`, `SurfaceLight`… tự đổi màu mà không phải sửa
 * từng màn hình.
 */
object LightPalette {
    val primary = Color(0xFF10B981)
    val primaryDark = Color(0xFF059669)
    val secondaryAmber = Color(0xFFF59E0B)
    val accentIndigo = Color(0xFF6366F1)

    val macroProtein = Color(0xFF3B82F6)
    val macroCarb = Color(0xFFF59E0B)
    val macroFat = Color(0xFFEF4444)

    val background = Color(0xFFF8FAFC)
    val surface = Color(0xFFFFFFFF)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)
    val border = Color(0xFFE2E8F0)

    val forestDeep = Color(0xFF064E3B)
    val forestInk = Color(0xFF064E3B)
    val mintSoft = Color(0xFFECFDF5)
    val mintMid = Color(0xFFD1FAE5)
    val limeAccent = Color(0xFF84CC16)
    val amberSoft = Color(0xFFFEF3C7)
    val indigoSoft = Color(0xFFEEF2FF)
    val surfaceMuted = Color(0xFFF1F5F9)
    val textMuted = Color(0xFF94A3B8)
    val dangerSoft = Color(0xFFFEE2E2)
    val shadowGreen = Color(0x3310B981)

    val heroStart = Color(0xFF0F9D72)
    val heroEnd = Color(0xFF10B981)
    val mintGradientTop = Color(0xFFF0FDF7)
    val mintGradientBottom = Color(0xFFF8FAFC)

    val mealBreakfast = Color(0xFFF59E0B)
    val mealLunch = Color(0xFF10B981)
    val mealDinner = Color(0xFF6366F1)
    val mealSnack = Color(0xFFEC4899)
}

object DarkPalette {
    // Xanh sáng hơn để đủ tương phản trên nền tối.
    val primary = Color(0xFF34D399)
    val primaryDark = Color(0xFF059669)
    val secondaryAmber = Color(0xFFFBBF24)
    val accentIndigo = Color(0xFF818CF8)

    val macroProtein = Color(0xFF60A5FA)
    val macroCarb = Color(0xFFFBBF24)
    val macroFat = Color(0xFFF87171)

    val background = Color(0xFF0B1220)
    val surface = Color(0xFF151E2E)
    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFF9FB0C7)
    val border = Color(0xFF2A3648)

    val forestDeep = Color(0xFF064E3B)
    val forestInk = Color(0xFFA7F3D0)
    val mintSoft = Color(0xFF10251C)
    val mintMid = Color(0xFF17362A)
    val limeAccent = Color(0xFFA3E635)
    val amberSoft = Color(0xFF33260F)
    val indigoSoft = Color(0xFF1B2340)
    val surfaceMuted = Color(0xFF1B2536)
    val textMuted = Color(0xFF7286A0)
    val dangerSoft = Color(0xFF3A1B1B)
    val shadowGreen = Color(0x5510B981)

    val heroStart = Color(0xFF0B6B4F)
    val heroEnd = Color(0xFF0F9D72)
    val mintGradientTop = Color(0xFF0D1B17)
    val mintGradientBottom = Color(0xFF0B1220)

    val mealBreakfast = Color(0xFFFBBF24)
    val mealLunch = Color(0xFF34D399)
    val mealDinner = Color(0xFF818CF8)
    val mealSnack = Color(0xFFF472B6)
}

/**
 * Snapshot state để đổi theme: composable nào đọc token màu sẽ tự recompose
 * khi giá trị này đổi. Chỉ [NutriVisionTheme] ghi vào đây.
 */
object ThemePaletteState {
    internal var isDark by mutableStateOf(false)
}

private fun pick(light: Color, dark: Color): Color =
    if (ThemePaletteState.isDark) dark else light

// Màu nhận diện thương hiệu NutriVision
val PrimaryGreen: Color get() = pick(LightPalette.primary, DarkPalette.primary)
val PrimaryGreenDark: Color get() = pick(LightPalette.primaryDark, DarkPalette.primaryDark)
val SecondaryAmber: Color get() = pick(LightPalette.secondaryAmber, DarkPalette.secondaryAmber)
val AccentIndigo: Color get() = pick(LightPalette.accentIndigo, DarkPalette.accentIndigo)

// Màu 3 nhóm chất dinh dưỡng vĩ mô (Macro)
val MacroProtein: Color get() = pick(LightPalette.macroProtein, DarkPalette.macroProtein)
val MacroCarb: Color get() = pick(LightPalette.macroCarb, DarkPalette.macroCarb)
val MacroFat: Color get() = pick(LightPalette.macroFat, DarkPalette.macroFat)

// Màu nền và màu trung tính
val BackgroundLight: Color get() = pick(LightPalette.background, DarkPalette.background)
val SurfaceLight: Color get() = pick(LightPalette.surface, DarkPalette.surface)
val TextPrimary: Color get() = pick(LightPalette.textPrimary, DarkPalette.textPrimary)
val TextSecondary: Color get() = pick(LightPalette.textSecondary, DarkPalette.textSecondary)
val BorderLight: Color get() = pick(LightPalette.border, DarkPalette.border)

val ForestDeep: Color get() = pick(LightPalette.forestDeep, DarkPalette.forestDeep)
/** Chữ/icon đặt trên nền mint: đậm ở nền sáng, sáng ở nền tối. */
val ForestInk: Color get() = pick(LightPalette.forestInk, DarkPalette.forestInk)
val MintSoft: Color get() = pick(LightPalette.mintSoft, DarkPalette.mintSoft)
val MintMid: Color get() = pick(LightPalette.mintMid, DarkPalette.mintMid)
val LimeAccent: Color get() = pick(LightPalette.limeAccent, DarkPalette.limeAccent)
val AmberSoft: Color get() = pick(LightPalette.amberSoft, DarkPalette.amberSoft)
val IndigoSoft: Color get() = pick(LightPalette.indigoSoft, DarkPalette.indigoSoft)
val SurfaceMuted: Color get() = pick(LightPalette.surfaceMuted, DarkPalette.surfaceMuted)
val TextMuted: Color get() = pick(LightPalette.textMuted, DarkPalette.textMuted)
val DangerSoft: Color get() = pick(LightPalette.dangerSoft, DarkPalette.dangerSoft)
val ShadowGreen: Color get() = pick(LightPalette.shadowGreen, DarkPalette.shadowGreen)

// Gradient
val HeroGradientStart: Color get() = pick(LightPalette.heroStart, DarkPalette.heroStart)
val HeroGradientEnd: Color get() = pick(LightPalette.heroEnd, DarkPalette.heroEnd)
val MintGradientTop: Color get() = pick(LightPalette.mintGradientTop, DarkPalette.mintGradientTop)
val MintGradientBottom: Color get() = pick(LightPalette.mintGradientBottom, DarkPalette.mintGradientBottom)

// Màu theo bữa
val MealBreakfast: Color get() = pick(LightPalette.mealBreakfast, DarkPalette.mealBreakfast)
val MealLunch: Color get() = pick(LightPalette.mealLunch, DarkPalette.mealLunch)
val MealDinner: Color get() = pick(LightPalette.mealDinner, DarkPalette.mealDinner)
val MealSnack: Color get() = pick(LightPalette.mealSnack, DarkPalette.mealSnack)
