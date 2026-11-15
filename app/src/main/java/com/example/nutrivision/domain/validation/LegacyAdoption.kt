package com.example.nutrivision.domain.validation

/**
 * Quyet dinh co nhan du lieu cua tuan 3 (user_id = "local") vao tai khoan that hay khong.
 *
 * Chi nhan khi email cua ho so cu trung voi email dang nhap: tren cung mot may co the co
 * nhieu nguoi dung, khong duoc de tai khoan vua tao thay du lieu cua nguoi khac.
 */
object LegacyAdoption {

    fun emailMatches(legacyEmail: String?, currentEmail: String): Boolean {
        val legacy = legacyEmail?.trim().orEmpty()
        val current = currentEmail.trim()
        if (legacy.isEmpty() || current.isEmpty()) return false
        return legacy.equals(current, ignoreCase = true)
    }
}
