package com.example.nutrivision.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nutrivision.data.local.entity.UserEntity
import com.example.nutrivision.data.local.relation.DataFingerprint
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE user_id = :userId LIMIT 1")
    fun observeUser(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE user_id = :userId LIMIT 1")
    suspend fun getUser(userId: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    @Query(
        """
        SELECT COUNT(*) AS row_count, COALESCE(MAX(updated_at), 0) AS last_update
        FROM users WHERE user_id = :userId
        """
    )
    fun observeFingerprint(userId: String): Flow<DataFingerprint>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)

    @Query("DELETE FROM users WHERE user_id = :userId")
    suspend fun delete(userId: String)
}
