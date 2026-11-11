package com.example.nutrivision.ui.screens

import com.example.nutrivision.domain.text.tr
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Female
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Male
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nutrivision.domain.calc.NutritionCalculator
import com.example.nutrivision.domain.model.ActivityLevel
import com.example.nutrivision.domain.model.Goal
import com.example.nutrivision.domain.model.UserProfile
import com.example.nutrivision.ui.components.AppBackground
import com.example.nutrivision.ui.components.AppTextField
import com.example.nutrivision.ui.components.GradientButton
import com.example.nutrivision.ui.components.SectionHeader
import com.example.nutrivision.ui.theme.BorderLight
import com.example.nutrivision.ui.theme.ForestDeep
import com.example.nutrivision.ui.theme.ForestInk
import com.example.nutrivision.ui.theme.MintMid
import com.example.nutrivision.ui.theme.PrimaryGreen
import com.example.nutrivision.ui.theme.SurfaceLight
import com.example.nutrivision.ui.theme.TextMuted
import com.example.nutrivision.ui.theme.TextPrimary
import com.example.nutrivision.ui.theme.TextSecondary
import com.example.nutrivision.ui.util.grouped
import kotlin.math.roundToInt
import java.util.Locale

@Composable
fun ProfileSetupScreen(
    initial: UserProfile?,
    draftEmail: String,
    draftName: String,
    canGoBack: Boolean,
    onBack: () -> Unit,
    onSave: (
        email: String,
        name: String,
        gender: String,
        age: Int,
        heightCm: Double,
        weightKg: Double,
        activity: ActivityLevel,
        goal: Goal,
    ) -> Unit,
) {
    val email = initial?.email ?: draftEmail
    var name by remember { mutableStateOf(initial?.displayName ?: draftName) }
    var gender by remember { mutableStateOf(initial?.gender ?: UserProfile.GENDER_MALE) }
    var ageText by remember { mutableStateOf(initial?.age?.toString() ?: "") }
    var heightText by remember {
        mutableStateOf(initial?.heightCm?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "")
    }
    var weightText by remember {
        mutableStateOf(initial?.weightKg?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "")
    }
    var activity by remember { mutableStateOf(initial?.activityLevel ?: ActivityLevel.MODERATE) }
    var goal by remember { mutableStateOf(initial?.goal ?: Goal.MAINTAIN) }
    var error by remember { mutableStateOf<String?>(null) }

    val age = ageText.toIntOrNull()
    val height = heightText.replace(',', '.').toDoubleOrNull()
    val weight = weightText.replace(',', '.').toDoubleOrNull()
    val inputsValid = age != null && age in 10..100 &&
        height != null && height in 100.0..250.0 &&
        weight != null && weight in 25.0..300.0

    val preview = if (inputsValid) {
        val bmr = NutritionCalculator.bmr(gender, age!!, height!!, weight!!)
        val tdee = NutritionCalculator.tdee(bmr, activity)
        val targets = NutritionCalculator.targets(gender, age, height, weight, activity, goal)
        Triple(bmr, tdee, targets)
    } else null

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (canGoBack) {
                    Surface(
                        onClick = onBack,
                        shape = CircleShape,
                        color = SurfaceLight,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                        modifier = Modifier.size(40.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = tr("Quay lại", "Back"), tint = TextPrimary, modifier = Modifier.size(17.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                }
                Text(
                    if (initial == null) tr("Bước 2 / 2", "Step 2 / 2") else tr("Chỉnh sửa hồ sơ", "Edit profile"),
                    style = MaterialTheme.typography.labelLarge,
                    color = PrimaryGreen,
                )
            }

            Spacer(Modifier.height(18.dp))
            Text(
                if (initial == null) tr("Để app tính đúng\ncho riêng bạn", "So the app calculates\njust for you") else tr("Cập nhật thông tin\ncơ thể", "Update your\nbody details"),
                style = MaterialTheme.typography.displayMedium,
                color = TextPrimary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                tr("Các chỉ số dưới đây được dùng để tính BMR, TDEE và mục tiêu calo mỗi ngày.", "These numbers are used to calculate BMR, TDEE and your daily calorie goal."),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )

            Spacer(Modifier.height(22.dp))

            EmailBadge(email)

            Spacer(Modifier.height(14.dp))

            AppTextField(
                value = name,
                onValueChange = { name = it; error = null },
                label = tr("Tên hiển thị", "Display name"),
                placeholder = tr("Ví dụ: Minh Anh", "e.g. Minh Anh"),
            )

            Spacer(Modifier.height(16.dp))
            SectionHeader(title = tr("Giới tính", "Gender"))
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GenderCard(
                    label = tr("Nam", "Male"),
                    icon = Icons.Rounded.Male,
                    selected = gender == UserProfile.GENDER_MALE,
                    onClick = { gender = UserProfile.GENDER_MALE },
                    modifier = Modifier.weight(1f),
                )
                GenderCard(
                    label = tr("Nữ", "Female"),
                    icon = Icons.Rounded.Female,
                    selected = gender == UserProfile.GENDER_FEMALE,
                    onClick = { gender = UserProfile.GENDER_FEMALE },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppTextField(
                    value = ageText,
                    onValueChange = { ageText = it.filter(Char::isDigit); error = null },
                    label = tr("Tuổi", "Age"),
                    placeholder = "22",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                )
                AppTextField(
                    value = heightText,
                    onValueChange = { heightText = it.filter { c -> c.isDigit() || c == '.' || c == ',' }; error = null },
                    label = tr("Chiều cao", "Height"),
                    placeholder = "170",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f),
                )
                AppTextField(
                    value = weightText,
                    onValueChange = { weightText = it.filter { c -> c.isDigit() || c == '.' || c == ',' }; error = null },
                    label = tr("Cân nặng", "Weight"),
                    placeholder = "65",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(18.dp))
            SectionHeader(title = tr("Mức vận động", "Activity level"), subtitle = tr("Chọn mức gần đúng với một tuần của bạn", "Pick the closest match to your week"))
            Spacer(Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(SurfaceLight)
                    .border(1.dp, BorderLight, RoundedCornerShape(22.dp)),
            ) {
                ActivityLevel.entries.forEachIndexed { index, level ->
                    ActivityRow(
                        level = level,
                        selected = activity == level,
                        onClick = { activity = level },
                    )
                    if (index != ActivityLevel.entries.lastIndex) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(start = 52.dp, end = 16.dp)
                                .height(1.dp)
                                .background(BorderLight.copy(alpha = 0.7f))
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            SectionHeader(title = tr("Mục tiêu", "Goal"), subtitle = tr("Mục tiêu quyết định mức calo và đạm mỗi ngày", "Your goal sets daily calories and protein"))
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GoalCard(
                    goal = Goal.LOSE,
                    icon = Icons.Rounded.TrendingDown,
                    selected = goal == Goal.LOSE,
                    onClick = { goal = Goal.LOSE },
                    modifier = Modifier.weight(1f),
                )
                GoalCard(
                    goal = Goal.MAINTAIN,
                    icon = Icons.Rounded.Remove,
                    selected = goal == Goal.MAINTAIN,
                    onClick = { goal = Goal.MAINTAIN },
                    modifier = Modifier.weight(1f),
                )
                GoalCard(
                    goal = Goal.GAIN,
                    icon = Icons.Rounded.TrendingUp,
                    selected = goal == Goal.GAIN,
                    onClick = { goal = Goal.GAIN },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(20.dp))

            if (preview != null) {
                val (bmr, tdee, targets) = preview
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .background(Brush.linearGradient(listOf(ForestDeep, PrimaryGreen)))
                        .padding(20.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = Color(0xFFFFD166), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(7.dp))
                        Text(
                            tr("Mục tiêu mỗi ngày", "Daily target"),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.8f),
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            targets.calories.grouped(),
                            style = MaterialTheme.typography.displayMedium,
                            color = Color.White,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "kcal",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.75f),
                            modifier = Modifier.padding(bottom = 5.dp),
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        PreviewMetric("BMR", "${bmr.roundToInt().grouped()} kcal")
                        PreviewMetric("TDEE", "${tdee.roundToInt().grouped()} kcal")
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MacroPreviewChip(tr("Đạm", "Protein"), targets.proteinG, Color(0xFF93C5FD))
                        MacroPreviewChip("Carb", targets.carbsG, Color(0xFFFCD34D))
                        MacroPreviewChip(tr("Béo", "Fat"), targets.fatG, Color(0xFFFCA5A5))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        tr("Ước tính dựa trên thông tin cơ thể của bạn.", "Estimated from your body details."),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.62f),
                    )
                }
            } else {
                Text(
                    tr("Nhập đủ tuổi, chiều cao và cân nặng để xem mục tiêu ước tính.", "Enter age, height and weight to preview your estimated target."),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
            }

            if (error != null) {
                Spacer(Modifier.height(10.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(20.dp))
            GradientButton(
                text = if (initial == null) tr("Lưu hồ sơ và bắt đầu", "Save profile and start") else tr("Lưu thay đổi", "Save changes"),
                onClick = {
                    error = when {
                        name.trim().length < 2 -> tr("Hãy nhập tên hiển thị", "Enter a display name")
                        age == null || age !in 10..100 -> tr("Tuổi cần trong khoảng 10–100", "Age must be between 10 and 100")
                        height == null || height !in 100.0..250.0 -> tr("Chiều cao cần trong khoảng 100–250 cm", "Height must be between 100 and 250 cm")
                        weight == null || weight !in 25.0..300.0 -> tr("Cân nặng cần trong khoảng 25–300 kg", "Weight must be between 25 and 300 kg")
                        else -> null
                    }
                    if (error == null) {
                        onSave(
                            email,
                            name.trim(),
                            gender,
                            age!!,
                            height!!,
                            weight!!,
                            activity,
                            goal,
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun EmailBadge(email: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MintMid.copy(alpha = 0.45f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Email, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(19.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(tr("Tài khoản", "Account"), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(
                email.ifBlank { tr("Chưa có email", "No email") },
                style = MaterialTheme.typography.bodyMedium,
                color = ForestInk,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun GenderCard(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val border by animateColorAsState(if (selected) PrimaryGreen else BorderLight, label = "genderBorder")
    val bg by animateColorAsState(if (selected) MintMid.copy(alpha = 0.55f) else SurfaceLight, label = "genderBg")
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .border(if (selected) 1.5.dp else 1.dp, border, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = if (selected) PrimaryGreen else TextSecondary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) ForestInk else TextPrimary,
        )
    }
}

@Composable
private fun ActivityRow(
    level: ActivityLevel,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (selected) PrimaryGreen.copy(alpha = 0.13f) else Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.DirectionsRun,
                contentDescription = null,
                tint = if (selected) PrimaryGreen else TextMuted,
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(level.label, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Text(level.description, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .border(2.dp, if (selected) PrimaryGreen else BorderLight, CircleShape)
                .background(if (selected) PrimaryGreen else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(Color.White))
            }
        }
    }
}

@Composable
private fun GoalCard(
    goal: Goal,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val border by animateColorAsState(if (selected) PrimaryGreen else BorderLight, label = "goalBorder")
    val bg by animateColorAsState(if (selected) MintMid.copy(alpha = 0.55f) else SurfaceLight, label = "goalBg")
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .border(if (selected) 1.5.dp else 1.dp, border, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (selected) PrimaryGreen else TextSecondary,
            modifier = Modifier.size(21.dp),
        )
        Spacer(Modifier.height(7.dp))
        Text(
            goal.label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) ForestInk else TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun PreviewMetric(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
        Text(value, style = MaterialTheme.typography.titleSmall, color = Color.White)
    }
}

@Composable
private fun MacroPreviewChip(label: String, grams: Double, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(
            "$label ${grams.roundToInt()} g",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
        )
    }
}
