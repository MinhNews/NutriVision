package com.example.nutrivision.ui.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrivision.data.ai.FoodClassifier
import com.example.nutrivision.data.ai.FoodPrediction
import com.example.nutrivision.data.local.entity.MealItemEntity
import com.example.nutrivision.data.remote.AiDishResult
import com.example.nutrivision.data.remote.GeminiFoodService
import com.example.nutrivision.data.repository.FoodRepository
import com.example.nutrivision.data.repository.MealRepository
import com.example.nutrivision.domain.calc.DateUtils
import com.example.nutrivision.domain.model.Food
import com.example.nutrivision.domain.model.MealType
import com.example.nutrivision.domain.text.tr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar

enum class RecognizeStage { IDLE, ANALYZING, RESULT, FAILED }

/** Một món model gợi ý (có thể chưa khớp được món nào trong danh mục). */
data class PhotoCandidate(
    val foodId: String?,
    val name: String,
    val confidence: Float,
) {
    val percent: Int get() = (confidence * 100).toInt()
}

data class CameraUiState(
    val stage: RecognizeStage = RecognizeStage.IDLE,
    val photoPath: String? = null,
    val candidates: List<PhotoCandidate> = emptyList(),
    val food: Food? = null,
    val confidence: Double? = null,
    val source: String = MealItemEntity.SOURCE_MODEL,
    val grams: Double = 100.0,
    val mealType: MealType = defaultMealType(),
    val aiBusy: Boolean = false,
    val message: String? = null,
    val savedItemId: String? = null,
) {
    /** Model coi là chắc chắn khi đạt ngưỡng 0,7 lấy từ model_meta.json. */
    val confident: Boolean get() = (confidence ?: 0.0) >= FoodClassifier.CONFIDENCE_THRESHOLD

    val canSave: Boolean get() = food != null && grams > 0
}

/**
 * Toàn bộ luồng "chụp → nhận diện → lưu bữa ăn".
 *
 * Model chạy trên máy (không tốn mạng). Chỉ khi người dùng bấm "Nhờ AI mô tả"
 * ảnh mới được gửi lên Gemini, và món do Gemini đoán được lưu thành món riêng
 * (is_custom = 1) để không làm bẩn danh mục chuẩn.
 */
class CameraViewModel(
    private val userId: String,
    private val foodRepository: FoodRepository,
    private val mealRepository: MealRepository,
    private val classifier: FoodClassifier,
    private val gemini: GeminiFoodService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    val aiConfigured: Boolean get() = gemini.isConfigured

    /** Gọi sau khi CameraX đã ghi ảnh ra file. */
    fun onPhotoCaptured(path: String) {
        _uiState.value = _uiState.value.copy(
            stage = RecognizeStage.ANALYZING,
            photoPath = path,
            candidates = emptyList(),
            food = null,
            confidence = null,
            source = MealItemEntity.SOURCE_MODEL,
            message = null,
            savedItemId = null,
        )
        viewModelScope.launch {
            val bitmap = withContext(Dispatchers.IO) { loadBitmap(path) }
            if (bitmap == null) {
                fail(tr("Không mở được ảnh vừa chụp.", "Could not open the photo just taken."))
                return@launch
            }
            val predictions = withContext(Dispatchers.Default) { classifier.classify(bitmap) }
            bitmap.recycle()

            if (predictions.isEmpty()) {
                fail(tr("Model chưa nhận ra món nào.", "The model could not recognise any dish."))
                return@launch
            }
            val candidates = predictions.map { it.toCandidate() }
            val best = predictions.first()
            val bestFood = candidates.firstOrNull()?.foodId?.let { foodRepository.getFood(it) }
            _uiState.value = _uiState.value.copy(
                stage = RecognizeStage.RESULT,
                candidates = candidates,
                food = bestFood,
                confidence = best.confidence.toDouble(),
                grams = (bestFood?.portionWeightG ?: 100).toDouble(),
                source = MealItemEntity.SOURCE_MODEL,
            )
        }
    }

    fun selectCandidate(candidate: PhotoCandidate) {
        val foodId = candidate.foodId ?: return
        viewModelScope.launch {
            val food = foodRepository.getFood(foodId) ?: return@launch
            _uiState.value = _uiState.value.copy(
                food = food,
                confidence = candidate.confidence.toDouble(),
                grams = food.portionWeightG.toDouble(),
                source = MealItemEntity.SOURCE_MODEL,
                message = null,
            )
        }
    }

    /** Nút "Nhờ AI mô tả" — chỉ gọi khi người dùng chủ động bấm. */
    fun askAi() {
        val path = _uiState.value.photoPath ?: return
        if (_uiState.value.aiBusy) return
        _uiState.value = _uiState.value.copy(aiBusy = true, message = null)
        viewModelScope.launch {
            val bitmap = withContext(Dispatchers.IO) { loadBitmap(path, maxSide = 1024) }
            if (bitmap == null) {
                _uiState.value = _uiState.value.copy(
                    aiBusy = false,
                    message = tr("Không mở được ảnh để gửi AI.", "Could not open the photo to send to AI."),
                )
                return@launch
            }
            val result = gemini.describeMeal(bitmap)
            bitmap.recycle()
            when (result) {
                is AiDishResult.Success -> {
                    val dish = result.dish
                    val food = foodRepository.saveAiFood(
                        userId = userId,
                        name = dish.name,
                        portionGrams = dish.portionGrams,
                        calories = dish.calories,
                        protein = dish.protein,
                        carbs = dish.carbs,
                        fat = dish.fat,
                        note = dish.note,
                    )
                    _uiState.value = _uiState.value.copy(
                        stage = RecognizeStage.RESULT,
                        aiBusy = false,
                        food = food,
                        grams = dish.portionGrams,
                        confidence = dish.confidence,
                        source = MealItemEntity.SOURCE_GEMINI,
                        message = dish.note,
                    )
                }
                is AiDishResult.Failure -> {
                    _uiState.value = _uiState.value.copy(aiBusy = false, message = result.message)
                }
            }
        }
    }

    fun setGrams(grams: Double) {
        _uiState.value = _uiState.value.copy(grams = grams.coerceIn(5.0, 2000.0))
    }

    fun setMealType(type: MealType) {
        _uiState.value = _uiState.value.copy(mealType = type)
    }

    fun save() {
        val state = _uiState.value
        val food = state.food ?: return
        if (state.savedItemId != null) return
        viewModelScope.launch {
            val itemId = mealRepository.addItem(
                userId = userId,
                date = DateUtils.todayKey(),
                mealType = state.mealType,
                food = food,
                grams = state.grams,
                source = state.source,
                confidence = state.confidence,
                photoUri = state.photoPath,
            )
            _uiState.value = _uiState.value.copy(savedItemId = itemId)
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    /** Quay lại màn chụp, xoá ảnh chưa dùng để không để rác trong bộ nhớ máy. */
    fun reset(discardPhoto: Boolean = true) {
        val state = _uiState.value
        if (discardPhoto && state.savedItemId == null) {
            state.photoPath?.let { runCatching { File(it).delete() } }
        }
        _uiState.value = CameraUiState(mealType = state.mealType)
    }

    private fun fail(message: String) {
        _uiState.value = _uiState.value.copy(stage = RecognizeStage.FAILED, message = message)
    }

    private fun FoodPrediction.toCandidate(): PhotoCandidate {
        val name = runCatching { classifier.displayName(label) }.getOrDefault(label)
        return PhotoCandidate(foodId = label, name = name, confidence = confidence)
    }

    companion object {
        /** Ảnh chỉ cần đủ nét để hiển thị và chạy model, không cần giữ 12 MP. */
        fun loadBitmap(path: String, maxSide: Int = 1280): Bitmap? = runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, bounds)
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / sample > maxSide) sample *= 2
            BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
        }.getOrNull()
    }
}

/** Bữa mặc định theo giờ máy: sáng < 10h, trưa < 15h, tối < 21h, còn lại là bữa phụ. */
internal fun defaultMealType(now: Calendar = Calendar.getInstance()): MealType {
    val hour = now.get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 10 -> MealType.BREAKFAST
        hour < 15 -> MealType.LUNCH
        hour < 21 -> MealType.DINNER
        else -> MealType.SNACK
    }
}
