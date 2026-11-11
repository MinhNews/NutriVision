package com.example.nutrivision.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Bảng 1: hồ sơ người dùng. Demo dùng 1 dòng user_id = "local". */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "display_name")
    val displayName: String?,

    @ColumnInfo(name = "gender")
    val gender: String,

    @ColumnInfo(name = "age")
    val age: Int,

    @ColumnInfo(name = "height_cm")
    val heightCm: Double,

    @ColumnInfo(name = "weight_kg")
    val weightKg: Double,

    @ColumnInfo(name = "activity_level")
    val activityLevel: String,

    @ColumnInfo(name = "goal")
    val goal: String,

    @ColumnInfo(name = "bmr")
    val bmr: Double,

    @ColumnInfo(name = "tdee")
    val tdee: Double,

    @ColumnInfo(name = "target_calories")
    val targetCalories: Int,

    @ColumnInfo(name = "target_protein")
    val targetProtein: Double,

    @ColumnInfo(name = "target_carbs")
    val targetCarbs: Double,

    @ColumnInfo(name = "target_fat")
    val targetFat: Double,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,

    @ColumnInfo(name = "sync_status")
    val syncStatus: String = SYNC_PENDING,
) {
    companion object {
        const val SYNC_PENDING = "pending"
        const val SYNC_SYNCED = "synced"
    }
}
