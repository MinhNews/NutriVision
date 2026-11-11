package com.example.nutrivision.ui.screens

import com.example.nutrivision.domain.text.tr
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrivision.ui.components.GradientButton
import com.example.nutrivision.ui.theme.ForestDeep
import com.example.nutrivision.ui.theme.PrimaryGreen
import com.example.nutrivision.ui.theme.PrimaryGreenDark

private data class OnboardPage(
    val icon: ImageVector,
    val tag: String,
    val title: String,
    val body: String,
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
) {
    var page by rememberSaveable { mutableIntStateOf(0) }

    val pages = remember {
        listOf(
            OnboardPage(
                icon = Icons.Rounded.CameraAlt,
                tag = tr("Nhận diện AI", "AI recognition"),
                title = tr("Nhận diện món ăn\nthông minh", "Smart meal\nrecognition"),
                body = tr("Chụp ảnh món ăn, AI giúp bạn nhận diện món và gợi ý thông tin dinh dưỡng tương ứng.", "Snap your meal and AI identifies the dish with matching nutrition info."),
            ),
            OnboardPage(
                icon = Icons.Rounded.Insights,
                tag = tr("Phân tích dinh dưỡng", "Nutrition insights"),
                title = tr("Hiểu rõ dinh dưỡng\ntrong từng bữa ăn", "Understand nutrition\nin every meal"),
                body = tr("Theo dõi Calories, Protein, Carbs và Fat để hiểu rõ giá trị dinh dưỡng của mỗi bữa ăn.", "Track calories, protein, carbs and fat to see the value of every meal."),
            ),
            OnboardPage(
                icon = Icons.Rounded.TrackChanges,
                tag = tr("Dinh dưỡng cá nhân", "Personal nutrition"),
                title = tr("Kiểm soát cân nặng\nvà thói quen ăn uống", "Manage your weight\nand eating habits"),
                body = tr("Thiết lập mục tiêu cân nặng, theo dõi dinh dưỡng hằng ngày và từng bước xây dựng chế độ ăn phù hợp.", "Set a weight goal, track daily nutrition and build an eating plan that fits you."),
            ),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(ForestDeep, Color(0xFF065F46), PrimaryGreenDark)
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(PrimaryGreen.copy(alpha = 0.55f), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.12f),
                    radius = size.minDimension * 0.6f,
                ),
                radius = size.minDimension * 0.6f,
                center = Offset(size.width * 0.85f, size.height * 0.12f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFF84CC16).copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(size.width * 0.1f, size.height * 0.8f),
                    radius = size.minDimension * 0.5f,
                ),
                radius = size.minDimension * 0.5f,
                center = Offset(size.width * 0.1f, size.height * 0.8f),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(Color.White.copy(alpha = 0.14f))
                        .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Spa, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    "NutriVision",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    tr("Bỏ qua", "Skip"),
                    color = Color.White.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onFinish)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }

            Spacer(Modifier.weight(0.55f))

            AnimatedContent(
                targetState = page,
                transitionSpec = {
                    val direction = if (targetState > initialState) 1 else -1
                    (fadeIn(tween(320)) + slideInHorizontally(tween(380)) { direction * it / 3 }) togetherWith
                        (fadeOut(tween(180)) + slideOutHorizontally(tween(280)) { -direction * it / 3 })
                },
                label = "onboarding",
            ) { index ->
                val item = pages[index]
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PulsingIcon(icon = item.icon)
                    Spacer(Modifier.height(30.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.13f))
                            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(50))
                            .padding(horizontal = 13.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFD9F99D),
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            item.tag,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                        )
                    }
                    Spacer(Modifier.height(18.dp))
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 42.sp,
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = item.body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.78f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 6.dp),
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                pages.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(7.dp)
                            .width(if (index == page) 24.dp else 7.dp)
                            .clip(CircleShape)
                            .background(if (index == page) Color.White else Color.White.copy(alpha = 0.3f)),
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            GradientButton(
                text = if (page == pages.lastIndex) tr("Bắt đầu", "Get started") else tr("Tiếp tục", "Continue"),
                onClick = {
                    if (page == pages.lastIndex) onFinish() else page += 1
                },
                modifier = Modifier.fillMaxWidth(),
                gradient = listOf(Color.White, Color(0xFFE7FFF5)),
                contentColor = ForestDeep,
            )
        }
    }
}

@Composable
private fun PulsingIcon(icon: ImageVector) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val glow by transition.animateFloat(
        initialValue = 0.22f,
        targetValue = 0.42f,
        animationSpec = infiniteRepeatable(tween(1800), repeatMode = RepeatMode.Reverse),
        label = "glow",
    )
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(1800), repeatMode = RepeatMode.Reverse),
        label = "scale",
    )
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(190.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(PrimaryGreen.copy(alpha = glow), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(150.dp)
                .background(
                    Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.08f))
                    ),
                    CircleShape,
                )
                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size((104 * scale).dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFFD9F99D), Color(0xFF6EE7B7)))
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = ForestDeep,
                    modifier = Modifier.size(48.dp),
                )
            }
        }
    }
}
