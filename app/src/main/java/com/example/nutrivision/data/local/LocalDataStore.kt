package com.example.nutrivision.data.local

import androidx.room.withTransaction
import com.example.nutrivision.domain.validation.LegacyAdoption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Xu ly du lieu con lai tu tuan 3 (khi chua co tai khoan that, moi thu dung user_id = "local").
 *
 * - [adoptLegacyData]: lan dau dang nhap/ dang ky, chuyen users + meals cua "local" sang UID that
 *   de nguoi dung khong mat nhat ky da ghi. meal_items di theo meals qua meal_id (CASCADE).
 * - [wipe]: xoa sach du lieu cua mot tai khoan tren may (dung khi xoa tai khoan).
 */
class LocalDataStore(private val db: NutriVisionDatabase) {

    suspend fun adoptLegacyData(userId: String, email: String): Int = withContext(Dispatchers.IO) {
        if (userId == LEGACY_USER_ID) return@withContext 0
        runCatching {
            db.withTransaction {
                val sql = db.openHelper.writableDatabase
                // Chi nhan du lieu cu khi dung email cua ho so cu, tranh truong hop tai khoan moi
                // vua dang ky lai thay ho so cua nguoi dung truoc tren may.
                val legacyEmail = sql.query(
                    "SELECT email FROM users WHERE user_id = ? LIMIT 1",
                    arrayOf(LEGACY_USER_ID),
                ).use { if (it.moveToFirst()) it.getString(0) else null }
                if (!LegacyAdoption.emailMatches(legacyEmail, email)) {
                    return@withTransaction 0
                }
                if (countMeals(sql, LEGACY_USER_ID) == 0 && countUser(sql, LEGACY_USER_ID) == 0) {
                    return@withTransaction 0
                }
                // Tai khoan moi da co du lieu rieng thi khong tron de tranh vi pham UNIQUE(meals).
                if (countUser(sql, userId) > 0 || countMeals(sql, userId) > 0) {
                    return@withTransaction 0
                }
                // users.user_id la cha cua meals.user_id: doi ca hai trong cung transaction nen phai
                // hoan kiem tra khoa ngoai den luc COMMIT, neu khong se vo ngay o lenh UPDATE dau.
                sql.execSQL("PRAGMA defer_foreign_keys = ON")
                val movedUsers = sql.compileStatement(
                    "UPDATE users SET user_id = ? WHERE user_id = ?"
                ).use { st ->
                    st.bindString(1, userId)
                    st.bindString(2, LEGACY_USER_ID)
                    st.executeUpdateDelete()
                }
                val movedMeals = sql.compileStatement(
                    "UPDATE meals SET user_id = ? WHERE user_id = ?"
                ).use { st ->
                    st.bindString(1, userId)
                    st.bindString(2, LEGACY_USER_ID)
                    st.executeUpdateDelete()
                }
                movedUsers + movedMeals
            }
        }.getOrDefault(0)
    }

    suspend fun wipe(userId: String) = withContext(Dispatchers.IO) {
        db.withTransaction {
            val sql = db.openHelper.writableDatabase
            sql.execSQL("DELETE FROM meals WHERE user_id = ?", arrayOf(userId))
            sql.execSQL("DELETE FROM users WHERE user_id = ?", arrayOf(userId))
        }
    }

    private fun countUser(sql: androidx.sqlite.db.SupportSQLiteDatabase, userId: String): Int =
        sql.query("SELECT COUNT(*) FROM users WHERE user_id = ?", arrayOf(userId)).use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }

    private fun countMeals(sql: androidx.sqlite.db.SupportSQLiteDatabase, userId: String): Int =
        sql.query("SELECT COUNT(*) FROM meals WHERE user_id = ?", arrayOf(userId)).use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }

    companion object {
        const val LEGACY_USER_ID = "local"
    }
}
