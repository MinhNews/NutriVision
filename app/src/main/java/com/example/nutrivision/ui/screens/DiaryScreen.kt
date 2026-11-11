package com.example.nutrivision.ui.screens

import com.example.nutrivision.domain.text.tr
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.nutrivision.domain.calc.DateUtils
import com.example.nutrivision.domain.model.MealItem
import com.example.nutrivision.domain.model.MealType
import com.example.nutrivision.domain.model.MealWithItems
import com.example.nutrivision.domain.text.guessFoodKind
import com.example.nutrivision.ui.components.CalorieRing
import com.example.nutrivision.ui.components.EmptyState
import com.example.nutrivision.ui.components.FoodIdAvatar
import com.example.nutrivision.ui.components.GlassCard
import com.example.nutrivision.ui.components.MacroProgress
import com.example.nutrivision.ui.components.MealTypeAvatar
import com.example.nutrivision.ui.components.MonthCalendarDialog
import com.example.nutrivision.ui.components.mealTypeColor
import com.example.nutrivision.ui.theme.BorderLight
import com.example.nutrivision.ui.theme.ForestDeep
import com.example.nutrivision.ui.theme.ForestInk
import com.example.nutrivision.ui.theme.HeroGradientEnd
import com.example.nutrivision.ui.theme.HeroGradientStart
import com.example.nutrivision.ui.theme.MintMid
import com.example.nutrivision.ui.theme.PrimaryGreen
import com.example.nutrivision.ui.theme.SurfaceLight
import com.example.nutrivision.ui.theme.TextMuted
import com.example.nutrivision.ui.theme.TextPrimary
import com.example.nutrivision.ui.theme.TextSecondary
import com.example.nutrivision.ui.util.grouped
import com.example.nutrivision.ui.util.multiplierText
import com.example.nutrivision.ui.viewmodel.DiaryUiState
import com.example.nutrivision.ui.viewmodel.DiaryViewModel
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun DiaryScreen(
    viewModel: DiaryViewModel,
    uiState: DiaryUiState,
    onAddMeal: (MealType) -> Unit,
    onEditItem: (MealItem, MealType) -> Unit,
) {
    var pendingDelete by remember { mutableStateOf<MealItem?>(null) }
    var showCalendar by remember { mutableStateOf(false) }
    val week = remember(uiState.today, uiState.date) {
        val recent = DateUtils.lastNDays(7, uiState.today)
        if (uiState.date in recent || uiState.date > uiState.today) {
            recent
        } else {
            // Đang xem một ngày xa hơn 7 ngày: dải tuần trượt theo ngày đó.
            DateUtils.lastNDays(7, uiState.date)
        }
    }

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
                        tr("Nhật ký", "Diary"),
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                    )
                    Text(
                        "${DateUtils.weekdayLong(uiState.date)}, ${DateUtils.display(uiState.date)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
                if (uiState.date != uiState.today) {
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        onClick = { viewModel.selectDate(uiState.today) },
                        shape = RoundedCornerShape(50),
                        color = MintMid.copy(alpha = 0.7f),
                    ) {
                        Text(
                            tr("Hôm nay", "Today"),
                            style = MaterialTheme.typography.labelLarge,
                            color = ForestInk,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        )
                    }
                }
                IconButton(onClick = { showCalendar = true }) {
                    Icon(
                        Icons.Rounded.CalendarMonth,
                        contentDescription = tr("Mở lịch tháng", "Open month calendar"),
                        tint = PrimaryGreen,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                week.forEach { date ->
                    val selected = date == uiState.date
                    val isToday = date == uiState.today
                    val bg by animateColorAsState(
                        if (selected) PrimaryGreen else SurfaceLight,
                        label = "dayBg",
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(bg)
                            .border(
                                if (selected) 0.dp else 1.dp,
                                if (selected) Color.Transparent else BorderLight,
                                RoundedCornerShape(16.dp),
                            )
                            .clickable { viewModel.selectDate(date) }
                            .padding(vertical = 9.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            DateUtils.weekdayShort(date),
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                selected -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                                isToday -> PrimaryGreen
                                else -> TextMuted
                            },
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            DateUtils.dayNumber(date),
                            style = MaterialTheme.typography.titleSmall,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else TextPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(3.dp))
                        Box(
                            Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isToday) {
                                        if (selected) MaterialTheme.colorScheme.onPrimary else PrimaryGreen
                                    } else Color.Transparent
                                )
                        )
                    }
                }
            }
        }

        item {
            Surface(
                onClick = { showCalendar = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = SurfaceLight,
                border = BorderStroke(1.dp, BorderLight),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MintMid.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.CalendarMonth,
                            contentDescription = null,
                            tint = ForestInk,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            tr("Xem lịch tháng", "View month calendar"),
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            tr("Chọn bất kỳ ngày nào trong tháng hoặc năm", "Pick any day in the month or year"),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
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
        }

        item {
            DiarySummaryCard(uiState = uiState)
        }

        items(count = MealType.entries.size) { index ->
            val type = MealType.entries[index]
            MealSectionCard(
                type = type,
                meal = uiState.mealOf(type),
                onAdd = { onAddMeal(type) },
                onEdit = { item -> onEditItem(item, type) },
                onDelete = { pendingDelete = it },
            )
        }

        if (!uiState.hasData) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    EmptyState(
                        icon = Icons.Rounded.RestaurantMenu,
                        title = tr("Ngày này chưa có món nào", "No meals on this day"),
                        description = tr("Chọn một bữa bên trên để thêm món. Tổng calo và dinh dưỡng sẽ được cập nhật ngay.", "Pick a meal above to add food. Calories and nutrition update instantly."),
                    )
                }
            }
        }
    }

    if (showCalendar) {
        MonthCalendarDialog(
            selectedDate = uiState.date,
            today = uiState.today,
            onDismiss = { showCalendar = false },
            onSelect = { date ->
                viewModel.selectDate(date)
                showCalendar = false
            },
        )
    }

    pendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(tr("Xoá món khỏi bữa?", "Remove food from meal?"), style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    tr("“${item.foodName}” (${item.portionGrams.roundToInt()} g) sẽ bị xoá khỏi nhật ký và tổng calo được tính lại.", "“${item.foodName}” (${item.portionGrams.roundToInt()} g) will be removed from the diary and totals recalculated."),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteItem(item.id)
                    pendingDelete = null
                }) {
                    Text(tr("Xoá", "Remove"), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(tr("Giữ lại", "Keep"), color = TextSecondary)
                }
            },
            containerColor = SurfaceLight,
            shape = RoundedCornerShape(24.dp),
        )
    }
}

@Composable
private fun DiarySummaryCard(uiState: DiaryUiState) {
    val profile = uiState.profile
    val target = profile?.targetCalories
    val consumed = uiState.nutrition.calories
    val remaining = target?.let { it - consumed }
    val over = remaining != null && remaining < 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.linearGradient(listOf(ForestDeep, HeroGradientStart, HeroGradientEnd)))
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CalorieRing(
            progress = if (target != null && target > 0) consumed.toFloat() / target else 0f,
            modifier = Modifier.size(92.dp),
            strokeWidth = 10.dp,
            gradientColors = if (over) listOf(Color(0xFFFFE08A), Color(0xFFF59E0B)) else listOf(Color.White, Color(0xFFD9F99D)),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    consumed.roundToInt().grouped(),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                )
                Text("kcal", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                if (target == null) tr("Chưa có mục tiêu", "No goal yet") else if (over) tr("Vượt ${abs(remaining!!).roundToInt().grouped()} kcal", "${abs(remaining!!).roundToInt().grouped()} kcal over") else tr("Còn lại ${remaining!!.roundToInt().grouped()} kcal", "${remaining!!.roundToInt().grouped()} kcal left"),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
            Text(
                if (target == null) tr("Thiết lập hồ sơ để có mục tiêu", "Set up your profile for a goal") else tr("Mục tiêu ${target.grouped()} kcal/ngày", "Goal ${target.grouped()} kcal/day"),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.68f),
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniMacro(tr("Đạm", "Protein"), uiState.nutrition.protein, Color(0xFF93C5FD))
                MiniMacro("Carb", uiState.nutrition.carbs, Color(0xFFFCD34D))
                MiniMacro(tr("Béo", "Fat"), uiState.nutrition.fat, Color(0xFFFCA5A5))
            }
        }
    }
}

@Composable
private fun MiniMacro(label: String, grams: Double, color: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(5.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.65f))
        }
        Text(
            "${grams.roundToInt()} g",
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
        )
    }
}

@Composable
private fun MealSectionCard(
    type: MealType,
    meal: MealWithItems?,
    onAdd: () -> Unit,
    onEdit: (MealItem) -> Unit,
    onDelete: (MealItem) -> Unit,
) {
    val color = mealTypeColor(type)
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp),
        borderColor = color.copy(alpha = 0.18f),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MealTypeAvatar(type = type, size = 40.dp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(type.label, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(
                    if (meal == null) tr("Chưa có món", "No items") else tr("${meal.items.size} món", "${meal.items.size} items"),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
            if (meal != null) {
                Text(
                    "${meal.nutrition.calories.roundToInt().grouped()} kcal",
                    style = MaterialTheme.typography.labelLarge,
                    color = color,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.width(6.dp))
            Surface(
                onClick = onAdd,
                shape = CircleShape,
                color = color.copy(alpha = 0.13f),
                modifier = Modifier.size(32.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Add, contentDescription = tr("Thêm món", "Add food"), tint = color, modifier = Modifier.size(18.dp))
                }
            }
        }

        if (meal == null || meal.items.isEmpty()) {
            Text(
                tr("Thêm món cho ${type.label.lowercase()} để tính dinh dưỡng.", "Add food for ${type.label.lowercase()} to see nutrition."),
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier.padding(top = 10.dp, start = 4.dp),
            )
        } else {
            Spacer(Modifier.height(8.dp))
            meal.items.forEach { item ->
                DiaryItemRow(
                    item = item,
                    onEdit = { onEdit(item) },
                    onDelete = { onDelete(item) },
                )
            }
        }
    }
}

@Composable
private fun DiaryItemRow(
    item: MealItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onEdit)
            .padding(horizontal = 4.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FoodIdAvatar(
            foodId = item.foodId,
            name = item.foodName,
            isDrink = item.foodName.guessFoodKind() == "drink",
            size = 42.dp,
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.foodName,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                tr(
                    "${item.portionGrams.roundToInt()} g · ${(item.portionGrams / item.basePortionG.coerceAtLeast(1)).multiplierText()} khẩu phần",
                    "${item.portionGrams.roundToInt()} g · ${(item.portionGrams / item.basePortionG.coerceAtLeast(1)).multiplierText()} servings",
                ),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "${item.nutrition.calories.roundToInt().grouped()} kcal",
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "Đ ${item.nutrition.protein.roundToInt()} · C ${item.nutrition.carbs.roundToInt()} · B ${item.nutrition.fat.roundToInt()}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
        }
        Spacer(Modifier.width(2.dp))
        Surface(
            onClick = onEdit,
            shape = CircleShape,
            color = Color.Transparent,
            modifier = Modifier.size(30.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Edit, contentDescription = tr("Sửa khẩu phần", "Edit portion"), tint = TextMuted, modifier = Modifier.size(16.dp))
            }
        }
        Surface(
            onClick = onDelete,
            shape = CircleShape,
            color = Color.Transparent,
            modifier = Modifier.size(30.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.DeleteOutline, contentDescription = tr("Xoá món", "Remove food"), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
            }
        }
    }
}
