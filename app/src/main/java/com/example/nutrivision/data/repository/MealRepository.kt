package com.example.nutrivision.data.repository

import androidx.room.withTransaction
import com.example.nutrivision.data.local.NutriVisionDatabase
import com.example.nutrivision.data.local.dao.MealDao
import com.example.nutrivision.data.local.entity.MealEntity
import com.example.nutrivision.data.local.entity.MealItemEntity
import com.example.nutrivision.data.mapper.toDomain
import com.example.nutrivision.domain.model.DayTotal
import com.example.nutrivision.domain.model.Food
import com.example.nutrivision.domain.model.MealItem
import com.example.nutrivision.domain.model.MealType
import com.example.nutrivision.domain.model.MealWithItems
import com.example.nutrivision.domain.model.Nutrition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Mọi thao tác ghi đều chạy trong transaction:
 * tạo bữa nếu chưa có → thêm món → tính lại tổng chỉ khi đọc (SUM).
 */
class MealRepository(
    private val db: NutriVisionDatabase,
    private val mealDao: MealDao,
) {

    fun observeDay(userId: String, date: String): Flow<List<MealWithItems>> =
        mealDao.observeMealsWithItems(userId, date)
            .map { list -> list.map { it.toDomain() } }

    fun observeDayNutrition(userId: String, date: String): Flow<Nutrition> =
        mealDao.observeDayNutrition(userId, date).map { it.toDomain() }

    fun observeRangeNutrition(userId: String, fromDate: String, toDate: String): Flow<Nutrition> =
        mealDao.observeRangeNutrition(userId, fromDate, toDate).map { it.toDomain() }

    fun observeDailyTotals(userId: String, fromDate: String, toDate: String): Flow<List<DayTotal>> =
        mealDao.observeDailyTotals(userId, fromDate, toDate).map { list -> list.map { it.toDomain() } }

    fun observeItem(itemId: String): Flow<MealItem?> =
        mealDao.observeItem(itemId).map { it?.toDomain() }

    suspend fun getItem(itemId: String): MealItem? = mealDao.getItem(itemId)?.toDomain()

    suspend fun getItemEntity(itemId: String): MealItemEntity? = mealDao.getItem(itemId)

    suspend fun getMeal(mealId: String): MealEntity? = mealDao.getMeal(mealId)

    suspend fun addItem(
        userId: String,
        date: String,
        mealType: MealType,
        food: Food,
        grams: Double,
        note: String? = null,
    ): String = db.withTransaction {
        val now = System.currentTimeMillis()
        val meal = ensureMeal(userId, date, mealType, now)
        val nutrition = food.nutritionFor(grams)
        val sortOrder = mealDao.maxSortOrder(meal.id) + 1
        val item = MealItemEntity(
            id = UUID.randomUUID().toString(),
            mealId = meal.id,
            foodId = food.id,
            foodName = food.name,
            portionGrams = grams,
            basePortionG = food.portionWeightG,
            calories = nutrition.calories,
            protein = nutrition.protein,
            carbs = nutrition.carbs,
            fat = nutrition.fat,
            fiber = nutrition.fiber,
            sodiumMg = nutrition.sodiumMg,
            source = MealItemEntity.SOURCE_USER,
            confidence = null,
            confirmed = 1,
            photoUri = null,
            sortOrder = sortOrder,
            createdAt = now,
            updatedAt = now,
            deletedAt = null,
        )
        mealDao.insertItem(item)
        if (!note.isNullOrBlank()) {
            mealDao.updateMeal(meal.copy(note = note, updatedAt = now))
        }
        item.id
    }

    /** Sửa khẩu phần và/hoặc đổi bữa. Trả về meal_id cũ để dọn bữa rỗng. */
    suspend fun updateItem(
        itemId: String,
        food: Food,
        grams: Double,
        mealType: MealType,
    ) = db.withTransaction {
        val now = System.currentTimeMillis()
        val item = mealDao.getItem(itemId) ?: return@withTransaction
        val currentMeal = mealDao.getMeal(item.mealId)
        val targetMeal = if (currentMeal?.mealType == mealType.key) {
            currentMeal
        } else {
            ensureMeal(currentMeal?.userId ?: UserRepository.LOCAL_USER_ID, currentMeal?.date ?: "", mealType, now)
        }
        val nutrition = food.nutritionFor(grams)
        mealDao.updateItem(
            item.copy(
                mealId = targetMeal.id,
                foodId = food.id,
                foodName = food.name,
                portionGrams = grams,
                basePortionG = food.portionWeightG,
                calories = nutrition.calories,
                protein = nutrition.protein,
                carbs = nutrition.carbs,
                fat = nutrition.fat,
                fiber = nutrition.fiber,
                sodiumMg = nutrition.sodiumMg,
                updatedAt = now,
            )
        )
        if (currentMeal != null && currentMeal.id != targetMeal.id) {
            removeMealIfEmpty(currentMeal.id, now)
        }
    }

    suspend fun deleteItem(itemId: String) = db.withTransaction {
        val now = System.currentTimeMillis()
        val item = mealDao.getItem(itemId) ?: return@withTransaction
        mealDao.softDeleteItem(itemId, now)
        removeMealIfEmpty(item.mealId, now)
    }

    suspend fun deleteMeal(mealId: String) = db.withTransaction {
        val now = System.currentTimeMillis()
        mealDao.softDeleteItemsOfMeal(mealId, now)
        mealDao.softDeleteMeal(mealId, now)
    }

    private suspend fun ensureMeal(
        userId: String,
        date: String,
        mealType: MealType,
        now: Long,
    ): MealEntity {
        val existing = mealDao.findMeal(userId, date, mealType.key)
        if (existing != null && existing.deletedAt == null) return existing
        if (existing != null) {
            // Bữa từng bị xoá mềm → hồi sinh, không tạo dòng trùng vì có UNIQUE.
            val revived = existing.copy(
                deletedAt = null,
                updatedAt = now,
                syncStatus = MealEntity.SYNC_PENDING,
            )
            mealDao.updateMeal(revived)
            return revived
        }
        val meal = MealEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            mealType = mealType.key,
            date = date,
            eatenAt = now,
            photoUri = null,
            note = null,
            createdAt = now,
            updatedAt = now,
            deletedAt = null,
            syncStatus = MealEntity.SYNC_PENDING,
        )
        mealDao.insertMeal(meal)
        return mealDao.findMeal(userId, date, mealType.key) ?: meal
    }

    private suspend fun removeMealIfEmpty(mealId: String, now: Long) {
        if (mealDao.countActiveItems(mealId) == 0) {
            mealDao.hardDeleteMeal(mealId)
        }
    }
}
