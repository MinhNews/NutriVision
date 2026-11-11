package com.example.nutrivision.data.mapper

import com.example.nutrivision.data.local.entity.FoodEntity
import com.example.nutrivision.data.local.entity.MealEntity
import com.example.nutrivision.data.local.entity.MealItemEntity
import com.example.nutrivision.data.local.entity.UserEntity
import com.example.nutrivision.data.local.relation.DailyTotalRow
import com.example.nutrivision.data.local.relation.MealWithItemsEntity
import com.example.nutrivision.data.local.relation.NutritionSumRow
import com.example.nutrivision.domain.model.ActivityLevel
import com.example.nutrivision.domain.model.DayTotal
import com.example.nutrivision.domain.model.Food
import com.example.nutrivision.domain.model.Goal
import com.example.nutrivision.domain.model.Meal
import com.example.nutrivision.domain.model.MealItem
import com.example.nutrivision.domain.model.MealType
import com.example.nutrivision.domain.model.MealWithItems
import com.example.nutrivision.domain.model.Nutrition
import com.example.nutrivision.domain.model.UserProfile

fun UserEntity.toDomain(): UserProfile = UserProfile(
    userId = userId,
    email = email,
    displayName = displayName,
    gender = gender,
    age = age,
    heightCm = heightCm,
    weightKg = weightKg,
    activityLevel = ActivityLevel.fromKey(activityLevel),
    goal = Goal.fromKey(goal),
    bmr = bmr,
    tdee = tdee,
    targetCalories = targetCalories,
    targetProtein = targetProtein,
    targetCarbs = targetCarbs,
    targetFat = targetFat,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun UserProfile.toEntity(syncStatus: String = UserEntity.SYNC_PENDING): UserEntity = UserEntity(
    userId = userId,
    email = email,
    displayName = displayName,
    gender = gender,
    age = age,
    heightCm = heightCm,
    weightKg = weightKg,
    activityLevel = activityLevel.key,
    goal = goal.key,
    bmr = bmr,
    tdee = tdee,
    targetCalories = targetCalories,
    targetProtein = targetProtein,
    targetCarbs = targetCarbs,
    targetFat = targetFat,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus,
)

fun FoodEntity.toDomain(): Food = Food(
    id = id,
    name = name,
    category = category,
    kind = kind,
    modelClass = modelClass == 1,
    isCustom = isCustom == 1,
    portionName = portionName,
    portionWeightG = portionWeightG,
    calories = calories,
    protein = protein,
    carbs = carbs,
    fat = fat,
    fiber = fiber,
    sodiumMg = sodiumMg,
    caloriesPer100g = caloriesPer100g,
    proteinPer100g = proteinPer100g,
    carbsPer100g = carbsPer100g,
    fatPer100g = fatPer100g,
    fiberPer100g = fiberPer100g,
    sodiumMgPer100g = sodiumMgPer100g,
    description = description,
    source = source,
    method = method,
    confidence = confidence,
)

fun MealEntity.toDomain(): Meal = Meal(
    id = id,
    date = date,
    type = MealType.fromKey(mealType),
    eatenAt = eatenAt,
    photoUri = photoUri,
    note = note,
)

fun MealItemEntity.toDomain(): MealItem = MealItem(
    id = id,
    mealId = mealId,
    foodId = foodId,
    foodName = foodName,
    portionGrams = portionGrams,
    basePortionG = basePortionG,
    nutrition = Nutrition(
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        fiber = fiber,
        sodiumMg = sodiumMg,
    ),
    source = source,
    confidence = confidence,
    confirmed = confirmed == 1,
    photoUri = photoUri,
    sortOrder = sortOrder,
)

fun MealWithItemsEntity.toDomain(): MealWithItems = MealWithItems(
    meal = meal.toDomain(),
    items = items.filter { it.deletedAt == null }
        .sortedBy { it.sortOrder }
        .map { it.toDomain() },
)

fun NutritionSumRow.toDomain(): Nutrition = Nutrition(
    calories = calories,
    protein = protein,
    carbs = carbs,
    fat = fat,
    fiber = fiber,
    sodiumMg = sodiumMg,
)

fun DailyTotalRow.toDomain(): DayTotal = DayTotal(date = date, calories = calories)
