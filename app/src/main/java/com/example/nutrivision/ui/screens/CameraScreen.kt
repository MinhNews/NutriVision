package com.example.nutrivision.ui.screens

import com.example.nutrivision.domain.text.tr
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nutrivision.ui.components.GradientButton
import com.example.nutrivision.ui.theme.ForestDeep
import com.example.nutrivision.ui.theme.PrimaryGreen

@Composable
fun CameraScreen(
    onSearchClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF031F17), ForestDeep, Color(0xFF043D2C))))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(PrimaryGreen.copy(alpha = 0.35f), Color.Transparent),
                    center = Offset(size.width * 0.8f, size.height * 0.15f),
                    radius = size.minDimension * 0.6f,
                ),
                radius = size.minDimension * 0.6f,
                center = Offset(size.width * 0.8f, size.height * 0.15f),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp),
        ) {
            Spacer(Modifier.height(14.dp))
            Column {
                Text(
                    tr("Nhận diện món ăn", "Meal recognition"),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                )
                Text(
                    tr("Chụp ảnh để nhận diện nhanh", "Take a photo for quick recognition"),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                )
            }

            Spacer(Modifier.height(22.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.86f)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.09f),
                                Color(0xFF10B981).copy(alpha = 0.12f),
                                Color.White.copy(alpha = 0.04f),
                            )
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(32.dp)),
            ) {
                ScannerOverlay()

                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.CameraAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(38.dp),
                        )
                    }
                    Spacer(Modifier.height(18.dp))
                    Text(
                        tr("Tính năng đang được hoàn thiện", "Feature coming soon"),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        tr("Trong lúc chờ, bạn có thể tìm và thêm món thủ công.", "Meanwhile, you can search and add food manually."),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.62f),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(26.dp))
                    .padding(16.dp),
            ) {
                Text(
                    tr("Thêm món thủ công", "Add food manually"),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
                Text(
                    tr("Tìm trong danh mục món ăn sẵn có để ghi bữa ngay hôm nay.", "Search the built-in food catalogue and log a meal today."),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f),
                )
                Spacer(Modifier.height(14.dp))
                GradientButton(
                    text = tr("Tìm món", "Find food"),
                    icon = Icons.Rounded.Search,
                    onClick = onSearchClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ScannerOverlay() {
    val transition = rememberInfiniteTransition(label = "scan")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scanLine",
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val corner = 34.dp.toPx()
        val stroke = 3.dp.toPx()
        val inset = 18.dp.toPx()
        val length = 34.dp.toPx()
        val color = Color(0xFFD9F99D).copy(alpha = 0.9f)

        drawLine(color, Offset(inset, inset + length), Offset(inset, inset), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(inset, inset), Offset(inset + length, inset), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(size.width - inset - length, inset), Offset(size.width - inset, inset), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(size.width - inset, inset), Offset(size.width - inset, inset + length), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(inset, size.height - inset - length), Offset(inset, size.height - inset), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(inset, size.height - inset), Offset(inset + length, size.height - inset), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(size.width - inset - length, size.height - inset), Offset(size.width - inset, size.height - inset), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(size.width - inset, size.height - inset), Offset(size.width - inset, size.height - inset - length), strokeWidth = stroke, cap = StrokeCap.Round)

        val y = inset + (size.height - inset * 2) * progress
        drawLine(
            brush = Brush.horizontalGradient(
                listOf(Color.Transparent, Color(0xFFD9F99D).copy(alpha = 0.85f), Color(0xFFD9F99D).copy(alpha = 0.1f), Color.Transparent)
            ),
            start = Offset(inset + corner * 0.2f, y),
            end = Offset(size.width - inset - corner * 0.2f, y),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawCircle(
            color = Color(0xFFD9F99D).copy(alpha = 0.18f),
            radius = 26.dp.toPx(),
            center = Offset(size.width / 2, y),
        )
    }
}
