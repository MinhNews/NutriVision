package com.example.nutrivision.data.local.seed

import android.content.Context
import android.util.Log
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.nutrivision.domain.model.Food
import com.example.nutrivision.domain.text.guessFoodKind
import com.example.nutrivision.domain.text.toSearchKey
import org.json.JSONArray

/**
 * Nạp 145 món từ assets/foods_nutrition.json vào bảng foods đúng một lần,
 * ngay khi Room mở database lần đầu (RoomDatabase.Callback.onCreate).
 *
 * Chạy đồng bộ trong transaction của SQLite nên không có cửa sổ nào
 * mà UI đọc được bảng foods rỗng.
 */
class FoodSeeder(private val context: Context) {

    fun seedIfEmpty(db: SupportSQLiteDatabase) {
        if (countFoods(db) > 0) return

        val json = context.assets.open(ASSET_NAME).bufferedReader(Charsets.UTF_8).use { it.readText() }
        val array = JSONArray(json)
        val now = System.currentTimeMillis()

        db.beginTransaction()
        try {
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                val name = o.optString("name")
                val kind = name.guessFoodKind()
                val portionWeight = o.optInt("portion_weight_g", 100)

                db.execSQL(
                    """
                    INSERT OR IGNORE INTO foods (
                        id, name, category, kind, model_class, is_custom, owner_user_id,
                        portion_name, portion_weight_g,
                        calories, protein, carbs, fat, fiber, sodium_mg,
                        calories_per_100g, protein_per_100g, carbs_per_100g,
                        fat_per_100g, fiber_per_100g, sodium_mg_per_100g,
                        search_key, description, source, method, confidence,
                        created_at, updated_at, deleted_at
                    ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                    """.trimIndent(),
                    arrayOf(
                        o.optString("id"),
                        name,
                        o.optString("category", Food.CATEGORY_VIETNAMESE),
                        kind,
                        if (o.optBoolean("model_class", false)) 1 else 0,
                        0,
                        null,
                        o.optString("portion_name", "$portionWeight g"),
                        portionWeight,
                        o.optDouble("calories", 0.0),
                        o.optDouble("protein", 0.0),
                        o.optDouble("carbs", 0.0),
                        o.optDouble("fat", 0.0),
                        o.optDouble("fiber", 0.0),
                        o.optDouble("sodium_mg", 0.0),
                        o.optDouble("calories_per_100g", 0.0),
                        o.optDouble("protein_per_100g", 0.0),
                        o.optDouble("carbs_per_100g", 0.0),
                        o.optDouble("fat_per_100g", 0.0),
                        o.optDouble("fiber_per_100g", 0.0),
                        o.optDouble("sodium_mg_per_100g", 0.0),
                        name.toSearchKey(),
                        o.optString("description").takeIf { it.isNotBlank() },
                        o.optString("source").takeIf { it.isNotBlank() },
                        o.optString("method").takeIf { it.isNotBlank() },
                        o.optString("confidence").takeIf { it.isNotBlank() },
                        now,
                        now,
                        null,
                    ),
                )
            }
            db.setTransactionSuccessful()
            Log.i(TAG, "Đã nạp ${countFoods(db)} món từ $ASSET_NAME")
        } finally {
            db.endTransaction()
        }
    }

    private fun countFoods(db: SupportSQLiteDatabase): Int =
        db.query("SELECT COUNT(*) FROM foods").use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }

    companion object {
        private const val TAG = "FoodSeeder"
        private const val ASSET_NAME = "foods_nutrition.json"
    }
}
