package com.example.nutrivision.data.repository

import com.example.nutrivision.data.local.dao.FoodDao
import com.example.nutrivision.data.mapper.toDomain
import com.example.nutrivision.domain.model.Food
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
}
