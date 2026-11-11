package com.example.nutrivision.ui.screens

import com.example.nutrivision.domain.text.tr
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.nutrivision.domain.model.Food
import com.example.nutrivision.domain.model.MealType
import com.example.nutrivision.ui.components.AppBackground
import com.example.nutrivision.ui.components.AppTopBar
import com.example.nutrivision.ui.components.DotsLoading
import com.example.nutrivision.ui.components.EmptyState
import com.example.nutrivision.ui.components.FoodAvatar
import com.example.nutrivision.ui.components.PillChip
import com.example.nutrivision.ui.theme.BorderLight
import com.example.nutrivision.ui.theme.ForestInk
import com.example.nutrivision.ui.theme.MintMid
import com.example.nutrivision.ui.theme.PrimaryGreen
import com.example.nutrivision.ui.theme.SurfaceLight
import com.example.nutrivision.ui.theme.TextMuted
import com.example.nutrivision.ui.theme.TextPrimary
import com.example.nutrivision.ui.theme.TextSecondary
import com.example.nutrivision.ui.components.mealTypeColor
import com.example.nutrivision.ui.util.grouped
import com.example.nutrivision.ui.viewmodel.FoodFilter
import com.example.nutrivision.ui.viewmodel.SearchUiState
import com.example.nutrivision.ui.viewmodel.SearchViewModel
import kotlin.math.roundToInt

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    uiState: SearchUiState,
    mealType: MealType,
    onBack: () -> Unit,
    onPick: (Food) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    // Bind ô nhập vào StateFlow đồng bộ, không chờ truy vấn DB, để con trỏ không nhảy.
    val query by viewModel.query.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(10.dp))
            AppTopBar(
                title = tr("Tìm món", "Find food"),
                subtitle = tr("Thêm vào ${mealType.label.lowercase()}", "Add to ${mealType.label.lowercase()}"),
                onBack = onBack,
            )

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = query,
                onValueChange = viewModel::setQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                placeholder = {
                    Text(
                        tr("Gõ “pho bo”, “bun bo”, “com tam”…", "Try “pho”, “banh mi”, “com tam”…"),
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                leadingIcon = {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = PrimaryGreen)
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setQuery("") }) {
                            Icon(Icons.Rounded.Close, contentDescription = tr("Xoá tìm kiếm", "Clear search"), tint = TextMuted)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = BorderLight,
                    focusedContainerColor = SurfaceLight,
                    unfocusedContainerColor = SurfaceLight,
                    cursorColor = PrimaryGreen,
                ),
            )

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FoodFilter.entries.forEach { filter ->
                    PillChip(
                        text = filter.label,
                        selected = uiState.filter == filter,
                        onClick = { viewModel.setFilter(filter) },
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (query.isBlank()) {
                        tr("${uiState.foods.size} món trong danh mục", "${uiState.foods.size} items in the catalogue")
                    } else {
                        tr("${uiState.foods.size} kết quả cho “$query”", "${uiState.foods.size} results for “$query”")
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    modifier = Modifier.weight(1f),
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(mealTypeColor(mealType).copy(alpha = 0.12f))
                        .padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(mealTypeColor(mealType))
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        mealType.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = mealTypeColor(mealType),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (uiState.loading) {
                Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    DotsLoading(color = PrimaryGreen, dotSize = 8.dp)
                }
            } else if (uiState.foods.isEmpty()) {
                EmptyState(
                    icon = Icons.Rounded.SearchOff,
                    title = tr("Không tìm thấy món phù hợp", "No matching food"),
                    description = tr("Thử tên ngắn hơn, gõ không dấu hoặc bỏ bộ lọc.", "Try a shorter name, no accents, or clear the filter."),
                    tint = PrimaryGreen,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(items = uiState.foods, key = { it.id }) { food ->
                        FoodRow(food = food, onClick = { onPick(food) })
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodRow(food: Food, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceLight.copy(alpha = 0.92f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FoodAvatar(food = food, size = 56.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    food.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (food.modelClass) {
                    Spacer(Modifier.width(6.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(MintMid.copy(alpha = 0.8f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = ForestInk, modifier = Modifier.size(10.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("AI", style = MaterialTheme.typography.labelSmall, color = ForestInk)
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                "${food.portionName} · ${food.calories.roundToInt().grouped()} kcal",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
    }
}
