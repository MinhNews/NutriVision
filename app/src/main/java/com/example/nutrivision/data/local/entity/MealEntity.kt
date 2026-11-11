package com.example.nutrivision.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Bảng 3: một lần ăn (Sáng / Trưa / Tối / Phụ) trong một ngày.
 * Mỗi ngày mỗi loại bữa chỉ có đúng 1 dòng → UNIQUE(user_id, date, meal_type).
 * KHÔNG lưu total_calories: tổng được tính bằng SUM trên meal_items.
 */
@Entity(
    tableName = "meals",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["user_id", "date", "meal_type"], unique = true),
        Index(value = ["user_id", "date"]),
        Index(value = ["sync_status"]),
    ],
)
data class MealEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "meal_type")
    val mealType: String,

    /** "yyyy-MM-dd" theo giờ máy. */
    val date: String,

    @ColumnInfo(name = "eaten_at")
    val eatenAt: Long,

    @ColumnInfo(name = "photo_uri")
    val photoUri: String?,

    val note: String?,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,

    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null,

    @ColumnInfo(name = "sync_status")
    val syncStatus: String = SYNC_PENDING,
) {
    companion object {
        const val SYNC_PENDING = "pending"
        const val SYNC_SYNCED = "synced"
        const val SYNC_PENDING_DELETE = "pending_delete"
    }
}
