package com.example.nutrivision.domain.calc

import com.example.nutrivision.domain.model.ActivityLevel
import com.example.nutrivision.domain.model.Goal
import com.example.nutrivision.domain.model.Nutrition
import com.example.nutrivision.domain.model.UserProfile
import kotlin.math.roundToInt

data class MacroTargets(
    val calories: Int,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
)

/**
 * Công thức thuần Kotlin — không phụ thuộc Android nên test được bằng JUnit.
 *
 * BMR: Mifflin–St Jeor. TDEE = BMR × hệ số vận động.
 * Mục tiêu calo: giảm 15%, duy trì 0%, tăng 15%.
 * Macro: đạm theo g/kg cân nặng, carb 45% calo, béo 25% calo.
 */
object NutritionCalculator {

    fun bmr(gender: String, age: Int, heightCm: Double, weightKg: Double): Double {
        if (age <= 0 || heightCm <= 0 || weightKg <= 0) return 0.0
        val base = 10 * weightKg + 6.25 * heightCm - 5 * age
        return if (gender == UserProfile.GENDER_MALE) base + 5 else base - 161
    }

    fun tdee(bmr: Double, activityLevel: ActivityLevel): Double =
        (bmr * activityLevel.factor).coerceAtLeast(0.0)

    fun targetCalories(tdee: Double, goal: Goal): Int = when (goal) {
        Goal.LOSE -> (tdee * 0.85).roundToInt()
        Goal.MAINTAIN -> tdee.roundToInt()
        Goal.GAIN -> (tdee * 1.15).roundToInt()
    }.coerceAtLeast(0)

    fun proteinPerKg(goal: Goal): Double = when (goal) {
        Goal.LOSE -> 1.6
        Goal.MAINTAIN -> 1.2
        Goal.GAIN -> 1.8
    }

    fun targets(
        gender: String,
        age: Int,
        heightCm: Double,
        weightKg: Double,
        activityLevel: ActivityLevel,
        goal: Goal,
    ): MacroTargets {
        val bmrValue = bmr(gender, age, heightCm, weightKg)
        val tdeeValue = tdee(bmrValue, activityLevel)
        val calories = targetCalories(tdeeValue, goal)
        val protein = weightKg * proteinPerKg(goal)
        val carbs = calories * 0.45 / 4.0
        val fat = calories * 0.25 / 9.0
        return MacroTargets(
            calories = calories,
            proteinG = round1(protein),
            carbsG = round1(carbs),
            fatG = round1(fat),
        )
    }

    /** Dùng cho unit test và cho phần "dinh dưỡng theo gram" trên màn chi tiết. */
    fun scale(nutritionPer100g: Nutrition, grams: Double): Nutrition {
        if (grams <= 0.0) return Nutrition.ZERO
        val ratio = grams / 100.0
        return Nutrition(
            calories = nutritionPer100g.calories * ratio,
            protein = nutritionPer100g.protein * ratio,
            carbs = nutritionPer100g.carbs * ratio,
            fat = nutritionPer100g.fat * ratio,
            fiber = nutritionPer100g.fiber * ratio,
            sodiumMg = nutritionPer100g.sodiumMg * ratio,
        )
    }

    private fun round1(value: Double): Double = (value * 10).roundToInt() / 10.0
}
