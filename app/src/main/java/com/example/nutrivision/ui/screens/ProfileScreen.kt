package com.example.nutrivision.ui.screens

import com.example.nutrivision.domain.text.tr
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.nutrivision.domain.model.UserProfile
import com.example.nutrivision.data.prefs.AppLanguage
import com.example.nutrivision.data.prefs.ThemeMode
import com.example.nutrivision.data.prefs.displayName
import com.example.nutrivision.ui.components.GlassCard
import com.example.nutrivision.ui.theme.BorderLight
import com.example.nutrivision.ui.theme.ForestDeep
import com.example.nutrivision.ui.theme.ForestInk
import com.example.nutrivision.ui.theme.HeroGradientEnd
import com.example.nutrivision.ui.theme.HeroGradientStart
import com.example.nutrivision.ui.theme.LocalThemeController
import com.example.nutrivision.ui.theme.MintMid
import com.example.nutrivision.ui.theme.PrimaryGreen
import com.example.nutrivision.ui.theme.SecondaryAmber
import com.example.nutrivision.ui.theme.SurfaceLight
import com.example.nutrivision.ui.theme.TextMuted
import com.example.nutrivision.ui.theme.TextPrimary
import com.example.nutrivision.ui.theme.TextSecondary
import com.example.nutrivision.ui.text.LocalLanguageController
import com.example.nutrivision.ui.util.grouped
import com.example.nutrivision.ui.util.oneDecimal
import kotlin.math.roundToInt

@Composable
fun ProfileScreen(
    profile: UserProfile,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
) {
    val themeController = LocalThemeController.current
    val languageController = LocalLanguageController.current
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column {
                Text(tr("Cá nhân", "Profile"), style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                Text(
                    tr("Hồ sơ và mục tiêu dinh dưỡng của bạn", "Your profile and nutrition goals"),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceLight)
                    .border(1.dp, BorderLight, RoundedCornerShape(24.dp))
                    .clickable(onClick = onEditProfile)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MintMid.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        initialsOf(profile.displayName ?: profile.email),
                        style = MaterialTheme.typography.titleLarge,
                        color = ForestInk,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        profile.displayName ?: tr("Người dùng NutriVision", "NutriVision user"),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        profile.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Icon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(ForestDeep, HeroGradientStart, HeroGradientEnd)))
                    .padding(18.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            tr("Mục tiêu hằng ngày", "Daily goal"),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.7f),
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                profile.targetCalories.grouped(),
                                style = MaterialTheme.typography.displayMedium,
                                color = Color.White,
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                "kcal",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White.copy(alpha = 0.75f),
                                modifier = Modifier.padding(bottom = 6.dp),
                            )
                        }
                    }
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.14f))
                            .clickable(onClick = onEditProfile)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text(
                            tr("Đổi mục tiêu", "Change goal"),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    GoalMetric("TDEE", "${profile.tdee.roundToInt().grouped()} kcal")
                    GoalMetric(tr("Mục tiêu", "Goal"), profile.goal.label)
                    GoalMetric(tr("Vận động", "Activity"), profile.activityLevel.label)
                }
            }
        }

        item { SectionLabel(tr("TÀI KHOẢN", "ACCOUNT")) }

        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                SettingsRow(
                    icon = Icons.Rounded.Person,
                    title = tr("Hồ sơ & mục tiêu", "Profile & goals"),
                    value = "${profile.heightCm.roundToInt()} cm · ${profile.weightKg.oneDecimal()} kg",
                    onClick = onEditProfile,
                )
                SettingsRow(
                    icon = Icons.Rounded.MonitorWeight,
                    title = tr("Chỉ số BMI", "BMI"),
                    value = "${profile.bmi.oneDecimal()} · ${profile.bmiLabel}",
                    valueColor = SecondaryAmber,
                    showDivider = false,
                )
            }
        }

        item { SectionLabel(tr("CÀI ĐẶT", "SETTINGS")) }

        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                SettingsRow(
                    icon = Icons.Rounded.Palette,
                    title = tr("Giao diện", "Appearance"),
                    value = themeController?.mode?.label ?: ThemeMode.SYSTEM.label,
                    onClick = if (themeController != null) {
                        { showThemeDialog = true }
                    } else {
                        null
                    },
                )
                SettingsRow(
                    icon = Icons.Rounded.NotificationsActive,
                    title = tr("Nhắc nhở giờ ăn", "Meal reminders"),
                    value = tr("Sắp có", "Coming soon"),
                )
                SettingsRow(
                    icon = Icons.Rounded.Translate,
                    title = tr("Ngôn ngữ", "Language"),
                    value = languageController?.language?.displayName
                        ?: AppLanguage.VIETNAMESE.displayName,
                    onClick = if (languageController != null) {
                        { showLanguageDialog = true }
                    } else {
                        null
                    },
                    showDivider = false,
                )
            }
        }

        item { SectionLabel(tr("KHÁC", "OTHER")) }

        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                SettingsRow(
                    icon = Icons.Rounded.HelpOutline,
                    title = tr("Trợ giúp & phản hồi", "Help & feedback"),
                    value = tr("Sắp có", "Coming soon"),
                )
                SettingsRow(
                    icon = Icons.Rounded.Info,
                    title = tr("Về NutriVision", "About NutriVision"),
                    value = tr("Phiên bản 1.0", "Version 1.0"),
                    showDivider = false,
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceLight.copy(alpha = 0.8f))
                    .border(1.dp, BorderLight, RoundedCornerShape(20.dp))
                    .clickable(onClick = onLogout)
                    .padding(vertical = 15.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Rounded.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    tr("Đăng xuất", "Sign out"),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        item {
            Text(
                "NutriVision 1.0",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
    }

    if (showThemeDialog && themeController != null) {
        ThemePickerDialog(
            current = themeController.mode,
            onSelect = { mode ->
                themeController.onChange(mode)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false },
        )
    }

    if (showLanguageDialog && languageController != null) {
        LanguagePickerDialog(
            current = languageController.language,
            onSelect = { language ->
                languageController.onChange(language)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false },
        )
    }
}

@Composable
private fun LanguagePickerDialog(
    current: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("Ngôn ngữ", "Language"), style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                AppLanguage.entries.forEach { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelect(language) }
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                // Tên bản ngữ để người dùng nhận ra cả khi đang ở ngôn ngữ kia.
                                if (language == AppLanguage.ENGLISH) language.labelEn else language.labelVi,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary,
                                fontWeight = if (language == current) FontWeight.SemiBold else FontWeight.Normal,
                            )
                            Text(
                                when (language) {
                                    AppLanguage.VIETNAMESE -> tr("Ngôn ngữ mặc định", "Default language")
                                    AppLanguage.ENGLISH -> tr("Giao diện tiếng Anh", "English interface")
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                            )
                        }
                        if (language == current) {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(tr("Xong", "Done"), color = PrimaryGreen)
            }
        },
        containerColor = SurfaceLight,
        shape = RoundedCornerShape(24.dp),
    )
}

@Composable
private fun ThemePickerDialog(
    current: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("Giao diện", "Appearance"), style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                ThemeMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelect(mode) }
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                mode.label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary,
                                fontWeight = if (mode == current) FontWeight.SemiBold else FontWeight.Normal,
                            )
                            Text(
                                when (mode) {
                                    ThemeMode.SYSTEM -> tr("Đổi theo cài đặt của điện thoại", "Follow the phone setting")
                                    ThemeMode.LIGHT -> tr("Luôn dùng nền sáng", "Always use light")
                                    ThemeMode.DARK -> tr("Luôn dùng nền tối", "Always use dark")
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                            )
                        }
                        if (mode == current) {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(tr("Xong", "Done"), color = PrimaryGreen)
            }
        },
        containerColor = SurfaceLight,
        shape = RoundedCornerShape(24.dp),
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = TextMuted,
        modifier = Modifier.padding(start = 4.dp),
    )
}

@Composable
private fun GoalMetric(label: String, value: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.6f),
        )
        Text(
            value,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    value: String?,
    onClick: (() -> Unit)? = null,
    valueColor: Color = TextSecondary,
    showDivider: Boolean = true,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
                )
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(MintMid.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = ForestInk, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(11.dp))
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                modifier = Modifier.weight(1f),
            )
            if (value != null) {
                Text(
                    value,
                    style = MaterialTheme.typography.labelMedium,
                    color = valueColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (onClick != null) {
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(17.dp),
                )
            }
        }
        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 59.dp, end = 14.dp)
                    .height(1.dp)
                    .background(BorderLight.copy(alpha = 0.8f))
            )
        }
    }
}

private fun initialsOf(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts.first().take(1).uppercase()
        else -> (parts.first().take(1) + parts.last().take(1)).uppercase()
    }
}
