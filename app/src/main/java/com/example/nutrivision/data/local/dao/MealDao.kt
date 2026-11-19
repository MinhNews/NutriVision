package com.example.nutrivision.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.nutrivision.data.local.entity.MealEntity
import com.example.nutrivision.data.local.entity.MealItemEntity
import com.example.nutrivision.data.local.relation.DataFingerprint
import com.example.nutrivision.data.local.relation.DailyTotalRow
import com.example.nutrivision.data.local.relation.MealWithItemsEntity
import com.example.nutrivision.data.local.relation.NutritionSumRow
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {

    @Transaction
    @Query(
        """
        SELECT * FROM meals
        WHERE user_id = :userId AND date = :date AND deleted_at IS NULL
        ORDER BY CASE meal_type
            WHEN 'breakfast' THEN 1
            WHEN 'lunch' THEN 2
            WHEN 'dinner' THEN 3
            ELSE 4 END
        """
    )
    fun observeMealsWithItems(userId: String, date: String): Flow<List<MealWithItemsEntity>>

    @Transaction
    @Query("SELECT * FROM meals WHERE id = :mealId LIMIT 1")
    fun observeMealWithItems(mealId: String): Flow<MealWithItemsEntity?>

    @Query(
        """
        SELECT COALESCE(SUM(i.calories), 0) AS kcal,
               COALESCE(SUM(i.protein), 0) AS protein,
               COALESCE(SUM(i.carbs), 0) AS carbs,
               COALESCE(SUM(i.fat), 0) AS fat,
               COALESCE(SUM(i.fiber), 0) AS fiber,
               COALESCE(SUM(i.sodium_mg), 0) AS sodium_mg
        FROM meal_items i
        JOIN meals m ON m.id = i.meal_id
        WHERE m.user_id = :userId AND m.date = :date
          AND m.deleted_at IS NULL AND i.deleted_at IS NULL
        """
    )
    fun observeDayNutrition(userId: String, date: String): Flow<NutritionSumRow>

    @Query(
        """
        SELECT COALESCE(SUM(i.calories), 0) AS kcal,
               COALESCE(SUM(i.protein), 0) AS protein,
               COALESCE(SUM(i.carbs), 0) AS carbs,
               COALESCE(SUM(i.fat), 0) AS fat,
               COALESCE(SUM(i.fiber), 0) AS fiber,
               COALESCE(SUM(i.sodium_mg), 0) AS sodium_mg
        FROM meal_items i
        JOIN meals m ON m.id = i.meal_id
        WHERE m.user_id = :userId
          AND m.date BETWEEN :fromDate AND :toDate
          AND m.deleted_at IS NULL AND i.deleted_at IS NULL
        """
    )
    fun observeRangeNutrition(userId: String, fromDate: String, toDate: String): Flow<NutritionSumRow>

    @Query(
        """
        SELECT m.date AS date, COALESCE(SUM(i.calories), 0) AS kcal
        FROM meals m
        JOIN meal_items i ON i.meal_id = m.id
        WHERE m.user_id = :userId
          AND m.deleted_at IS NULL AND i.deleted_at IS NULL
          AND m.date BETWEEN :fromDate AND :toDate
        GROUP BY m.date
        ORDER BY m.date
        """
    )
    fun observeDailyTotals(userId: String, fromDate: String, toDate: String): Flow<List<DailyTotalRow>>

    @Query("SELECT * FROM meals WHERE id = :mealId LIMIT 1")
    suspend fun getMeal(mealId: String): MealEntity?

    @Query(
        """
        SELECT * FROM meals
        WHERE user_id = :userId AND date = :date AND meal_type = :mealType
        LIMIT 1
        """
    )
    suspend fun findMeal(userId: String, date: String, mealType: String): MealEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMeal(meal: MealEntity): Long

    @Update
    suspend fun updateMeal(meal: MealEntity)

    @Query("UPDATE meals SET deleted_at = :now, updated_at = :now, sync_status = 'pending_delete' WHERE id = :mealId")
    suspend fun softDeleteMeal(mealId: String, now: Long)

    @Query("DELETE FROM meals WHERE id = :mealId")
    suspend fun hardDeleteMeal(mealId: String)

    @Insert
    suspend fun insertItem(item: MealItemEntity)

    @Update
    suspend fun updateItem(item: MealItemEntity)

    @Query("SELECT * FROM meal_items WHERE id = :itemId LIMIT 1")
    suspend fun getItem(itemId: String): MealItemEntity?

    @Query("SELECT * FROM meal_items WHERE id = :itemId LIMIT 1")
    fun observeItem(itemId: String): Flow<MealItemEntity?>

    @Query("SELECT * FROM meal_items WHERE meal_id = :mealId AND deleted_at IS NULL ORDER BY sort_order")
    suspend fun getItems(mealId: String): List<MealItemEntity>

    @Query("SELECT COUNT(*) FROM meal_items WHERE meal_id = :mealId AND deleted_at IS NULL")
    suspend fun countActiveItems(mealId: String): Int

    @Query("SELECT COALESCE(MAX(sort_order), -1) FROM meal_items WHERE meal_id = :mealId")
    suspend fun maxSortOrder(mealId: String): Int

    @Query("UPDATE meal_items SET deleted_at = :now, updated_at = :now WHERE id = :itemId")
    suspend fun softDeleteItem(itemId: String, now: Long)

    /** Đếm món còn sống đang dùng cùng một file ảnh — để biết có được xoá file không. */
    @Query("SELECT COUNT(*) FROM meal_items WHERE photo_uri = :photoUri AND deleted_at IS NULL")
    suspend fun countItemsUsingPhoto(photoUri: String): Int

    /** Gắn lại đường dẫn ảnh sau khi tải từ Firestore về; không đổi updated_at để khỏi đẩy lại. */
    @Query("UPDATE meal_items SET photo_uri = :photoUri WHERE id = :itemId")
    suspend fun setItemPhoto(itemId: String, photoUri: String)

    @Query("UPDATE meal_items SET deleted_at = :now, updated_at = :now WHERE meal_id = :mealId AND deleted_at IS NULL")
    suspend fun softDeleteItemsOfMeal(mealId: String, now: Long)

    @Query("DELETE FROM meal_items WHERE id = :itemId")
    suspend fun hardDeleteItem(itemId: String)

    @Query("UPDATE meal_items SET meal_id = :mealId, updated_at = :now WHERE id = :itemId")
    suspend fun moveItem(itemId: String, mealId: String, now: Long)

    // ------------------------------------------------------------------ đồng bộ Firestore

    @Query("SELECT * FROM meals WHERE user_id = :userId")
    suspend fun allMeals(userId: String): List<MealEntity>

    @Query(
        """
        SELECT * FROM meal_items
        WHERE meal_id IN (SELECT id FROM meals WHERE user_id = :userId)
        """
    )
    suspend fun allItemsOfUser(userId: String): List<MealItemEntity>

    /**
     * Upsert cho đồng bộ: không dùng REPLACE vì REPLACE = DELETE + INSERT,
     * mà meals là bảng cha của meal_items (CASCADE) nên sẽ xoá mất món trong bữa.
     */
    @Transaction
    suspend fun upsertMeal(meal: MealEntity) {
        if (getMeal(meal.id) == null) insertMeal(meal) else updateMeal(meal)
    }

    @Transaction
    suspend fun upsertItem(item: MealItemEntity) {
        if (getItem(item.id) == null) insertItem(item) else updateItem(item)
    }

    @Query("UPDATE meals SET sync_status = :status WHERE id IN (:ids)")
    suspend fun setMealSyncStatus(ids: List<String>, status: String)

    // ------------------------------------------------------- nhắc nhở & đồng bộ tự động

    /** Số món đã ghi cho một bữa trong ngày - worker dùng để không nhắc lại bữa đã ghi. */
    @Query(
        """
        SELECT COUNT(*) FROM meal_items i
        JOIN meals m ON i.meal_id = m.id
        WHERE m.user_id = :userId AND m.date = :date AND m.meal_type = :mealType
          AND m.deleted_at IS NULL AND i.deleted_at IS NULL
        """
    )
    suspend fun countMealTypeItems(userId: String, date: String, mealType: String): Int

    /** Tổng calo đã ăn trong ngày - dùng cho nội dung thông báo. */
    @Query(
        """
        SELECT COALESCE(SUM(i.calories), 0) FROM meal_items i
        JOIN meals m ON i.meal_id = m.id
        WHERE m.user_id = :userId AND m.date = :date
          AND m.deleted_at IS NULL AND i.deleted_at IS NULL
        """
    )
    suspend fun totalCalories(userId: String, date: String): Double

    @Query(
        """
        SELECT COUNT(*) AS row_count, COALESCE(MAX(updated_at), 0) AS last_update
        FROM meals WHERE user_id = :userId
        """
    )
    fun observeMealFingerprint(userId: String): Flow<DataFingerprint>

    @Query(
        """
        SELECT COUNT(*) AS row_count, COALESCE(MAX(i.updated_at), 0) AS last_update
        FROM meal_items i
        JOIN meals m ON i.meal_id = m.id
        WHERE m.user_id = :userId
        """
    )
    fun observeItemFingerprint(userId: String): Flow<DataFingerprint>
}
