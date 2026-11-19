package com.example.nutrivision.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.nutrivision.domain.text.tr
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationManagerCompat
import com.example.nutrivision.BuildConfig
import com.example.nutrivision.R
import com.example.nutrivision.data.prefs.AppLanguage
import com.example.nutrivision.data.prefs.ReminderState
import com.example.nutrivision.data.prefs.ThemeMode
import com.example.nutrivision.data.prefs.displayName
import com.example.nutrivision.domain.model.UserProfile
import com.example.nutrivision.notifications.MealSlot
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ProfileScreen(
    profile: UserProfile,
    accountEmail: String,
    emailVerified: Boolean,
    syncing: Boolean,
    lastSyncAt: Long,
    syncError: String?,
    reminders: ReminderState,
    onSetReminderEnabled: (MealSlot, Boolean) -> Unit,
    onSetReminderTime: (MealSlot, Int, Int) -> Unit,
    onResendVerification: () -> Unit,
    onRefreshVerification: () -> Unit,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
) {
    val themeController = LocalThemeController.current
    val languageController = LocalLanguageController.current
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    // Bữa đang chờ cấp quyền thông báo (để bật lại đúng bữa đó sau khi người dùng đồng ý).
    var pendingSlot by remember { mutableStateOf(MealSlot.BREAKFAST) }

    // Từ Android 13 thông báo cần quyền runtime - xin đúng lúc người dùng bật nhắc nhở.
    val notificationPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) onSetReminderEnabled(pendingSlot, true) }

    fun changeReminder(slot: MealSlot, enabled: Boolean) {
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (enabled && needsPermission) {
            pendingSlot = slot
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onSetReminderEnabled(slot, enabled)
        }
    }

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
                )
                SettingsRow(
                    icon = Icons.Rounded.CloudSync,
                    title = tr("Đồng bộ", "Sync"),
                    value = when {
                        syncing -> tr("Đang đồng bộ…", "Syncing…")
                        syncError != null -> tr("Lỗi đồng bộ", "Sync error")
                        lastSyncAt <= 0L -> tr("Chưa đồng bộ", "Not synced")
                        else -> syncLabel(lastSyncAt)
                    },
                    valueColor = if (syncError != null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        PrimaryGreen
                    },
                    note = syncError,
                    noteColor = MaterialTheme.colorScheme.error,
                    showDivider = false,
                )
            }
        }

        item {
            EmailVerificationCard(
                email = accountEmail,
                verified = emailVerified,
                onResend = onResendVerification,
                onRefresh = onRefreshVerification,
            )
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
                    value = reminders.enabledTimesLabel ?: tr("Tắt", "Off"),
                    valueColor = if (reminders.anyEnabled) PrimaryGreen else TextSecondary,
                    onClick = { showReminderDialog = true },
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
                    icon = Icons.Rounded.Info,
                    title = tr("Về NutriVision", "About NutriVision"),
                    value = tr(
                        "Phiên bản ${BuildConfig.VERSION_NAME}",
                        "Version ${BuildConfig.VERSION_NAME}",
                    ),
                    onClick = { showAboutDialog = true },
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
                "NutriVision ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
    }

    if (showAboutDialog) {
        AboutDialog(
            accountEmail = accountEmail,
            emailVerified = emailVerified,
            onDismiss = { showAboutDialog = false },
        )
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

    if (showReminderDialog) {
        ReminderDialog(
            state = reminders,
            onToggle = ::changeReminder,
            onTimeChange = onSetReminderTime,
            onDismiss = { showReminderDialog = false },
        )
    }
}

/**
 * Hộp thoại chỉnh nhắc nhở: mỗi bữa một công tắc và một nút giờ.
 * Bấm vào giờ mở TimePicker của hệ thống (đồng hồ 24 giờ) - không cần tự vẽ.
 */
@Composable
private fun ReminderDialog(
    state: ReminderState,
    onToggle: (MealSlot, Boolean) -> Unit,
    onTimeChange: (MealSlot, Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("Nhắc nhở giờ ăn", "Meal reminders")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    tr(
                        "Chọn giờ cho từng bữa. Bữa nào hôm nay đã ghi rồi thì app không nhắc nữa.",
                        "Pick a time per meal. If that meal is already logged today, we stay quiet.",
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                // Nhắc nhở chỉ có tác dụng khi hệ điều hành cho phép thông báo - nói rõ thay vì im lặng.
                if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                    Text(
                        tr(
                            "Thông báo đang bị tắt cho NutriVision nên nhắc nhở sẽ không hiện.",
                            "Notifications are turned off for NutriVision, so reminders will not appear.",
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                    TextButton(
                        contentPadding = PaddingValues(0.dp),
                        onClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        },
                    ) {
                        Text(tr("Mở cài đặt thông báo", "Open notification settings"))
                    }
                }
                state.slots.forEach { setting ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                setting.slot.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                            )
                            TextButton(
                                onClick = {
                                    TimePickerDialog(
                                        context,
                                        { _, hour, minute ->
                                            onTimeChange(setting.slot, hour, minute)
                                        },
                                        setting.hour,
                                        setting.minute,
                                        true,
                                    ).show()
                                },
                                contentPadding = PaddingValues(0.dp),
                            ) {
                                Text(
                                    setting.timeLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (setting.enabled) PrimaryGreen else TextMuted,
                                )
                            }
                        }
                        Switch(
                            checked = setting.enabled,
                            onCheckedChange = { onToggle(setting.slot, it) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(tr("Xong", "Done")) }
        },
    )
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

/**
 * Hộp thoại "Về NutriVision": chỉ hiện thông tin thật lấy từ app — phiên bản build,
 * tài khoản đang đăng nhập, và những gì app thực sự làm với dữ liệu của người dùng.
 */
@Composable
private fun AboutDialog(
    accountEmail: String,
    emailVerified: Boolean,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("Về NutriVision", "About NutriVision"), style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = null,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(17.dp)),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "NutriVision",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            tr(
                                "Phiên bản ${BuildConfig.VERSION_NAME} (bản ${BuildConfig.VERSION_CODE})",
                                "Version ${BuildConfig.VERSION_NAME} (build ${BuildConfig.VERSION_CODE})",
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                        )
                    }
                }

                Text(
                    tr(
                        "Nhận diện món ăn bằng AI ngay trên máy, tính dinh dưỡng theo khẩu phần và đồng bộ nhật ký giữa các thiết bị của bạn.",
                        "Recognise dishes with on-device AI, work out nutrition for each portion, and keep your diary in sync across your devices.",
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )

                AboutRow(
                    icon = Icons.Rounded.AutoAwesome,
                    title = tr("Nhận diện trên máy", "On-device recognition"),
                    detail = tr(
                        "Mô hình TFLite chạy trong máy, dùng được khi không có mạng.",
                        "A TFLite model runs on the device and works offline.",
                    ),
                )
                AboutRow(
                    icon = Icons.Rounded.CameraAlt,
                    title = tr("Ảnh chỉ rời máy khi bạn đồng ý", "Photos leave only when you ask"),
                    detail = tr(
                        "Ảnh được gửi lên Gemini chỉ khi bạn bấm \"Nhờ AI mô tả\".",
                        "A photo is sent to Gemini only when you tap \"Ask AI to describe\".",
                    ),
                )
                AboutRow(
                    icon = Icons.Rounded.CloudSync,
                    title = tr("Đồng bộ tự động", "Automatic sync"),
                    detail = tr(
                        "Nhật ký và ảnh món tự đồng bộ qua Firebase cho các thiết bị cùng tài khoản.",
                        "Your diary and meal photos sync through Firebase across your own devices.",
                    ),
                )
                AboutRow(
                    icon = Icons.Rounded.Lock,
                    title = tr("Tài khoản riêng", "Private account"),
                    detail = tr(
                        "Mỗi tài khoản chỉ đọc và ghi được dữ liệu của chính mình.",
                        "Each account can only read and write its own data.",
                    ),
                )
                AboutRow(
                    icon = Icons.Rounded.Email,
                    title = tr("Đang đăng nhập", "Signed in as"),
                    detail = if (emailVerified) {
                        tr("$accountEmail · đã xác thực", "$accountEmail · verified")
                    } else {
                        tr("$accountEmail · chưa xác thực email", "$accountEmail · email not verified")
                    },
                )

                Text(
                    tr(
                        "Số liệu dinh dưỡng là ước lượng để tham khảo, không thay thế tư vấn y tế.",
                        "Nutrition numbers are estimates for reference only, not medical advice.",
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(tr("Đóng", "Close"), color = PrimaryGreen)
            }
        },
        containerColor = SurfaceLight,
        shape = RoundedCornerShape(24.dp),
    )
}

@Composable
private fun AboutRow(
    icon: ImageVector,
    title: String,
    detail: String,
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MintMid.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = ForestInk, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                detail,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
        }
    }
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
    note: String? = null,
    noteColor: Color = TextSecondary,
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
        if (note != null) {
            Text(
                note,
                style = MaterialTheme.typography.labelSmall,
                color = noteColor,
                modifier = Modifier.padding(start = 59.dp, end = 14.dp, bottom = 10.dp),
            )
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

@Composable
private fun EmailVerificationCard(
    email: String,
    verified: Boolean,
    onResend: () -> Unit,
    onRefresh: () -> Unit,
) {
    val tint = if (verified) PrimaryGreen else SecondaryAmber
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(if (verified) MintMid.copy(alpha = 0.45f) else SecondaryAmber.copy(alpha = 0.14f))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (verified) Icons.Rounded.Verified else Icons.Rounded.Email,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (verified) {
                        tr("Email đã xác thực", "Email verified")
                    } else {
                        tr("Email chưa xác thực", "Email not verified")
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                )
                Text(
                    email,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (!verified) {
            Spacer(Modifier.height(6.dp))
            Text(
                tr(
                    "Xác thực email để bảo vệ tài khoản và khôi phục mật khẩu khi quên.",
                    "Verify your email to protect your account and recover a forgotten password.",
                ),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
            Row {
                TextButton(onClick = onResend) {
                    Text(tr("Gửi lại link", "Resend link"), color = PrimaryGreen)
                }
                TextButton(onClick = onRefresh) {
                    Text(tr("Tôi đã xác thực", "I have verified"), color = PrimaryGreen)
                }
            }
        }
    }
}

private fun syncLabel(lastSyncAt: Long): String =
    if (lastSyncAt <= 0L) {
        tr("Chưa đồng bộ", "Not synced")
    } else {
        SimpleDateFormat("dd/MM HH:mm", Locale.US).format(Date(lastSyncAt))
    }

private fun initialsOf(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts.first().take(1).uppercase()
        else -> (parts.first().take(1) + parts.last().take(1)).uppercase()
    }
}
