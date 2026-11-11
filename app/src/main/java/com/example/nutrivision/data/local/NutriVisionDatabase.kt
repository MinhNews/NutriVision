package com.example.nutrivision.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.nutrivision.data.local.dao.FoodDao
import com.example.nutrivision.data.local.dao.MealDao
import com.example.nutrivision.data.local.dao.UserDao
import com.example.nutrivision.data.local.entity.FoodEntity
import com.example.nutrivision.data.local.entity.MealEntity
import com.example.nutrivision.data.local.entity.MealItemEntity
import com.example.nutrivision.data.local.entity.UserEntity
import com.example.nutrivision.data.local.seed.FoodSeeder

@Database(
    entities = [
        UserEntity::class,
        FoodEntity::class,
        MealEntity::class,
        MealItemEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class NutriVisionDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun foodDao(): FoodDao
    abstract fun mealDao(): MealDao

    companion object {
        const val NAME = "nutrivision.db"

        fun build(context: Context): NutriVisionDatabase {
            val seeder = FoodSeeder(context.applicationContext)
            return Room.databaseBuilder(context.applicationContext, NutriVisionDatabase::class.java, NAME)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        seeder.seedIfEmpty(db)
                    }
                })
                .build()
        }
    }
}
