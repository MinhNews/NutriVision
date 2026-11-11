package com.example.nutrivision.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Bảng 2: danh mục món ăn. 145 dòng seed từ assets + món người dùng tự thêm.
 * Dinh dưỡng lưu cả bản cho khẩu phần gốc và bản /100g để quy đổi khi kéo gram.
 */
@Entity(
    tableName = "foods",
    indices = [
        Index(value = ["search_key"]),
        Index(value = ["kind"]),
        Index(value = ["model_class"]),
        Index(value = ["is_custom"]),
    ],
)
data class FoodEntity(
    @PrimaryKey
    val id: String,

    val name: String,

    val category: String,

    /** dish / drink / topping — tách khỏi category (việt/ngoại). */
    val kind: String,

    @ColumnInfo(name = "model_class")
    val modelClass: Int,

    @ColumnInfo(name = "is_custom")
    val isCustom: Int,

    @ColumnInfo(name = "owner_user_id")
    val ownerUserId: String?,

    @ColumnInfo(name = "portion_name")
    val portionName: String,

    @ColumnInfo(name = "portion_weight_g")
    val portionWeightG: Int,

    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double,

    @ColumnInfo(name = "sodium_mg")
    val sodiumMg: Double,

    @ColumnInfo(name = "calories_per_100g")
    val caloriesPer100g: Double,

    @ColumnInfo(name = "protein_per_100g")
    val proteinPer100g: Double,

    @ColumnInfo(name = "carbs_per_100g")
    val carbsPer100g: Double,

    @ColumnInfo(name = "fat_per_100g")
    val fatPer100g: Double,

    @ColumnInfo(name = "fiber_per_100g")
    val fiberPer100g: Double,

    @ColumnInfo(name = "sodium_mg_per_100g")
    val sodiumMgPer100g: Double,

    @ColumnInfo(name = "search_key")
    val searchKey: String,

    val description: String?,
    val source: String?,
    val method: String?,
    val confidence: String?,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,

    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null,
)
