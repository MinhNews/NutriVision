package com.example.nutrivision.ui.components

import com.example.nutrivision.domain.text.tr
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrivision.domain.calc.DateUtils
import com.example.nutrivision.domain.model.DayTotal
import com.example.nutrivision.domain.model.Nutrition
import com.example.nutrivision.ui.theme.BorderLight
import com.example.nutrivision.ui.theme.HeroGradientEnd
import com.example.nutrivision.ui.theme.HeroGradientStart
import com.example.nutrivision.ui.theme.MacroCarb
import com.example.nutrivision.ui.theme.MacroFat
import com.example.nutrivision.ui.theme.MacroProtein
import com.example.nutrivision.ui.theme.PrimaryGreen
import com.example.nutrivision.ui.theme.TextMuted
import com.example.nutrivision.ui.theme.TextPrimary
import com.example.nutrivision.ui.theme.TextSecondary
import com.example.nutrivision.ui.util.grouped
import kotlin.math.roundToInt

/**
 * Biểu đồ cột 7 ngày. Chỉ vẽ số liệu thật đọc từ DB;
 * ngày không ghi bữa nào = 0 và hiển thị "–", không bịa số.
 */
@Composable
fun WeeklyBarChart(
    days: List<DayTotal>,
    targetCalories: Int?,
    modifier: Modifier = Modifier,
) {
    val maxValue = maxOf(
        days.maxOfOrNull { it.calories } ?: 0.0,
        (targetCalories ?: 0).toDouble(),
        1.0,
    )
    val today = DateUtils.todayKey()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(196.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        days.forEachIndexed { index, day ->
            val isToday = day.date == today
            val fraction = (day.calories / maxValue).toFloat().coerceIn(0f, 1f)
            val animated by animateFloatAsState(
                targetValue = fraction,
                animationSpec = tween(720, delayMillis = index * 55, easing = FastOutSlowInEasing),
                label = "bar$index",
            )
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Text(
                    text = if (day.calories > 0) day.calories.roundToInt().grouped() else "–",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isToday) PrimaryGreen else TextSecondary,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(132.dp),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.62f)
                            .fillMaxHeight(if (day.calories > 0) animated.coerceAtLeast(0.04f) else 0.025f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (day.calories > 0) {
                                    if (isToday) {
                                        Brush.verticalGradient(listOf(HeroGradientStart, HeroGradientEnd))
                                    } else {
                                        Brush.verticalGradient(
                                            listOf(HexMint, PrimaryGreen.copy(alpha = 0.55f))
                                        )
                                    }
                                } else {
                                    Brush.verticalGradient(listOf(BorderLight, BorderLight))
                                }
                            ),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = DateUtils.weekdayShort(day.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isToday) PrimaryGreen else TextMuted,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

/**
 * Biểu đồ cột cho cả tháng (28–31 ngày). Mỗi ngày một cột mảnh nên không ghi
 * số trên đầu cột; chỉ ghi nhãn ngày thưa (1, 5, 10, …) và đánh dấu hôm nay.
 * Ngày chưa tới để mờ, ngày không ghi bữa chỉ có vạch đáy — không bịa dữ liệu.
 */
@Composable
fun MonthlyBarChart(
    days: List<DayTotal>,
    targetCalories: Int?,
    today: String,
    modifier: Modifier = Modifier,
) {
    if (days.isEmpty()) return
    val maxValue = maxOf(
        days.maxOfOrNull { it.calories } ?: 0.0,
        (targetCalories ?: 0).toDouble(),
        1.0,
    )
    val targetFraction = targetCalories?.let { (it / maxValue).toFloat().coerceIn(0f, 1f) }

    var started by remember(days.first().date) { mutableStateOf(false) }
    LaunchedEffect(days.first().date) { started = true }
    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(620),
        label = "monthBars",
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(176.dp),
    ) {
        val labelArea = 20.dp.toPx()
        val chartHeight = size.height - labelArea
        val slot = size.width / days.size
        val barWidth = (slot * 0.58f).coerceAtLeast(1.5.dp.toPx())
        val radius = CornerRadius(barWidth / 2f, barWidth / 2f)
        val emptyHeight = 2.dp.toPx()

        targetFraction?.let { fraction ->
            if (fraction > 0.02f && fraction < 0.99f) {
                val y = chartHeight * (1f - fraction)
                drawLine(
                    color = PrimaryGreen.copy(alpha = 0.35f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
                )
            }
        }

        days.forEachIndexed { index, day ->
            val isFuture = day.date > today
            val isToday = day.date == today
            val hasData = day.calories > 0.0
            val barHeight = if (hasData) {
                (chartHeight * (day.calories / maxValue) * progress)
                    .toFloat()
                    .coerceAtLeast(barWidth)
            } else {
                emptyHeight
            }
            val left = index * slot + (slot - barWidth) / 2f
            val color = when {
                isFuture -> BorderLight.copy(alpha = 0.45f)
                !hasData -> BorderLight
                isToday -> HeroGradientStart
                else -> PrimaryGreen.copy(alpha = 0.78f)
            }
            drawRoundRect(
                color = color,
                topLeft = Offset(left, chartHeight - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = radius,
            )
        }

        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = 9.sp.toPx()
        }
        val baseline = size.height - 4.dp.toPx()
        days.forEachIndexed { index, day ->
            val dm = DateUtils.dayOfMonth(day.date)
            val showLabel = index == 0 || index == days.lastIndex || dm % 5 == 0
            if (showLabel) {
                paint.color = if (day.date == today) {
                    PrimaryGreen.toArgb()
                } else {
                    TextMuted.toArgb()
                }
                drawContext.canvas.nativeCanvas.drawText(
                    dm.toString(),
                    index * slot + slot / 2f,
                    baseline,
                    paint,
                )
            }
        }
    }
}

private val HexMint = Color(0xFF6EE7B7)

/** Thanh tỷ lệ đạm / carb / béo của cả tuần, tính từ tổng thật. */
@Composable
fun MacroSplitBar(
    total: Nutrition,
    modifier: Modifier = Modifier,
) {
    val proteinKcal = total.protein * 4
    val carbKcal = total.carbs * 4
    val fatKcal = total.fat * 9
    val sum = proteinKcal + carbKcal + fatKcal

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(50)),
        ) {
            if (sum <= 0.0) {
                Box(modifier = Modifier.fillMaxWidth().fillMaxHeight().background(BorderLight))
            } else {
                Box(modifier = Modifier.weight((proteinKcal / sum).toFloat().coerceAtLeast(0.02f)).fillMaxHeight().background(MacroProtein))
                Box(modifier = Modifier.weight((carbKcal / sum).toFloat().coerceAtLeast(0.02f)).fillMaxHeight().background(MacroCarb))
                Box(modifier = Modifier.weight((fatKcal / sum).toFloat().coerceAtLeast(0.02f)).fillMaxHeight().background(MacroFat))
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            MacroLegend(tr("Đạm", "Protein"), proteinKcal, sum, MacroProtein)
            MacroLegend("Carb", carbKcal, sum, MacroCarb)
            MacroLegend(tr("Béo", "Fat"), fatKcal, sum, MacroFat)
        }
    }
}

@Composable
private fun MacroLegend(label: String, value: Double, sum: Double, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(5.dp))
        Text(
            text = if (sum > 0) "$label ${(value / sum * 100).roundToInt()}%" else "$label –",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
        )
    }
}
