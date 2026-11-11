package com.example.nutrivision.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.nutrivision.data.prefs.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = LightPalette.primary,
    onPrimary = Color.White,
    primaryContainer = LightPalette.mintMid,
    onPrimaryContainer = LightPalette.forestDeep,
    secondary = LightPalette.secondaryAmber,
    onSecondary = Color.White,
    secondaryContainer = LightPalette.amberSoft,
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = LightPalette.accentIndigo,
    onTertiary = Color.White,
    tertiaryContainer = LightPalette.indigoSoft,
    background = LightPalette.background,
    onBackground = LightPalette.textPrimary,
    surface = LightPalette.surface,
    onSurface = LightPalette.textPrimary,
    surfaceVariant = LightPalette.surfaceMuted,
    onSurfaceVariant = LightPalette.textSecondary,
    outline = LightPalette.border,
    outlineVariant = LightPalette.border,
    error = LightPalette.macroFat,
    errorContainer = LightPalette.dangerSoft,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPalette.primary,
    onPrimary = Color(0xFF06281C),
    primaryContainer = DarkPalette.mintMid,
    onPrimaryContainer = DarkPalette.forestDeep,
    secondary = DarkPalette.secondaryAmber,
    onSecondary = Color(0xFF2A1C00),
    secondaryContainer = DarkPalette.amberSoft,
    onSecondaryContainer = DarkPalette.secondaryAmber,
    tertiary = DarkPalette.accentIndigo,
    onTertiary = Color(0xFF111633),
    tertiaryContainer = DarkPalette.indigoSoft,
    background = DarkPalette.background,
    onBackground = DarkPalette.textPrimary,
    surface = DarkPalette.surface,
    onSurface = DarkPalette.textPrimary,
    surfaceVariant = DarkPalette.surfaceMuted,
    onSurfaceVariant = DarkPalette.textSecondary,
    outline = DarkPalette.border,
    outlineVariant = DarkPalette.border,
    error = DarkPalette.macroFat,
    errorContainer = DarkPalette.dangerSoft,
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

/** Cho biết app đang chạy nền tối, dùng để chỉnh icon thanh hệ thống. */
val LocalIsDarkTheme = staticCompositionLocalOf { false }

/** Chế độ hiển thị hiện tại + hàm đổi, để màn Cá nhân mở hộp chọn. */
data class ThemeController(
    val mode: ThemeMode,
    val onChange: (ThemeMode) -> Unit,
)

val LocalThemeController = staticCompositionLocalOf<ThemeController?> { null }

/**
 * Theme của app: theo hệ thống mặc định, có thể ép Sáng/Tối từ cài đặt.
 * Không dùng dynamic color để bảng màu thương hiệu ổn định trên mọi máy.
 */
@Composable
fun NutriVisionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    ThemePaletteState.isDark = darkTheme
    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content,
        )
    }
}
