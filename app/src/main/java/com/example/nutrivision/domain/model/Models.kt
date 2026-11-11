package com.example.nutrivision.domain.model

import com.example.nutrivision.domain.text.tr

/** Bốn bữa trong ngày — dùng chung giữa DB, ViewModel và UI. */
enum class MealType(
    val key: String,
    private val labelVi: String,
    private val labelEn: String,
    private val shortVi: String,
    private val shortEn: String,
) {
    BREAKFAST("breakfast", "Bữa sáng", "Breakfast", "Sáng", "AM"),
    LUNCH("lunch", "Bữa trưa", "Lunch", "Trưa", "Noon"),
    DINNER("dinner", "Bữa tối", "Dinner", "Tối", "PM"),
    SNACK("snack", "Bữa phụ", "Snack", "Phụ", "Snack");

    val label: String get() = tr(labelVi, labelEn)
    val shortLabel: String get() = tr(shortVi, shortEn)

    companion object {
        fun fromKey(key: String?): MealType = entries.firstOrNull { it.key == key } ?: SNACK
    }
}

enum class ActivityLevel(
    val key: String,
    private val labelVi: String,
    private val labelEn: String,
    private val descriptionVi: String,
    private val descriptionEn: String,
    val factor: Double,
) {
    SEDENTARY(
        "sedentary", "Ít vận động", "Sedentary",
        "Ngồi nhiều, hầu như không tập", "Mostly sitting, little exercise", 1.2,
    ),
    LIGHT(
        "light", "Vận động nhẹ", "Lightly active",
        "Tập 1–3 buổi/tuần", "1–3 workouts per week", 1.375,
    ),
    MODERATE(
        "moderate", "Vừa phải", "Moderately active",
        "Tập 3–5 buổi/tuần", "3–5 workouts per week", 1.55,
    ),
    ACTIVE(
        "active", "Năng động", "Active",
        "Tập 6–7 buổi/tuần", "6–7 workouts per week", 1.725,
    ),
    VERY_ACTIVE(
        "very_active", "Rất năng động", "Very active",
        "Lao động nặng hoặc tập 2 lần/ngày", "Heavy labour or two sessions a day", 1.9,
    );

    val label: String get() = tr(labelVi, labelEn)
    val description: String get() = tr(descriptionVi, descriptionEn)

    companion object {
        fun fromKey(key: String?): ActivityLevel = entries.firstOrNull { it.key == key } ?: MODERATE
    }
}

enum class Goal(
    val key: String,
    private val labelVi: String,
    private val labelEn: String,
    private val descriptionVi: String,
    private val descriptionEn: String,
) {
    LOSE("lose", "Giảm cân", "Lose weight", "Thâm hụt 15% calo", "15% calorie deficit"),
    MAINTAIN("maintain", "Duy trì", "Maintain", "Ăn đúng nhu cầu", "Eat at maintenance"),
    GAIN("gain", "Tăng cân", "Gain weight", "Thặng dư 15% calo", "15% calorie surplus");

    val label: String get() = tr(labelVi, labelEn)
    val description: String get() = tr(descriptionVi, descriptionEn)

    companion object {
        fun fromKey(key: String?): Goal = entries.firstOrNull { it.key == key } ?: MAINTAIN
    }
}

/** Dinh dưỡng đã quy đổi cho một khẩu phần cụ thể (không lưu trong bảng foods). */
data class Nutrition(
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double,
    val sodiumMg: Double,
) {
    operator fun plus(other: Nutrition) = Nutrition(
        calories = calories + other.calories,
        protein = protein + other.protein,
        carbs = carbs + other.carbs,
        fat = fat + other.fat,
        fiber = fiber + other.fiber,
        sodiumMg = sodiumMg + other.sodiumMg,
    )

    companion object {
        val ZERO = Nutrition(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    }
}

data class UserProfile(
    val userId: String,
    val email: String,
    val displayName: String?,
    val gender: String,
    val age: Int,
    val heightCm: Double,
    val weightKg: Double,
    val activityLevel: ActivityLevel,
    val goal: Goal,
    val bmr: Double,
    val tdee: Double,
    val targetCalories: Int,
    val targetProtein: Double,
    val targetCarbs: Double,
    val targetFat: Double,
    val createdAt: Long,
    val updatedAt: Long,
) {
    val bmi: Double
        get() = if (heightCm > 0) weightKg / ((heightCm / 100.0) * (heightCm / 100.0)) else 0.0

    val bmiLabel: String
        get() = when {
            bmi <= 0.0 -> tr("Chưa có dữ liệu", "No data")
            bmi < 18.5 -> tr("Thiếu cân", "Underweight")
            bmi < 23.0 -> tr("Bình thường", "Normal")
            bmi < 25.0 -> tr("Thừa cân nhẹ", "Slightly overweight")
            bmi < 30.0 -> tr("Thừa cân", "Overweight")
            else -> tr("Béo phì", "Obese")
        }

    val isMale: Boolean get() = gender == GENDER_MALE

    companion object {
        const val GENDER_MALE = "male"
        const val GENDER_FEMALE = "female"
    }
}

data class Food(
    val id: String,
    val name: String,
    val category: String,
    val kind: String,
    val modelClass: Boolean,
    val isCustom: Boolean,
    val portionName: String,
    val portionWeightG: Int,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double,
    val sodiumMg: Double,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val fatPer100g: Double,
    val fiberPer100g: Double,
    val sodiumMgPer100g: Double,
    val description: String?,
    val source: String?,
    val method: String?,
    val confidence: String?,
) {
    /** Calo/đạm/carb/béo cho đúng số gram người dùng chọn. */
    fun nutritionFor(grams: Double): Nutrition {
        if (grams <= 0.0) return Nutrition.ZERO
        val ratio = grams / 100.0
        return Nutrition(
            calories = caloriesPer100g * ratio,
            protein = proteinPer100g * ratio,
            carbs = carbsPer100g * ratio,
            fat = fatPer100g * ratio,
            fiber = fiberPer100g * ratio,
            sodiumMg = sodiumMgPer100g * ratio,
        )
    }

    val isDrink: Boolean get() = kind == KIND_DRINK

    companion object {
        const val KIND_DISH = "dish"
        const val KIND_DRINK = "drink"
        const val KIND_TOPPING = "topping"
        const val CATEGORY_VIETNAMESE = "vietnamese"
    }
}

/** Một dòng trong bữa ăn. Dinh dưỡng là bản sao tại thời điểm lưu (snapshot). */
data class MealItem(
    val id: String,
    val mealId: String,
    val foodId: String?,
    val foodName: String,
    val portionGrams: Double,
    val basePortionG: Int,
    val nutrition: Nutrition,
    val source: String,
    val confidence: Double?,
    val confirmed: Boolean,
    val photoUri: String?,
    val sortOrder: Int,
)

data class Meal(
    val id: String,
    val date: String,
    val type: MealType,
    val eatenAt: Long,
    val photoUri: String?,
    val note: String?,
) 

data class MealWithItems(
    val meal: Meal,
    val items: List<MealItem>,
) {
    val nutrition: Nutrition
        get() = items.fold(Nutrition.ZERO) { acc, item -> acc + item.nutrition }

    val isEmpty: Boolean get() = items.isEmpty()
}

data class DailySummary(
    val date: String,
    val consumed: Nutrition,
    val profile: UserProfile?,
) {
    val targetCalories: Int? get() = profile?.targetCalories
    val remainingCalories: Int?
        get() = profile?.let { (it.targetCalories - consumed.calories).toInt() }

    val calorieProgress: Float
        get() {
            val target = profile?.targetCalories ?: return 0f
            if (target <= 0) return 0f
            return (consumed.calories / target).toFloat().coerceIn(0f, 1f)
        }
}

data class DayTotal(
    val date: String,
    val calories: Double,
)
