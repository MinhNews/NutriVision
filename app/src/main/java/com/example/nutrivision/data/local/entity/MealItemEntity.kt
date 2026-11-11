package com.example.nutrivision.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Bảng 4: một món nằm trong một bữa.
 * Dinh dưỡng là BẢN SAO tại thời điểm lưu — sửa foods sau này không làm lịch sử đổi số.
 */
@Entity(
    tableName = "meal_items",
    foreignKeys = [
        ForeignKey(
            entity = MealEntity::class,
            parentColumns = ["id"],
            childColumns = ["meal_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = FoodEntity::class,
            parentColumns = ["id"],
            childColumns = ["food_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["meal_id"]),
        Index(value = ["food_id"]),
        Index(value = ["meal_id", "deleted_at"]),
    ],
)
data class MealItemEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "meal_id")
    val mealId: String,

    @ColumnInfo(name = "food_id")
    val foodId: String?,

    /** Tên món lúc lưu — snapshot. */
    @ColumnInfo(name = "food_name")
    val foodName: String,

    /** Số gram thật người dùng chọn — nguồn duy nhất để tính. */
    @ColumnInfo(name = "portion_grams")
    val portionGrams: Double,

    /** Khẩu phần gốc của món lúc lưu, để hiện "x1,5" và dựng lại chip. */
    @ColumnInfo(name = "base_portion_g")
    val basePortionG: Int,

    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double,

    @ColumnInfo(name = "sodium_mg")
    val sodiumMg: Double,

    /** user / model / gemini — tuần này chỉ có "user". */
    val source: String,

    val confidence: Double?,

    @ColumnInfo(name = "confirmed")
    val confirmed: Int,

    @ColumnInfo(name = "photo_uri")
    val photoUri: String?,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,

    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null,
) {
    companion object {
        const val SOURCE_USER = "user"
        const val SOURCE_MODEL = "model"
        const val SOURCE_GEMINI = "gemini"
    }
}
