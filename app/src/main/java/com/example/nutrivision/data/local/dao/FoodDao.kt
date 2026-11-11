package com.example.nutrivision.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nutrivision.data.local.entity.FoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {

    @Query("SELECT * FROM foods WHERE deleted_at IS NULL ORDER BY name")
    fun observeAll(): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE id = :id AND deleted_at IS NULL LIMIT 1")
    fun observeById(id: String): Flow<FoodEntity?>

    @Query("SELECT * FROM foods WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): FoodEntity?

    /**
     * Tìm theo tên đã bỏ dấu. `kind` = null nghĩa là tìm tất cả (món + đồ uống + topping).
     * Món model nhận diện được xếp trước để gợi ý hữu ích hơn.
     */
    @Query(
        """
        SELECT * FROM foods
        WHERE deleted_at IS NULL
          AND search_key LIKE '%' || :query || '%'
          AND (:kind IS NULL OR kind = :kind)
        ORDER BY model_class DESC, name ASC
        LIMIT 60
        """
    )
    fun observeSearch(query: String, kind: String?): Flow<List<FoodEntity>>

    @Query("SELECT COUNT(*) FROM foods WHERE deleted_at IS NULL")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM foods")
    suspend fun countIncludingDeleted(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(foods: List<FoodEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(food: FoodEntity)
}
