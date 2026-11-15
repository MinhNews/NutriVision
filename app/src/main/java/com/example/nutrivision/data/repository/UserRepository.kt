package com.example.nutrivision.data.repository

import com.example.nutrivision.data.local.dao.UserDao
import com.example.nutrivision.data.mapper.toDomain
import com.example.nutrivision.data.mapper.toEntity
import com.example.nutrivision.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepository(private val userDao: UserDao) {

    fun observeProfile(userId: String): Flow<UserProfile?> =
        userDao.observeUser(userId).map { it?.toDomain() }

    suspend fun getProfile(userId: String): UserProfile? =
        userDao.getUser(userId)?.toDomain()

    suspend fun saveProfile(profile: UserProfile) {
        userDao.upsert(profile.toEntity())
    }

    suspend fun hasProfile(userId: String): Boolean = userDao.getUser(userId) != null

    suspend fun clear(userId: String) = userDao.delete(userId)
}
