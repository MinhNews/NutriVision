package com.example.nutrivision.data.local.relation

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Relation
import com.example.nutrivision.data.local.entity.MealEntity
import com.example.nutrivision.data.local.entity.MealItemEntity

data class MealWithItemsEntity(
    @Embedded val meal: MealEntity,
    @Relation(parentColumn = "id", entityColumn = "meal_id")
    val items: List<MealItemEntity>,
)

/** Tổng dinh dưỡng của một ngày — kết quả SUM, không lưu cứng. */
data class NutritionSumRow(
    @ColumnInfo(name = "kcal") val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double,
    @ColumnInfo(name = "sodium_mg") val sodiumMg: Double,
)

data class DailyTotalRow(
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "kcal") val calories: Double,
)
