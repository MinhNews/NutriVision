package com.example.nutrivision.ui.components

import com.example.nutrivision.domain.text.tr
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.nutrivision.domain.calc.DateUtils
import com.example.nutrivision.ui.theme.BorderLight
import com.example.nutrivision.ui.theme.PrimaryGreen
import com.example.nutrivision.ui.theme.SurfaceLight
import com.example.nutrivision.ui.theme.TextMuted
import com.example.nutrivision.ui.theme.TextPrimary
import com.example.nutrivision.ui.theme.TextSecondary

/**
 * Lịch tháng đầy đủ để chọn ngày xem nhật ký. Đi được từng tháng và từng năm,
 * chặn ngày tương lai vì chưa có dữ liệu. Thứ Hai là cột đầu tiên.
 */
@Composable
fun MonthCalendarDialog(
    selectedDate: String,
    today: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    var month by remember { mutableStateOf(DateUtils.firstOfMonth(selectedDate)) }
    val thisMonth = DateUtils.firstOfMonth(today)
    val canGoNextMonth = month < thisMonth
    val canGoNextYear = DateUtils.addYears(month, 1) <= thisMonth
    val days = DateUtils.daysOfMonth(month)
    val leading = DateUtils.dayOfWeekIndex(days.first())
    val cellCount = leading + days.size
    val rows = (cellCount + 6) / 7

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = SurfaceLight,
            border = BorderStroke(1.dp, BorderLight),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { month = DateUtils.addMonths(month, -1) }) {
                        Icon(
                            Icons.Rounded.ChevronLeft,
                            contentDescription = tr("Tháng trước", "Previous month"),
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            DateUtils.monthYearLabel(month),
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            DateUtils.monthSpanLabel(month),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                        )
                    }
                    IconButton(onClick = { month = DateUtils.addMonths(month, 1) }, enabled = canGoNextMonth) {
                        Icon(
                            Icons.Rounded.ChevronRight,
                            contentDescription = tr("Tháng sau", "Next month"),
                            tint = if (canGoNextMonth) TextSecondary else TextMuted.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = { month = DateUtils.addYears(month, -1) },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    ) {
                        Text(
                            "‹ ${DateUtils.year(DateUtils.addYears(month, -1))}",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    TextButton(
                        onClick = { month = DateUtils.addYears(month, 1) },
                        enabled = canGoNextYear,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    ) {
                        Text(
                            "${DateUtils.year(DateUtils.addYears(month, 1))} ›",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (canGoNextYear) TextSecondary else TextMuted.copy(alpha = 0.4f),
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    tr("T2 T3 T4 T5 T6 T7 CN", "Mon Tue Wed Thu Fri Sat Sun")
                        .split(" ")
                        .forEachIndexed { index, label ->
                            Text(
                                text = label,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (index == 6) TextMuted else TextSecondary,
                            )
                        }
                }
                Spacer(Modifier.height(4.dp))

                Column {
                    repeat(rows) { row ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            repeat(7) { column ->
                                val index = row * 7 + column - leading
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (index in days.indices) {
                                        val date = days[index]
                                        DayCell(
                                            date = date,
                                            selected = date == selectedDate,
                                            isToday = date == today,
                                            isFuture = date > today,
                                            onClick = { onSelect(date) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = { onSelect(today) }) {
                        Text(tr("Hôm nay", "Today"), color = PrimaryGreen, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onDismiss) {
                        Text(tr("Đóng", "Close"), color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: String,
    selected: Boolean,
    isToday: Boolean,
    isFuture: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) PrimaryGreen else Color.Transparent
    Surface(
        onClick = onClick,
        enabled = !isFuture,
        modifier = Modifier.fillMaxSize(),
        shape = CircleShape,
        color = background,
        border = if (!selected && isToday) BorderStroke(1.5.dp, PrimaryGreen) else null,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = DateUtils.dayOfMonth(date).toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    selected -> MaterialTheme.colorScheme.onPrimary
                    isFuture -> TextMuted.copy(alpha = 0.35f)
                    isToday -> PrimaryGreen
                    else -> TextPrimary
                },
                fontWeight = if (selected || isToday) FontWeight.Bold else FontWeight.Normal,
            )
        }
    }
}
