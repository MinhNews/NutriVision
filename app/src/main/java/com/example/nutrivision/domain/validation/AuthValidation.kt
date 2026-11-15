package com.example.nutrivision.domain.validation

/** Loi nhap lieu cua form dang nhap / dang ky, tach khoi UI de test duoc bang unit test. */
enum class AuthIssue {
    EMAIL_INVALID,
    PASSWORD_TOO_SHORT,
    NAME_MISSING,
    PASSWORD_MISMATCH,
    TERMS_NOT_ACCEPTED,
}

/**
 * Kiem tra du lieu truoc khi goi Firebase. Firebase van la lop bao ve cuoi cung
 * (vi du mat khau qua yeu, email da ton tai), nhung chan som giup nguoi dung biet ngay.
 */
object AuthValidation {

    /** Firebase yeu cau toi thieu 6 ky tu, khong bat buoc chu hoa hay so. */
    const val MIN_PASSWORD_LENGTH = 6

    const val MIN_NAME_LENGTH = 2

    fun check(
        email: String,
        password: String,
        displayName: String,
        confirmPassword: String,
        isRegister: Boolean,
        termsAccepted: Boolean,
    ): AuthIssue? {
        val cleanEmail = email.trim()
        return when {
            !looksLikeEmail(cleanEmail) -> AuthIssue.EMAIL_INVALID
            password.length < MIN_PASSWORD_LENGTH -> AuthIssue.PASSWORD_TOO_SHORT
            isRegister && displayName.trim().length < MIN_NAME_LENGTH -> AuthIssue.NAME_MISSING
            isRegister && password != confirmPassword -> AuthIssue.PASSWORD_MISMATCH
            isRegister && !termsAccepted -> AuthIssue.TERMS_NOT_ACCEPTED
            else -> null
        }
    }

    /**
     * Kiem tra hinh dang email o muc "co the gui len Firebase duoc":
     * co dung mot dau @, co phan ten, phan ten mien co dau cham, khong co khoang trang.
     */
    fun looksLikeEmail(value: String): Boolean {
        if (value.isEmpty() || value.any { it.isWhitespace() }) return false
        val at = value.indexOf('@')
        if (at <= 0 || at != value.lastIndexOf('@')) return false
        val domain = value.substring(at + 1)
        val dot = domain.lastIndexOf('.')
        return dot > 0 && dot < domain.length - 1
    }
}
