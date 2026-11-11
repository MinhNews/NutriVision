package com.example.nutrivision.ui.components

import com.example.nutrivision.domain.text.tr
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.nutrivision.ui.theme.BackgroundLight
import com.example.nutrivision.ui.theme.BorderLight
import com.example.nutrivision.ui.theme.ForestInk
import com.example.nutrivision.ui.theme.HeroGradientEnd
import com.example.nutrivision.ui.theme.HeroGradientStart
import com.example.nutrivision.ui.theme.MintGradientTop
import com.example.nutrivision.ui.theme.MintMid
import com.example.nutrivision.ui.theme.PrimaryGreen
import com.example.nutrivision.ui.theme.ShadowGreen
import com.example.nutrivision.ui.theme.SurfaceLight
import com.example.nutrivision.ui.theme.TextMuted
import com.example.nutrivision.ui.theme.TextPrimary
import com.example.nutrivision.ui.theme.TextSecondary
import com.example.nutrivision.ui.util.grouped
import kotlin.math.roundToInt

/**
 * Nền chung: gradient mint rất nhạt + 2 quầng sáng mờ tạo chiều sâu.
 * Không dùng ảnh nền để APK nhẹ và không phụ thuộc mạng.
 */
@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(MintGradientTop, BackgroundLight, BackgroundLight)
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(PrimaryGreen.copy(alpha = 0.13f), Color.Transparent),
                    center = Offset(size.width * 0.95f, size.height * 0.02f),
                    radius = size.minDimension * 0.55f,
                ),
                radius = size.minDimension * 0.55f,
                center = Offset(size.width * 0.95f, size.height * 0.02f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF6366F1).copy(alpha = 0.07f), Color.Transparent),
                    center = Offset(size.width * 0.05f, size.height * 0.65f),
                    radius = size.minDimension * 0.45f,
                ),
                radius = size.minDimension * 0.45f,
                center = Offset(size.width * 0.05f, size.height * 0.65f),
            )
        }
        content()
    }
}

@Composable
fun Modifier.pressScale(interactionSource: MutableInteractionSource): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "pressScale",
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    color: Color = SurfaceLight,
    borderColor: Color = BorderLight,
    elevation: Dp = 1.dp,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier
            .shadow(elevation, shape, clip = false)
            .clip(shape)
            .border(1.dp, borderColor, shape),
        color = color,
        shape = shape,
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    height: Dp = 56.dp,
    contentColor: Color = Color.White,
    gradient: List<Color> = listOf(HeroGradientStart, HeroGradientEnd),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val alpha by animateFloatAsState(if (enabled) 1f else 0.45f, label = "btnAlpha")
    Box(
        modifier = modifier
            .height(height)
            .pressScale(interactionSource)
            .shadow(if (enabled) 14.dp else 0.dp, RoundedCornerShape(20.dp), ambientColor = ShadowGreen, spotColor = ShadowGreen)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(gradient))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !loading,
                onClick = onClick,
            )
            .graphicsLayer { this.alpha = alpha },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            if (loading) {
                DotsLoading(color = contentColor, dotSize = 7.dp)
            } else {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor,
                )
            }
        }
    }
}

@Composable
fun PillChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    selectedColor: Color = PrimaryGreen,
) {
    val bg by animateColorAsState(if (selected) selectedColor else SurfaceLight, label = "chipBg")
    val fg by animateColorAsState(
        if (selected) {
            if (selectedColor.luminance() > 0.4f) Color(0xFF06281C) else Color.White
        } else {
            TextSecondary
        },
        label = "chipFg",
    )
    val border by animateColorAsState(if (selected) selectedColor else BorderLight, label = "chipBorder")
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, tint = fg, modifier = Modifier.size(15.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, color = fg, maxLines = 1)
    }
}

/**
 * Vòng calo: track mờ + cung tiến trình có animation, gradient xanh → trắng.
 * Nội dung ở giữa do nơi gọi quyết định (số calo, nhãn...).
 */
@Composable
fun CalorieRing(
    progress: Float,
    modifier: Modifier = Modifier,
    trackColor: Color = Color.White.copy(alpha = 0.18f),
    gradientColors: List<Color> = listOf(Color.White, Color(0xFFD9F99D)),
    strokeWidth: Dp = 13.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "ringProgress",
    )
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2
            val arcSize = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            if (animated > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(gradientColors),
                    startAngle = -90f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
        }
        content()
    }
}

@Composable
fun MacroProgress(
    label: String,
    consumed: Double,
    target: Double,
    color: Color,
    modifier: Modifier = Modifier,
    onDark: Boolean = false,
) {
    val fraction = if (target > 0) (consumed / target).toFloat().coerceIn(0f, 1f) else 0f
    val animated by animateFloatAsState(fraction, tween(800, easing = FastOutSlowInEasing), label = "macro")
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = if (onDark) Color.White.copy(alpha = 0.82f) else TextSecondary,
            )
            Text(
                if (target > 0) "${consumed.roundToInt().grouped()} / ${target.roundToInt().grouped()} g"
                else "${consumed.roundToInt().grouped()} g",
                style = MaterialTheme.typography.labelMedium,
                color = if (onDark) Color.White else TextPrimary,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(50))
                .background(if (onDark) Color.White.copy(alpha = 0.16f) else color.copy(alpha = 0.14f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animated)
                    .height(7.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (onDark) Color.White else color)
            )
        }
    }
}

@Composable
fun AnimatedInt(
    value: Int,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.displayMedium,
    color: Color = TextPrimary,
) {
    val animated by animateIntAsState(value, tween(650, easing = FastOutSlowInEasing), label = "intValue")
    Text(text = animated.grouped(), modifier = modifier, style = style, color = color)
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction, contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text(actionLabel, style = MaterialTheme.typography.labelLarge, color = PrimaryGreen)
            }
        }
    }
}

@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            Surface(
                onClick = onBack,
                shape = CircleShape,
                color = SurfaceLight,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                modifier = Modifier.size(42.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.ArrowBackIosNew,
                        contentDescription = tr("Quay lại", "Back"),
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineMedium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
        trailing?.invoke()
    }
}

@Composable
fun IconBadge(
    icon: ImageVector,
    tint: Color,
    background: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 21.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.34f))
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(iconSize))
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    tint: Color = PrimaryGreen,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 26.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconBadge(
            icon = icon,
            tint = tint,
            background = tint.copy(alpha = 0.10f),
            size = 62.dp,
            iconSize = 28.dp,
        )
        Spacer(Modifier.height(14.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(
            description,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(14.dp))
            TextButton(onClick = onAction) {
                Text(actionLabel, color = PrimaryGreen, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

/** Ba chấm nhấp nháy cho trạng thái đang lưu. */
@Composable
fun DotsLoading(
    color: Color = Color.White,
    dotSize: Dp = 8.dp,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "dots")
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        repeat(3) { index ->
            val alpha by transition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(420, delayMillis = index * 130),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "dot$index",
            )
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(color.copy(alpha = alpha))
            )
        }
    }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailing: (@Composable () -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
    enabled: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        placeholder = placeholder?.let { { Text(it, color = TextMuted, style = MaterialTheme.typography.bodyMedium) } },
        leadingIcon = leadingIcon?.let {
            { Icon(it, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(20.dp)) }
        },
        trailingIcon = trailing,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        singleLine = singleLine,
        enabled = enabled,
        isError = isError,
        supportingText = supportingText?.let { { Text(it, style = MaterialTheme.typography.labelSmall) } },
        shape = RoundedCornerShape(18.dp),
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryGreen,
            unfocusedBorderColor = BorderLight,
            focusedContainerColor = SurfaceLight,
            unfocusedContainerColor = SurfaceLight,
            errorBorderColor = MaterialTheme.colorScheme.error,
            cursorColor = PrimaryGreen,
            focusedLabelColor = PrimaryGreen,
            unfocusedLabelColor = TextSecondary,
        ),
    )
}

/** Thẻ số liệu nhỏ dùng ở Dashboard / Hồ sơ / Thống kê. */
@Composable
fun StatTile(
    label: String,
    value: String,
    unit: String? = null,
    tint: Color = PrimaryGreen,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(tint.copy(alpha = 0.08f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Spacer(Modifier.height(3.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, style = MaterialTheme.typography.titleLarge, color = ForestInk)
            if (unit != null) {
                Spacer(Modifier.width(3.dp))
                Text(unit, style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.padding(bottom = 3.dp))
            }
        }
    }
}

@Composable
fun CheckRow(text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(MintMid),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Check, contentDescription = null, tint = ForestInk, modifier = Modifier.size(13.dp))
        }
        Spacer(Modifier.width(9.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}
