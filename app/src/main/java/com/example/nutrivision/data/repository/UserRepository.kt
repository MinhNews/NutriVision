package com.example.nutrivision.data.repository

import com.example.nutrivision.data.local.dao.UserDao
import com.example.nutrivision.data.mapper.toDomain
import com.example.nutrivision.data.mapper.toEntity
import com.example.nutrivision.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepository(private val userDao: UserDao) {

    fun observeProfile(userId: String = LOCAL_USER_ID): Flow<UserProfile?> =
        userDao.observeUser(userId).map { it?.toDomain() }

    suspend fun getProfile(userId: String = LOCAL_USER_ID): UserProfile? =
        userDao.getUser(userId)?.toDomain()

    suspend fun saveProfile(profile: UserProfile) {
        userDao.upsert(profile.toEntity())
    }

    suspend fun hasProfile(): Boolean = userDao.count() > 0

    suspend fun clear(userId: String = LOCAL_USER_ID) = userDao.delete(userId)

    companion object {
        /** Tuần 3 chưa có Firebase Auth → một người dùng cục bộ. */
        const val LOCAL_USER_ID = "local"
    }
}
