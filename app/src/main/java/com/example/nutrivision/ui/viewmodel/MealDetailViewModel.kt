package com.example.nutrivision.ui.viewmodel

import com.example.nutrivision.domain.text.tr
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrivision.data.repository.FoodRepository
import com.example.nutrivision.data.repository.MealRepository
import com.example.nutrivision.data.repository.UserRepository
import com.example.nutrivision.domain.calc.DateUtils
import com.example.nutrivision.domain.calc.NutritionCalculator
import com.example.nutrivision.domain.model.Food
import com.example.nutrivision.domain.model.MealType
import com.example.nutrivision.domain.model.Nutrition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class MealDetailUiState(
    val loading: Boolean = true,
    val isEditing: Boolean = false,
    val foodId: String? = null,
    val foodName: String = "",
    val description: String? = null,
    val category: String = Food.CATEGORY_VIETNAMESE,
    val portionName: String = "",
    val method: String? = null,
    val source: String? = null,
    val basePortionG: Int = 100,
    val per100g: Nutrition = Nutrition.ZERO,
    val grams: Double = 100.0,
    val mealType: MealType = MealType.LUNCH,
    val date: String = DateUtils.todayKey(),
    val saving: Boolean = false,
    val error: String? = null,
) {
    /** Calo/đạm/carb/béo thật theo đúng số gram đang chọn. */
    val nutrition: Nutrition
        get() = NutritionCalculator.scale(per100g, grams)

    /** Chỉ để hiển thị "x1,5" — không lưu vào DB. */
    val multiplier: Double
        get() = if (basePortionG > 0) grams / basePortionG else 1.0

    val canSave: Boolean
        get() = !saving && foodId != null && grams > 0

    val isVietnamese: Boolean get() = category == Food.CATEGORY_VIETNAMESE
}

class MealDetailViewModel(
    private val foodRepository: FoodRepository,
    private val mealRepository: MealRepository,
    private val foodId: String?,
    private val itemId: String?,
    initialMealType: MealType,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MealDetailUiState(
            isEditing = itemId != null,
            mealType = initialMealType,
        )
    )
    val uiState: StateFlow<MealDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            if (itemId != null) {
                val item = mealRepository.getItem(itemId) ?: run {
                    _uiState.update { it.copy(loading = false, error = tr("Không tìm thấy món trong bữa", "Item not found in this meal")) }
                    return@launch
                }
                val meal = mealRepository.getMeal(item.mealId)
                val food = item.foodId?.let { foodRepository.getFood(it) }
                val per100 = food?.toPer100g() ?: item.toPer100gSnapshot()
                _uiState.update {
                    it.copy(
                        loading = false,
                        isEditing = true,
                        foodId = item.foodId,
                        foodName = item.foodName,
                        description = food?.description,
                        category = food?.category ?: Food.CATEGORY_VIETNAMESE,
                        portionName = food?.portionName ?: "",
                        method = food?.method,
                        source = food?.source,
                        basePortionG = item.basePortionG.coerceAtLeast(1),
                        per100g = per100,
                        grams = item.portionGrams,
                        mealType = MealType.fromKey(meal?.mealType),
                        date = meal?.date ?: DateUtils.todayKey(),
                    )
                }
                return@launch
            }

            val resolvedId = foodId ?: run {
                _uiState.update { it.copy(loading = false, error = tr("Thiếu món cần xem", "Missing item to view")) }
                return@launch
            }
            val food = foodRepository.getFood(resolvedId) ?: run {
                _uiState.update { it.copy(loading = false, error = tr("Không tìm thấy món trong danh mục", "Item not found in the catalogue")) }
                return@launch
            }
            _uiState.update {
                it.copy(
                    loading = false,
                    foodId = food.id,
                    foodName = food.name,
                    description = food.description,
                    category = food.category,
                    portionName = food.portionName,
                    method = food.method,
                    source = food.source,
                    basePortionG = food.portionWeightG.coerceAtLeast(1),
                    per100g = food.toPer100g(),
                    grams = food.portionWeightG.toDouble(),
                )
            }
        }
    }

    fun setGrams(grams: Double) {
        val snapped = if (grams >= 100) {
            (grams / 5).roundToInt() * 5.0
        } else {
            (grams / 2).roundToInt() * 2.0
        }
        _uiState.update { it.copy(grams = snapped.coerceIn(10.0, 3000.0)) }
    }

    fun setMealType(type: MealType) {
        _uiState.update { it.copy(mealType = type) }
    }

    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        val id = state.foodId ?: return
        if (state.grams <= 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(saving = true, error = null) }
            val food = foodRepository.getFood(id)
            if (food == null) {
                _uiState.update { it.copy(saving = false, error = tr("Món đã bị xoá khỏi danh mục", "This item was removed from the catalogue")) }
                return@launch
            }
            if (state.isEditing && itemId != null) {
                mealRepository.updateItem(itemId, food, state.grams, state.mealType)
            } else {
                mealRepository.addItem(
                    userId = UserRepository.LOCAL_USER_ID,
                    date = state.date,
                    mealType = state.mealType,
                    food = food,
                    grams = state.grams,
                )
            }
            _uiState.update { it.copy(saving = false) }
            onSaved()
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val id = itemId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(saving = true) }
            mealRepository.deleteItem(id)
            _uiState.update { it.copy(saving = false) }
            onDeleted()
        }
    }
}

private fun Food.toPer100g(): Nutrition = Nutrition(
    calories = caloriesPer100g,
    protein = proteinPer100g,
    carbs = carbsPer100g,
    fat = fatPer100g,
    fiber = fiberPer100g,
    sodiumMg = sodiumMgPer100g,
)

/** Trường hợp món trong danh mục đã bị xoá: suy ngược /100g từ snapshot đã lưu. */
private fun com.example.nutrivision.domain.model.MealItem.toPer100gSnapshot(): Nutrition {
    val base = basePortionG.takeIf { it > 0 } ?: 100
    return NutritionCalculator.scale(nutrition, 100.0 / base)
}
