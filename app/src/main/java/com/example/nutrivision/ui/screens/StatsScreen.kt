package com.example.nutrivision.ui.screens

import com.example.nutrivision.domain.text.tr
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nutrivision.domain.calc.DateUtils
import com.example.nutrivision.ui.components.EmptyState
import com.example.nutrivision.ui.components.GlassCard
import com.example.nutrivision.ui.components.MacroSplitBar
import com.example.nutrivision.ui.components.MonthlyBarChart
import com.example.nutrivision.ui.components.SectionHeader
import com.example.nutrivision.ui.components.StatTile
import com.example.nutrivision.ui.components.WeeklyBarChart
import com.example.nutrivision.ui.theme.AccentIndigo
import com.example.nutrivision.ui.theme.AmberSoft
import com.example.nutrivision.ui.theme.BorderLight
import com.example.nutrivision.ui.theme.MintMid
import com.example.nutrivision.ui.theme.PrimaryGreen
import com.example.nutrivision.ui.theme.SecondaryAmber
import com.example.nutrivision.ui.theme.SurfaceLight
import com.example.nutrivision.ui.theme.SurfaceMuted
import com.example.nutrivision.ui.theme.TextMuted
import com.example.nutrivision.ui.theme.TextPrimary
import com.example.nutrivision.ui.theme.TextSecondary
import com.example.nutrivision.ui.util.grouped
import com.example.nutrivision.ui.viewmodel.StatsRange
import com.example.nutrivision.ui.viewmodel.StatsUiState
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun StatsScreen(
    uiState: StatsUiState,
    onGoToDiary: () -> Unit,
    onSelectRange: (StatsRange) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    val isMonth = uiState.range == StatsRange.MONTH
    val periodDays = uiState.dayCount
    val fromDate = uiState.fromDate
    val toDate = uiState.toDate
    val periodLabel = if (fromDate == null || toDate == null) {
        tr("Chưa có dữ liệu", "No data")
    } else if (isMonth) {
        "${DateUtils.monthYearLabel(fromDate)} · ${DateUtils.display(fromDate)} – ${DateUtils.display(toDate)}"
    } else {
        tr(
            "7 ngày gần nhất · ${DateUtils.display(fromDate)} – ${DateUtils.display(toDate)}",
            "Last 7 days · ${DateUtils.display(fromDate)} – ${DateUtils.display(toDate)}",
        )
    }
    val totalLabel = if (isMonth) tr("Tổng tháng", "Month total") else tr("Tổng 7 ngày", "7-day total")
    val macroSubtitle = if (isMonth) {
        tr("Tỷ lệ calo từ đạm, carb và béo trong tháng đã chọn", "Share of calories from protein, carbs and fat in the selected month")
    } else {
        tr("Tỷ lệ calo từ đạm, carb và béo trong 7 ngày", "Share of calories from protein, carbs and fat over 7 days")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column {
                Text(tr("Thống kê", "Stats"), style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                Text(
                    periodLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }

        item {
            RangeToggle(range = uiState.range, onSelect = onSelectRange)
        }

        if (isMonth) {
            item {
                MonthNavigator(
                    label = DateUtils.monthYearLabel(fromDate ?: DateUtils.todayKey()),
                    span = if (fromDate == null || toDate == null) "" else "${DateUtils.display(fromDate)} – ${DateUtils.display(toDate)}",
                    canGoNext = uiState.canGoNextMonth,
                    onPrevious = onPreviousMonth,
                    onNext = onNextMonth,
                )
            }
        }

        if (!uiState.hasData) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    EmptyState(
                        icon = Icons.Rounded.BarChart,
                        title = if (isMonth) tr("Chưa có dữ liệu tháng này", "No data this month") else tr("Chưa có dữ liệu 7 ngày", "No data in the last 7 days"),
                        description = if (isMonth) {
                            tr("Chọn một ngày trong tháng ở Nhật ký rồi ghi bữa để xem thống kê.", "Pick a day in the Diary and log a meal to see stats.")
                        } else {
                            tr("Ghi một bữa hôm nay để bắt đầu thấy xu hướng của bạn.", "Log a meal today to start seeing your trend.")
                        },
                        actionLabel = tr("Ghi bữa ăn", "Log a meal"),
                        onAction = onGoToDiary,
                    )
                }
            }
        } else {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tr("Calo mỗi ngày", "Daily calories"), style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                            Text(
                                if (uiState.profile != null) {
                                    tr("Mục tiêu ${uiState.profile.targetCalories.grouped()} kcal/ngày", "Goal ${uiState.profile.targetCalories.grouped()} kcal/day")
                                } else {
                                    tr("Chưa thiết lập mục tiêu", "No goal set")
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                            )
                        }
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(MintMid.copy(alpha = 0.5f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                tr("${uiState.loggedDays}/$periodDays ngày", "${uiState.loggedDays}/$periodDays days"),
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryGreen,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    if (isMonth) {
                        MonthlyBarChart(
                            days = uiState.days,
                            targetCalories = uiState.profile?.targetCalories,
                            today = DateUtils.todayKey(),
                        )
                    } else {
                        WeeklyBarChart(
                            days = uiState.days,
                            targetCalories = uiState.profile?.targetCalories,
                        )
                    }
                    if (isMonth) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            tr("Vạch đứt là mục tiêu calo mỗi ngày. Ngày chưa tới để mờ.", "Dashed line is the daily calorie goal. Future days are dimmed."),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                        )
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(
                        label = totalLabel,
                        value = uiState.totalCalories.roundToInt().grouped(),
                        unit = "kcal",
                        tint = PrimaryGreen,
                        modifier = Modifier.weight(1f),
                    )
                    StatTile(
                        label = tr("TB ngày có ghi", "Avg. per logged day"),
                        value = uiState.averagePerLoggedDay?.grouped() ?: "–",
                        unit = if (uiState.averagePerLoggedDay != null) "kcal" else null,
                        tint = AccentIndigo,
                        modifier = Modifier.weight(1f),
                    )
                    StatTile(
                        label = tr("Ngày đã ghi", "Days logged"),
                        value = uiState.loggedDays.toString(),
                        unit = "/$periodDays",
                        tint = SecondaryAmber,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    SectionHeader(
                        title = tr("Cơ cấu dinh dưỡng", "Macro breakdown"),
                        subtitle = macroSubtitle,
                    )
                    Spacer(Modifier.height(14.dp))
                    MacroSplitBar(total = uiState.nutrition)
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            tr("Đạm ${uiState.nutrition.protein.roundToInt()} g", "Protein ${uiState.nutrition.protein.roundToInt()} g"),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                        )
                        Text(
                            "Carb ${uiState.nutrition.carbs.roundToInt()} g",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                        )
                        Text(
                            tr("Béo ${uiState.nutrition.fat.roundToInt()} g", "Fat ${uiState.nutrition.fat.roundToInt()} g"),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                        )
                        Text(
                            tr("Xơ ${uiState.nutrition.fiber.roundToInt()} g", "Fiber ${uiState.nutrition.fiber.roundToInt()} g"),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                        )
                    }
                }
            }

            val profile = uiState.profile
            val average = uiState.averagePerLoggedDay
            if (profile != null && average != null) {
                item {
                    val diff = average - profile.targetCalories
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(AmberSoft.copy(alpha = 0.55f))
                            .padding(16.dp),
                    ) {
                        Text(
                            tr("So với mục tiêu", "Against your goal"),
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            when {
                                diff > 0 -> tr("Trung bình cao hơn ${abs(diff).grouped()} kcal/ngày so với mục tiêu ${profile.targetCalories.grouped()} kcal.", "Daily average is ${abs(diff).grouped()} kcal above your goal of ${profile.targetCalories.grouped()} kcal.")
                                diff < 0 -> tr("Trung bình thấp hơn ${abs(diff).grouped()} kcal/ngày so với mục tiêu ${profile.targetCalories.grouped()} kcal.", "Daily average is ${abs(diff).grouped()} kcal below your goal of ${profile.targetCalories.grouped()} kcal.")
                                else -> tr("Trung bình đúng bằng mục tiêu ${profile.targetCalories.grouped()} kcal/ngày.", "Daily average matches your goal of ${profile.targetCalories.grouped()} kcal.")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            tr("Trung bình tính trên ${uiState.loggedDays} ngày có ghi bữa.", "Average over ${uiState.loggedDays} logged days."),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                        )
                    }
                }
            }
        }

        item {
            Text(
                tr("Số liệu tính từ nhật ký đã lưu trên máy. Ngày không ghi bữa được tính là 0 kcal.", "Figures come from the diary stored on this device. Days without meals count as 0 kcal."),
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
        }
    }
}

@Composable
private fun RangeToggle(
    range: StatsRange,
    onSelect: (StatsRange) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(SurfaceMuted)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        RangeChip(
            label = tr("7 ngày", "7 days"),
            selected = range == StatsRange.WEEK,
            modifier = Modifier.weight(1f),
            onClick = { onSelect(StatsRange.WEEK) },
        )
        RangeChip(
            label = tr("Theo tháng", "By month"),
            selected = range == StatsRange.MONTH,
            modifier = Modifier.weight(1f),
            onClick = { onSelect(StatsRange.MONTH) },
        )
    }
}

@Composable
private fun RangeChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = if (selected) PrimaryGreen else Color.Transparent,
    ) {
        Text(
            text = label,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else TextSecondary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun MonthNavigator(
    label: String,
    span: String,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceLight)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50)),
        ) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.Rounded.ChevronLeft, contentDescription = tr("Tháng trước", "Previous month"), tint = TextSecondary)
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(label, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            if (span.isNotEmpty()) {
                Text(span, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
        }
        IconButton(onClick = onNext, enabled = canGoNext) {
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = tr("Tháng sau", "Next month"),
                tint = if (canGoNext) TextSecondary else TextMuted.copy(alpha = 0.4f),
            )
        }
    }
}
