package com.example.nutrivision.ui.screens

import com.example.nutrivision.domain.text.tr
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrivision.domain.calc.DateUtils
import com.example.nutrivision.domain.model.MealType
import com.example.nutrivision.ui.components.AnimatedInt
import com.example.nutrivision.ui.components.AppBackground
import com.example.nutrivision.ui.components.CalorieRing
import com.example.nutrivision.ui.components.EmptyState
import com.example.nutrivision.ui.components.FoodAvatar
import com.example.nutrivision.ui.components.GlassCard
import com.example.nutrivision.ui.components.MacroProgress
import com.example.nutrivision.ui.components.MealTypeAvatar
import com.example.nutrivision.ui.components.SectionHeader
import com.example.nutrivision.ui.components.mealTypeColor
import com.example.nutrivision.ui.util.greetingForHour
import com.example.nutrivision.ui.util.grouped
import com.example.nutrivision.ui.viewmodel.DashboardUiState
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onOpenProfile: () -> Unit,
    onOpenDiary: () -> Unit,
    onAddMeal: (MealType) -> Unit,
) {
    val today = uiState.date
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val profile = uiState.profile
    val consumed = uiState.nutrition.calories
    val target = profile?.targetCalories
    val remaining = target?.let { it - consumed }
    val over = remaining != null && remaining < 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        greetingForHour(hour),
                        style = MaterialTheme.typography.bodyMedium,
                        color = com.example.nutrivision.ui.theme.TextSecondary,
                    )
                    Text(
                        profile?.displayName ?: tr("Bạn hôm nay thế nào?", "How are you today?"),
                        style = MaterialTheme.typography.headlineMedium,
                        color = com.example.nutrivision.ui.theme.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "${DateUtils.weekdayLong(today)}, ${DateUtils.display(today)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = com.example.nutrivision.ui.theme.TextMuted,
                    )
                }
                Surface(
                    onClick = onOpenProfile,
                    shape = CircleShape,
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        com.example.nutrivision.ui.theme.BorderLight,
                    ),
                    modifier = Modifier.size(48.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (profile?.displayName.isNullOrBlank()) {
                            Icon(Icons.Rounded.Person, contentDescription = tr("Hồ sơ", "Profile"), tint = com.example.nutrivision.ui.theme.PrimaryGreen)
                        } else {
                            Text(
                                initialsOf(profile?.displayName),
                                style = MaterialTheme.typography.titleMedium,
                                color = com.example.nutrivision.ui.theme.PrimaryGreen,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }

        item {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically { it / 5 },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    com.example.nutrivision.ui.theme.ForestDeep,
                                    com.example.nutrivision.ui.theme.HeroGradientStart,
                                    com.example.nutrivision.ui.theme.HeroGradientEnd,
                                )
                            )
                        )
                        .padding(20.dp),
                ) {
                    Canvas(modifier = Modifier.fillMaxWidth().height(0.dp)) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.08f),
                            radius = size.minDimension * 1.6f,
                            center = Offset(size.width * 0.95f, 0f),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            tr("Hôm nay", "Today"),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                        )
                        Spacer(Modifier.weight(1f))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.14f))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                if (target == null) tr("Chưa có mục tiêu", "No goal yet") else tr("Mục tiêu ${target.grouped()} kcal", "Goal ${target.grouped()} kcal"),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (over) tr("Vượt mục tiêu", "Over goal") else tr("Còn lại", "Remaining"),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White.copy(alpha = 0.72f),
                            )
                            if (remaining != null) {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    AnimatedInt(
                                        value = abs(remaining).roundToInt(),
                                        style = MaterialTheme.typography.displayLarge,
                                        color = if (over) Color(0xFFFFD166) else Color.White,
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        "kcal",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(bottom = 8.dp),
                                    )
                                }
                            } else {
                                Text(
                                    "—",
                                    style = MaterialTheme.typography.displayLarge,
                                    color = Color.White,
                                )
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                tr("Đã nạp ${consumed.roundToInt().grouped()} kcal", "${consumed.roundToInt().grouped()} kcal logged"),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.66f),
                            )
                        }

                        CalorieRing(
                            progress = uiState.nutrition.calories.toFloat() /
                                (profile?.targetCalories?.takeIf { it > 0 } ?: 1).toFloat(),
                            modifier = Modifier.size(126.dp),
                            gradientColors = if (over) {
                                listOf(Color(0xFFFFE08A), Color(0xFFF59E0B))
                            } else {
                                listOf(Color.White, Color(0xFFD9F99D))
                            },
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${uiState.nutrition.calories.roundToInt().grouped()}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                )
                                Text(
                                    "kcal",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f),
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.13f))
                    )
                    Spacer(Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            MacroProgress(
                                label = tr("Đạm", "Protein"),
                                consumed = uiState.nutrition.protein,
                                target = profile?.targetProtein ?: 0.0,
                                color = Color(0xFF93C5FD),
                                onDark = true,
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            MacroProgress(
                                label = "Carb",
                                consumed = uiState.nutrition.carbs,
                                target = profile?.targetCarbs ?: 0.0,
                                color = Color(0xFFFCD34D),
                                onDark = true,
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            MacroProgress(
                                label = tr("Béo", "Fat"),
                                consumed = uiState.nutrition.fat,
                                target = profile?.targetFat ?: 0.0,
                                color = Color(0xFFFCA5A5),
                                onDark = true,
                            )
                        }
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(18.dp)) {
                SectionHeader(
                    title = tr("Bữa ăn hôm nay", "Today's meals"),
                    subtitle = if (uiState.itemCount > 0) tr("${uiState.itemCount} món đã ghi", "${uiState.itemCount} items logged") else tr("Chưa ghi món nào", "Nothing logged yet"),
                    actionLabel = tr("Nhật ký", "Diary"),
                    onAction = onOpenDiary,
                )
                Spacer(Modifier.height(12.dp))

                if (uiState.itemCount == 0) {
                    EmptyState(
                        icon = Icons.Rounded.RestaurantMenu,
                        title = tr("Ngày mới, bữa ăn mới", "New day, new meal"),
                        description = tr("Thêm món đầu tiên để bắt đầu theo dõi hôm nay.", "Add your first item to start tracking today."),
                        actionLabel = tr("Thêm món", "Add food"),
                        onAction = { onAddMeal(MealType.BREAKFAST) },
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        MealType.entries.forEach { type ->
                            val meal = uiState.mealOf(type)
                            MealCard(
                                type = type,
                                itemsText = meal?.items?.joinToString(" · ") { it.foodName }.orEmpty(),
                                itemCount = meal?.items?.size ?: 0,
                                calories = meal?.nutrition?.calories ?: 0.0,
                                onClick = {
                                    if (meal == null) onAddMeal(type) else onOpenDiary()
                                },
                                onAddClick = { onAddMeal(type) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MealCard(
    type: MealType,
    itemsText: String,
    itemCount: Int,
    calories: Double,
    onClick: () -> Unit,
    onAddClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(mealTypeColor(type).copy(alpha = 0.055f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MealTypeAvatar(type = type, size = 42.dp)
        Spacer(Modifier.width(11.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                type.label,
                style = MaterialTheme.typography.titleSmall,
                color = com.example.nutrivision.ui.theme.TextPrimary,
            )
            Text(
                if (itemCount == 0) tr("Chưa có món", "No items") else itemsText,
                style = MaterialTheme.typography.labelSmall,
                color = com.example.nutrivision.ui.theme.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                if (itemCount == 0) "–" else "${calories.roundToInt().grouped()} kcal",
                style = MaterialTheme.typography.labelLarge,
                color = if (itemCount == 0) com.example.nutrivision.ui.theme.TextMuted else com.example.nutrivision.ui.theme.TextPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            if (itemCount > 0) {
                Text(
                    tr("$itemCount món", "$itemCount items"),
                    style = MaterialTheme.typography.labelSmall,
                    color = com.example.nutrivision.ui.theme.TextMuted,
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Surface(
            onClick = onAddClick,
            shape = CircleShape,
            color = mealTypeColor(type).copy(alpha = 0.13f),
            modifier = Modifier.size(32.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = tr("Thêm món vào ${type.label}", "Add food to ${type.label}"),
                    tint = mealTypeColor(type),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

private fun initialsOf(name: String?): String {
    if (name.isNullOrBlank()) return "?"
    val parts = name.trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts.first().take(1).uppercase()
        else -> (parts.first().take(1) + parts.last().take(1)).uppercase()
    }
}
