package com.example.nutrivision.data.repository

import com.example.nutrivision.data.local.dao.FoodDao
import com.example.nutrivision.data.local.entity.FoodEntity
import com.example.nutrivision.data.mapper.toDomain
import com.example.nutrivision.domain.model.Food
import com.example.nutrivision.domain.text.guessFoodKind
import com.example.nutrivision.domain.text.toSearchKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FoodRepository(private val foodDao: FoodDao) {

    fun observeAll(): Flow<List<Food>> =
        foodDao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeFood(id: String): Flow<Food?> =
        foodDao.observeById(id).map { it?.toDomain() }

    fun search(query: String, kind: String? = null): Flow<List<Food>> {
        val normalized = query.trim().lowercase()
        return foodDao.observeSearch(normalized, kind).map { list -> list.map { it.toDomain() } }
    }

    suspend fun getFood(id: String): Food? = foodDao.getById(id)?.toDomain()

    suspend fun count(): Int = foodDao.count()

    /**
     * Lưu món do AI ước lượng thành món riêng của người dùng.
     *
     * AI trả về dinh dưỡng cho *cả khẩu phần*, còn bảng foods lưu chuẩn 100 g,
     * nên quy đổi về 100 g để thanh kéo gram hoạt động giống mọi món khác.
     */
    suspend fun saveAiFood(
        userId: String,
        name: String,
        portionGrams: Double,
        calories: Double,
        protein: Double,
        carbs: Double,
        fat: Double,
        note: String?,
    ): Food {
        val now = System.currentTimeMillis()
        val portion = portionGrams.coerceIn(10.0, 2000.0)
        val per100 = 100.0 / portion
        val cleanName = name.trim().ifBlank { "Món AI ước lượng" }
        val entity = FoodEntity(
            id = "ai_${now}_${cleanName.toSearchKey().replace(' ', '_').take(28)}",
            name = cleanName,
            category = Food.CATEGORY_VIETNAMESE,
            kind = cleanName.guessFoodKind(),
            modelClass = 0,
            isCustom = 1,
            ownerUserId = userId,
            portionName = "${portion.toInt()} g (AI ước lượng)",
            portionWeightG = portion.toInt(),
            calories = calories,
            protein = protein,
            carbs = carbs,
            fat = fat,
            fiber = 0.0,
            sodiumMg = 0.0,
            caloriesPer100g = calories * per100,
            proteinPer100g = protein * per100,
            carbsPer100g = carbs * per100,
            fatPer100g = fat * per100,
            fiberPer100g = 0.0,
            sodiumMgPer100g = 0.0,
            searchKey = cleanName.toSearchKey(),
            description = note,
            source = "Gemini",
            method = "ai-estimate",
            confidence = "low",
            createdAt = now,
            updatedAt = now,
            deletedAt = null,
        )
        foodDao.upsert(entity)
        return entity.toDomain()
    }
}
