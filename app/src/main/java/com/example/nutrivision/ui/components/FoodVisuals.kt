package com.example.nutrivision.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BreakfastDining
import androidx.compose.material.icons.rounded.DinnerDining
import androidx.compose.material.icons.rounded.FreeBreakfast
import androidx.compose.material.icons.rounded.Icecream
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LunchDining
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.nutrivision.domain.model.Food
import com.example.nutrivision.domain.model.MealType
import com.example.nutrivision.ui.theme.MealBreakfast
import com.example.nutrivision.ui.theme.MealDinner
import com.example.nutrivision.ui.theme.MealLunch
import com.example.nutrivision.ui.theme.MealSnack

private val foodGradients = listOf(
    listOf(Color(0xFF34D399), Color(0xFF059669)),
    listOf(Color(0xFFFBBF24), Color(0xFFF59E0B)),
    listOf(Color(0xFF818CF8), Color(0xFF6366F1)),
    listOf(Color(0xFFF472B6), Color(0xFFEC4899)),
    listOf(Color(0xFF60A5FA), Color(0xFF3B82F6)),
    listOf(Color(0xFFA3E635), Color(0xFF65A30D)),
)

fun mealTypeColor(type: MealType): Color = when (type) {
    MealType.BREAKFAST -> MealBreakfast
    MealType.LUNCH -> MealLunch
    MealType.DINNER -> MealDinner
    MealType.SNACK -> MealSnack
}

fun mealTypeIcon(type: MealType): ImageVector = when (type) {
    MealType.BREAKFAST -> Icons.Rounded.FreeBreakfast
    MealType.LUNCH -> Icons.Rounded.LunchDining
    MealType.DINNER -> Icons.Rounded.DinnerDining
    MealType.SNACK -> Icons.Rounded.Icecream
}

fun foodIcon(food: Food?): ImageVector = when {
    food == null -> Icons.Rounded.Restaurant
    food.kind == Food.KIND_DRINK -> Icons.Rounded.LocalCafe
    food.kind == Food.KIND_TOPPING -> Icons.Rounded.BreakfastDining
    else -> Icons.Rounded.Restaurant
}

/** Ảnh món đóng gói trong assets/food_images/<id>.webp, không cần mạng. */
private fun Context.hasFoodImage(foodId: String): Boolean =
    runCatching { assets.open("food_images/$foodId.webp").close() }.isSuccess

@Composable
fun FoodAvatar(
    food: Food?,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    icon: ImageVector? = null,
) {
    FoodIdAvatar(
        foodId = food?.id,
        name = food?.name.orEmpty(),
        icon = icon ?: foodIcon(food),
        modifier = modifier,
        size = size,
    )
}

/** Dùng cho meal_items: có foodId nên vẫn hiện được ảnh thật, thiếu ảnh thì fallback. */
@Composable
fun FoodIdAvatar(
    foodId: String?,
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    isDrink: Boolean = false,
    icon: ImageVector? = null,
) {
    val fallbackIcon = icon ?: if (isDrink) Icons.Rounded.LocalCafe else Icons.Rounded.Restaurant
    val context = LocalContext.current
    val hasImage = remember(foodId) {
        foodId != null && context.hasFoodImage(foodId)
    }
    val shape = RoundedCornerShape(size * 0.32f)

    if (foodId != null && hasImage) {
        SubcomposeAsyncImage(
            model = "file:///android_asset/food_images/$foodId.webp",
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(shape),
            loading = {
                AvatarFallback(
                    seed = foodId,
                    icon = fallbackIcon,
                    shape = shape,
                    iconSize = size * 0.46f,
                )
            },
            error = {
                AvatarFallback(
                    seed = foodId,
                    icon = fallbackIcon,
                    shape = shape,
                    iconSize = size * 0.46f,
                )
            },
        )
    } else {
        AngleAvatar(
            seed = foodId ?: name,
            icon = fallbackIcon,
            modifier = modifier,
            size = size,
        )
    }
}

@Composable
private fun AngleAvatar(
    seed: String,
    icon: ImageVector,
    modifier: Modifier,
    size: Dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.32f))
            .background(Brush.linearGradient(foodGradients[seed.hashCode().mod(foodGradients.size)])),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(size * 0.46f),
        )
    }
}

@Composable
private fun AvatarFallback(
    seed: String,
    icon: ImageVector,
    shape: RoundedCornerShape,
    iconSize: Dp,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .background(Brush.linearGradient(foodGradients[seed.hashCode().mod(foodGradients.size)])),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
fun MealTypeAvatar(
    type: MealType,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
) {
    val color = mealTypeColor(type)
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.32f))
            .background(color.copy(alpha = 0.13f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = mealTypeIcon(type),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(size * 0.46f),
        )
    }
}
