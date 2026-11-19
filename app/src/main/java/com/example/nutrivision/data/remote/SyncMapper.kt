package com.example.nutrivision.data.remote

import com.example.nutrivision.data.local.entity.MealEntity
import com.example.nutrivision.data.local.entity.MealItemEntity
import com.example.nutrivision.data.local.entity.UserEntity

/**
 * Chuyển entity ↔ document Firestore.
 *
 * Quy ước: mọi trường thời gian là Long milliseconds (updated_at quyết định thắng thua),
 * mọi trường bị xoá mềm dùng deleted_at khác null (tombstone) để máy khác biết mà xoá theo.
 */
object SyncMapper {

    fun userToMap(user: UserEntity): Map<String, Any?> = mapOf(
        "email" to user.email,
        "display_name" to user.displayName,
        "gender" to user.gender,
        "age" to user.age,
        "height_cm" to user.heightCm,
        "weight_kg" to user.weightKg,
        "activity_level" to user.activityLevel,
        "goal" to user.goal,
        "bmr" to user.bmr,
        "tdee" to user.tdee,
        "target_calories" to user.targetCalories,
        "target_protein" to user.targetProtein,
        "target_carbs" to user.targetCarbs,
        "target_fat" to user.targetFat,
        "created_at" to user.createdAt,
        "updated_at" to user.updatedAt,
    )

    fun userFromMap(userId: String, map: Map<String, Any?>): UserEntity = UserEntity(
        userId = userId,
        email = map.string("email"),
        displayName = map["display_name"] as? String,
        gender = map.string("gender", "male"),
        age = map.long("age").toInt(),
        heightCm = map.double("height_cm"),
        weightKg = map.double("weight_kg"),
        activityLevel = map.string("activity_level"),
        goal = map.string("goal"),
        bmr = map.double("bmr"),
        tdee = map.double("tdee"),
        targetCalories = map.long("target_calories").toInt(),
        targetProtein = map.double("target_protein"),
        targetCarbs = map.double("target_carbs"),
        targetFat = map.double("target_fat"),
        createdAt = map.long("created_at"),
        updatedAt = map.long("updated_at"),
        syncStatus = UserEntity.SYNC_SYNCED,
    )

    fun mealToMap(meal: MealEntity): Map<String, Any?> = mapOf(
        "meal_type" to meal.mealType,
        "date" to meal.date,
        "eaten_at" to meal.eatenAt,
        "note" to meal.note,
        "created_at" to meal.createdAt,
        "updated_at" to meal.updatedAt,
        "deleted_at" to meal.deletedAt,
    )

    fun mealFromMap(userId: String, id: String, map: Map<String, Any?>): MealEntity = MealEntity(
        id = id,
        userId = userId,
        mealType = map.string("meal_type", "snack"),
        date = map.string("date"),
        eatenAt = map.long("eaten_at"),
        photoUri = null,
        note = map["note"] as? String,
        createdAt = map.long("created_at"),
        updatedAt = map.long("updated_at"),
        deletedAt = map.optionalLong("deleted_at"),
        syncStatus = MealEntity.SYNC_SYNCED,
    )

    fun itemToMap(item: MealItemEntity, hasPhoto: Boolean = false): Map<String, Any?> = mapOf(
        "meal_id" to item.mealId,
        "food_id" to item.foodId,
        "food_name" to item.foodName,
        "portion_grams" to item.portionGrams,
        "base_portion_g" to item.basePortionG,
        "calories" to item.calories,
        "protein" to item.protein,
        "carbs" to item.carbs,
        "fat" to item.fat,
        "fiber" to item.fiber,
        "sodium_mg" to item.sodiumMg,
        "source" to item.source,
        "confidence" to item.confidence,
        "confirmed" to item.confirmed,
        "sort_order" to item.sortOrder,
        "created_at" to item.createdAt,
        "updated_at" to item.updatedAt,
        "deleted_at" to item.deletedAt,
        // Ảnh nằm ở Firebase Storage (users/{uid}/photos/{itemId}.jpg), Firestore chỉ giữ cờ.
        "has_photo" to hasPhoto,
    )

    fun itemFromMap(id: String, map: Map<String, Any?>): MealItemEntity = MealItemEntity(
        id = id,
        mealId = map.string("meal_id"),
        // Món do AI tạo có id sinh trên máy kia → có thể chưa tồn tại ở đây, để null cho an toàn.
        foodId = (map["food_id"] as? String)?.takeIf { it.isNotBlank() },
        foodName = map.string("food_name"),
        portionGrams = map.double("portion_grams"),
        basePortionG = map.long("base_portion_g").toInt(),
        calories = map.double("calories"),
        protein = map.double("protein"),
        carbs = map.double("carbs"),
        fat = map.double("fat"),
        fiber = map.double("fiber"),
        sodiumMg = map.double("sodium_mg"),
        source = map.string("source", MealItemEntity.SOURCE_USER),
        confidence = (map["confidence"] as? Number)?.toDouble(),
        confirmed = map.long("confirmed", 1L).toInt(),
        photoUri = null,
        sortOrder = map.long("sort_order").toInt(),
        createdAt = map.long("created_at"),
        updatedAt = map.long("updated_at"),
        deletedAt = map.optionalLong("deleted_at"),
    )

    private fun Map<String, Any?>.string(key: String, fallback: String = ""): String =
        (this[key] as? String)?.takeIf { it.isNotBlank() } ?: fallback

    private fun Map<String, Any?>.long(key: String, fallback: Long = 0L): Long =
        (this[key] as? Number)?.toLong() ?: fallback

    private fun Map<String, Any?>.optionalLong(key: String): Long? =
        (this[key] as? Number)?.toLong()

    private fun Map<String, Any?>.double(key: String, fallback: Double = 0.0): Double =
        (this[key] as? Number)?.toDouble() ?: fallback
}
