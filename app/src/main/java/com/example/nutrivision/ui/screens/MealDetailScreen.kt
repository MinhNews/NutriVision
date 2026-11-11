package com.example.nutrivision.ui.screens

import com.example.nutrivision.domain.text.tr
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.nutrivision.domain.model.Food
import com.example.nutrivision.domain.model.MealType
import com.example.nutrivision.ui.components.AnimatedInt
import com.example.nutrivision.ui.components.AppBackground
import com.example.nutrivision.ui.components.AppTopBar
import com.example.nutrivision.ui.components.DotsLoading
import com.example.nutrivision.ui.components.FoodAvatar
import com.example.nutrivision.ui.components.GlassCard
import com.example.nutrivision.ui.components.GradientButton
import com.example.nutrivision.ui.components.MealTypeAvatar
import com.example.nutrivision.ui.components.PillChip
import com.example.nutrivision.ui.components.mealTypeColor
import com.example.nutrivision.ui.theme.BorderLight
import com.example.nutrivision.ui.theme.ForestDeep
import com.example.nutrivision.ui.theme.HeroGradientEnd
import com.example.nutrivision.ui.theme.HeroGradientStart
import com.example.nutrivision.ui.theme.MacroCarb
import com.example.nutrivision.ui.theme.MacroFat
import com.example.nutrivision.ui.theme.MacroProtein
import com.example.nutrivision.ui.theme.MintMid
import com.example.nutrivision.ui.theme.PrimaryGreen
import com.example.nutrivision.ui.theme.SurfaceLight
import com.example.nutrivision.ui.theme.TextMuted
import com.example.nutrivision.ui.theme.TextPrimary
import com.example.nutrivision.ui.theme.TextSecondary
import com.example.nutrivision.ui.util.grouped
import com.example.nutrivision.ui.util.multiplierText
import com.example.nutrivision.ui.viewmodel.MealDetailUiState
import com.example.nutrivision.ui.viewmodel.MealDetailViewModel
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun MealDetailScreen(
    viewModel: MealDetailViewModel,
    uiState: MealDetailUiState,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onDeleted: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    AppBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
            ) {
                Spacer(Modifier.height(10.dp))
                AppTopBar(
                    title = if (uiState.isEditing) tr("Sửa khẩu phần", "Edit portion") else tr("Chi tiết món", "Food details"),
                    onBack = onBack,
                    trailing = if (uiState.isEditing) {
                        {
                            Surface(
                                onClick = { showDeleteDialog = true },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.size(42.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Rounded.DeleteOutline,
                                        contentDescription = tr("Xoá món khỏi bữa", "Remove food from meal"),
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                        }
                    } else null,
                )

                Spacer(Modifier.height(16.dp))

                if (uiState.loading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 80.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        DotsLoading(color = PrimaryGreen, dotSize = 9.dp)
                    }
                } else if (uiState.foodId == null) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            uiState.error ?: tr("Không tìm thấy món", "Food not found"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                } else {
                    FoodHeader(uiState)

                    Spacer(Modifier.height(14.dp))

                    NutritionPanel(uiState)

                    Spacer(Modifier.height(14.dp))

                    PortionPanel(uiState, viewModel)

                    Spacer(Modifier.height(14.dp))

                    MealTypePanel(uiState, viewModel)

                    Spacer(Modifier.height(14.dp))

                    SourceNote(uiState)

                    if (uiState.error != null) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            uiState.error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    Spacer(Modifier.height(120.dp))
                }
            }

            if (!uiState.loading && uiState.foodId != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.White.copy(alpha = 0.92f), Color.White)
                            )
                        )
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                ) {
                    GradientButton(
                        text = if (uiState.isEditing) {
                            tr("Cập nhật · ${uiState.nutrition.calories.roundToInt().grouped()} kcal", "Update · ${uiState.nutrition.calories.roundToInt().grouped()} kcal")
                        } else {
                            tr("Lưu vào ${uiState.mealType.label.lowercase()} · ${uiState.nutrition.calories.roundToInt().grouped()} kcal", "Save to ${uiState.mealType.label.lowercase()} · ${uiState.nutrition.calories.roundToInt().grouped()} kcal")
                        },
                        onClick = { viewModel.save(onSaved) },
                        enabled = uiState.canSave,
                        loading = uiState.saving,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(tr("Xoá món khỏi bữa?", "Remove food from meal?"), style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    tr("Món sẽ bị xoá khỏi nhật ký và tổng calo hôm đó được tính lại. Hành động này không xoá món khỏi danh mục.", "The item will be removed from the diary and that day's totals recalculated. It stays in the food catalogue."),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.delete(onDeleted)
                }) {
                    Text(tr("Xoá", "Remove"), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(tr("Giữ lại", "Keep"), color = TextSecondary)
                }
            },
            containerColor = SurfaceLight,
            shape = RoundedCornerShape(24.dp),
        )
    }
}

@Composable
private fun FoodHeader(uiState: MealDetailUiState) {
    val food = Food(
        id = uiState.foodId ?: "",
        name = uiState.foodName,
        category = uiState.category,
        kind = Food.KIND_DISH,
        modelClass = false,
        isCustom = false,
        portionName = uiState.portionName,
        portionWeightG = uiState.basePortionG,
        calories = uiState.per100g.calories * uiState.basePortionG / 100,
        protein = 0.0,
        carbs = 0.0,
        fat = 0.0,
        fiber = 0.0,
        sodiumMg = 0.0,
        caloriesPer100g = uiState.per100g.calories,
        proteinPer100g = uiState.per100g.protein,
        carbsPer100g = uiState.per100g.carbs,
        fatPer100g = uiState.per100g.fat,
        fiberPer100g = uiState.per100g.fiber,
        sodiumMgPer100g = uiState.per100g.sodiumMg,
        description = uiState.description,
        source = null,
        method = null,
        confidence = null,
    )

    GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FoodAvatar(food = food, size = 76.dp)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    uiState.foodName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(5.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Badge(
                        text = if (uiState.isVietnamese) tr("Món Việt", "Vietnamese dish") else tr("Món quốc tế", "International dish"),
                        color = if (uiState.isVietnamese) PrimaryGreen else com.example.nutrivision.ui.theme.AccentIndigo,
                    )
                    if (!uiState.isEditing) {
                        Badge(text = uiState.portionName, color = TextSecondary, soft = true)
                    }
                }
            }
        }
        if (!uiState.description.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(
                uiState.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun NutritionPanel(uiState: MealDetailUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.linearGradient(listOf(ForestDeep, HeroGradientStart, HeroGradientEnd)))
            .padding(20.dp),
    ) {
        Text(
            tr("Dinh dưỡng cho ${uiState.grams.roundToInt()} g", "Nutrition for ${uiState.grams.roundToInt()} g"),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White.copy(alpha = 0.72f),
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            AnimatedInt(
                value = uiState.nutrition.calories.roundToInt(),
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "kcal",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 7.dp),
            )
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.14f))
                    .padding(horizontal = 11.dp, vertical = 6.dp),
            ) {
                Text(
                    uiState.multiplier.multiplierText(),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NutritionTile(tr("Đạm", "Protein"), uiState.nutrition.protein, Color(0xFF93C5FD), Modifier.weight(1f))
            NutritionTile("Carb", uiState.nutrition.carbs, Color(0xFFFCD34D), Modifier.weight(1f))
            NutritionTile(tr("Béo", "Fat"), uiState.nutrition.fat, Color(0xFFFCA5A5), Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NutritionTile(tr("Xơ", "Fiber"), uiState.nutrition.fiber, Color(0xFF86EFAC), Modifier.weight(1f))
            NutritionTile(tr("Natri", "Sodium"), uiState.nutrition.sodiumMg, Color(0xFFC4B5FD), Modifier.weight(1f), unit = "mg")
        }
    }
}

@Composable
private fun NutritionTile(label: String, value: Double, dotColor: Color, modifier: Modifier = Modifier, unit: String = "g") {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(horizontal = 11.dp, vertical = 9.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(5.dp).clip(CircleShape).background(dotColor))
            Spacer(Modifier.width(5.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.66f))
        }
        Spacer(Modifier.height(2.dp))
        Text(
            if (unit == "mg") "${value.roundToInt()} mg" else "${value.roundToInt()} g",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
        )
    }
}

@Composable
private fun PortionPanel(uiState: MealDetailUiState, viewModel: MealDetailViewModel) {
    val base = uiState.basePortionG.toDouble()
    val small = ((base * 0.7) / 10).roundToInt() * 10.0
    val large = ((base * 1.4) / 10).roundToInt() * 10.0
    val maxGrams = maxOf(base * 3.0, 800.0).toFloat()

    val sliderValue by animateFloatAsState(
        targetValue = uiState.grams.toFloat(),
        label = "portionSlider",
    )

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(tr("Khẩu phần", "Portion"), style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(
                    tr("Kéo để đổi số gram — calo cập nhật ngay", "Drag to change grams — calories update instantly"),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
            Text(
                "${uiState.grams.roundToInt()} g",
                style = MaterialTheme.typography.headlineMedium,
                color = PrimaryGreen,
            )
        }

        Spacer(Modifier.height(10.dp))
        Slider(
            value = sliderValue,
            onValueChange = { viewModel.setGrams(it.toDouble()) },
            valueRange = 10f..maxGrams,
            colors = SliderDefaults.colors(
                thumbColor = PrimaryGreen,
                activeTrackColor = PrimaryGreen,
                inactiveTrackColor = MintMid,
            ),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PortionChip(tr("Nhỏ", "Small"), small, uiState.grams, Modifier.weight(1f)) { viewModel.setGrams(it) }
            PortionChip(tr("Vừa", "Medium"), base, uiState.grams, Modifier.weight(1f)) { viewModel.setGrams(it) }
            PortionChip(tr("Lớn", "Large"), large, uiState.grams, Modifier.weight(1f)) { viewModel.setGrams(it) }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            tr("Khẩu phần gốc: ${uiState.portionName.ifBlank { "${base.roundToInt()} g" }}", "Base portion: ${uiState.portionName.ifBlank { "${base.roundToInt()} g" }}"),
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
        )
    }
}

@Composable
private fun PortionChip(
    label: String,
    grams: Double,
    currentGrams: Double,
    modifier: Modifier = Modifier,
    onClick: (Double) -> Unit,
) {
    val selected = abs(currentGrams - grams) < 1.0
    PillChip(
        text = "$label ${grams.roundToInt()}g",
        selected = selected,
        onClick = { onClick(grams) },
        modifier = modifier,
    )
}

@Composable
private fun MealTypePanel(uiState: MealDetailUiState, viewModel: MealDetailViewModel) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Text(tr("Thêm vào bữa", "Add to meal"), style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Spacer(Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            // Nhãn tiếng Anh dài hơn tiếng Việt nên cho phép cuộn ngang thay vì cắt chữ.
            modifier = Modifier.horizontalScroll(rememberScrollState()),
        ) {
            MealType.entries.forEach { type ->
                PillChip(
                    text = type.label.removePrefix("Bữa "),
                    selected = uiState.mealType == type,
                    onClick = { viewModel.setMealType(type) },
                    selectedColor = mealTypeColor(type),
                )
            }
        }
        if (uiState.isEditing) {
            Spacer(Modifier.height(8.dp))
            Text(
                tr("Đang sửa món trong ngày ${com.example.nutrivision.domain.calc.DateUtils.display(uiState.date)}", "Editing an item on ${com.example.nutrivision.domain.calc.DateUtils.display(uiState.date)}"),
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
        }
    }
}

@Composable
private fun SourceNote(uiState: MealDetailUiState) {
    val methodLabel = when (uiState.method) {
        "direct" -> tr("Nguồn: USDA FoodData Central", "Source: USDA FoodData Central")
        "recipe" -> tr("Nguồn: công thức nguyên liệu", "Source: ingredient recipe")
        "estimate" -> tr("Nguồn: ước tính theo khẩu phần", "Source: portion estimate")
        else -> tr("Nguồn: danh mục dinh dưỡng NutriVision", "Source: NutriVision food catalogue")
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceLight.copy(alpha = 0.7f))
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(Icons.Rounded.Info, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                methodLabel,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold,
            )
            if (!uiState.source.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    uiState.source,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(3.dp))
            Text(
                tr("Số liệu dinh dưỡng mang tính tham khảo.", "Nutrition values are for reference only."),
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
        }
    }
}

@Composable
private fun Badge(text: String, color: Color, soft: Boolean = false) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (soft) BorderLight.copy(alpha = 0.6f) else color.copy(alpha = 0.12f))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(5.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(5.dp))
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = if (soft) TextSecondary else color,
            maxLines = 1,
        )
    }
}
