package com.example.nutrivision

import com.example.nutrivision.domain.calc.NutritionCalculator
import com.example.nutrivision.domain.model.ActivityLevel
import com.example.nutrivision.domain.model.Goal
import com.example.nutrivision.domain.model.Nutrition
import com.example.nutrivision.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Kiểm tra công thức dinh dưỡng bằng số tính tay — không cần thiết bị Android.
 * Đây là lớp duy nhất quyết định mục tiêu calo nên phải có test.
 */
class NutritionCalculatorTest {

    @Test
    fun `BMR nam theo Mifflin-St Jeor`() {
        // 10*70 + 6.25*175 - 5*22 + 5 = 1688.75
        val bmr = NutritionCalculator.bmr(
            gender = UserProfile.GENDER_MALE,
            age = 22,
            heightCm = 175.0,
            weightKg = 70.0,
        )
        assertEquals(1688.75, bmr, 0.001)
    }

    @Test
    fun `BMR nu theo Mifflin-St Jeor`() {
        // 10*55 + 6.25*160 - 5*30 - 161 = 1239
        val bmr = NutritionCalculator.bmr(
            gender = UserProfile.GENDER_FEMALE,
            age = 30,
            heightCm = 160.0,
            weightKg = 55.0,
        )
        assertEquals(1239.0, bmr, 0.001)
    }

    @Test
    fun `BMR tra ve 0 khi thieu du lieu`() {
        assertEquals(0.0, NutritionCalculator.bmr(UserProfile.GENDER_MALE, 0, 170.0, 60.0), 0.0)
        assertEquals(0.0, NutritionCalculator.bmr(UserProfile.GENDER_MALE, 20, 0.0, 60.0), 0.0)
        assertEquals(0.0, NutritionCalculator.bmr(UserProfile.GENDER_MALE, 20, 170.0, 0.0), 0.0)
    }

    @Test
    fun `TDEE nhan he so van dong`() {
        assertEquals(2000 * 1.2, NutritionCalculator.tdee(2000.0, ActivityLevel.SEDENTARY), 0.001)
        assertEquals(2000 * 1.55, NutritionCalculator.tdee(2000.0, ActivityLevel.MODERATE), 0.001)
        assertEquals(2000 * 1.9, NutritionCalculator.tdee(2000.0, ActivityLevel.VERY_ACTIVE), 0.001)
    }

    @Test
    fun `Muc tieu calo theo muc tieu can nang`() {
        assertEquals(1700, NutritionCalculator.targetCalories(2000.0, Goal.LOSE))
        assertEquals(2000, NutritionCalculator.targetCalories(2000.0, Goal.MAINTAIN))
        assertEquals(2300, NutritionCalculator.targetCalories(2000.0, Goal.GAIN))
    }

    @Test
    fun `Targets tra dung calo va macro`() {
        val targets = NutritionCalculator.targets(
            gender = UserProfile.GENDER_MALE,
            age = 22,
            heightCm = 175.0,
            weightKg = 70.0,
            activityLevel = ActivityLevel.MODERATE,
            goal = Goal.MAINTAIN,
        )
        // TDEE = 1688.75 * 1.55 = 2617.5625 -> 2618 kcal
        assertEquals(2618, targets.calories)
        // Đạm = 70 kg * 1.2 = 84 g
        assertEquals(84.0, targets.proteinG, 0.001)
        // Carb = 2618 * 0.45 / 4 = 294.525 -> 294.5 g
        assertEquals(294.5, targets.carbsG, 0.001)
        // Béo = 2618 * 0.25 / 9 = 72.72 -> 72.7 g
        assertEquals(72.7, targets.fatG, 0.001)
    }

    @Test
    fun `Doi gram quy doi dung tu 100g`() {
        val per100g = Nutrition(
            calories = 125.6,
            protein = 6.4,
            carbs = 18.0,
            fat = 3.2,
            fiber = 0.8,
            sodiumMg = 420.0,
        )
        val half = NutritionCalculator.scale(per100g, 500.0)
        assertEquals(628.0, half.calories, 0.001)
        assertEquals(32.0, half.protein, 0.001)
        assertEquals(90.0, half.carbs, 0.001)
        assertEquals(16.0, half.fat, 0.001)
        assertEquals(4.0, half.fiber, 0.001)
        assertEquals(2100.0, half.sodiumMg, 0.001)

        val full = NutritionCalculator.scale(per100g, 700.0)
        assertEquals(879.2, full.calories, 0.01)
    }

    @Test
    fun `Scale voi 0 gram tra ve khong`() {
        val per100g = Nutrition(100.0, 10.0, 10.0, 10.0, 1.0, 100.0)
        assertEquals(Nutrition.ZERO, NutritionCalculator.scale(per100g, 0.0))
        assertEquals(Nutrition.ZERO, NutritionCalculator.scale(per100g, -50.0))
    }
}
