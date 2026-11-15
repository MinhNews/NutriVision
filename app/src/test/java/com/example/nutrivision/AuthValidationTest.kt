package com.example.nutrivision

import com.example.nutrivision.domain.validation.AuthIssue
import com.example.nutrivision.domain.validation.AuthValidation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Kiểm tra phần chặn lỗi trước khi gọi Firebase.
 * Đây là lớp thuần Kotlin nên chạy được trên JVM, không cần emulator.
 */
class AuthValidationTest {

    private fun check(
        email: String = "minhanh@nutrivision-test.vn",
        password: String = "123456",
        name: String = "Minh Anh",
        confirm: String = "123456",
        isRegister: Boolean = true,
        terms: Boolean = true,
    ) = AuthValidation.check(
        email = email,
        password = password,
        displayName = name,
        confirmPassword = confirm,
        isRegister = isRegister,
        termsAccepted = terms,
    )

    @Test
    fun `dang ky hop le thi khong co loi`() {
        assertNull(check())
    }

    @Test
    fun `email thieu a cung bi chan`() {
        assertEquals(AuthIssue.EMAIL_INVALID, check(email = "minhanh.nutrivision.vn"))
    }

    @Test
    fun `email thieu ten mien bi chan`() {
        assertEquals(AuthIssue.EMAIL_INVALID, check(email = "minhanh@nutrivision"))
    }

    @Test
    fun `email co khoang trang bi chan`() {
        assertEquals(AuthIssue.EMAIL_INVALID, check(email = "minh anh@nutrivision.vn"))
    }

    @Test
    fun `mat khau ngan hon 6 ky tu bi chan`() {
        assertEquals(AuthIssue.PASSWORD_TOO_SHORT, check(password = "12345", confirm = "12345"))
    }

    @Test
    fun `mat khau dung 6 ky tu la du`() {
        assertNull(check(password = "123456", confirm = "123456"))
    }

    @Test
    fun `khong bat buoc chu hoa hay chu so`() {
        assertNull(check(password = "abcdef", confirm = "abcdef"))
    }

    @Test
    fun `hai lan mat khau khac nhau bi chan`() {
        assertEquals(AuthIssue.PASSWORD_MISMATCH, check(password = "123456", confirm = "123457"))
    }

    @Test
    fun `thieu ten hien thi bi chan`() {
        assertEquals(AuthIssue.NAME_MISSING, check(name = " "))
    }

    @Test
    fun `chua dong y dieu khoan bi chan`() {
        assertEquals(AuthIssue.TERMS_NOT_ACCEPTED, check(terms = false))
    }

    @Test
    fun `dang nhap khong can ten hien thi hay dieu khoan`() {
        assertNull(check(name = "", isRegister = false, terms = false))
    }

    @Test
    fun `dang nhap van kiem tra mat khau`() {
        assertEquals(AuthIssue.PASSWORD_TOO_SHORT, check(password = "abc", isRegister = false))
    }

    @Test
    fun `email hop le nhan dang dung`() {
        assertTrue(AuthValidation.looksLikeEmail("a@b.vn"))
        assertTrue(AuthValidation.looksLikeEmail("kiemthu.tuan4@nutrivision-test.vn"))
        assertFalse(AuthValidation.looksLikeEmail("a@b"))
        assertFalse(AuthValidation.looksLikeEmail("@b.vn"))
        assertFalse(AuthValidation.looksLikeEmail("a@@b.vn"))
        assertFalse(AuthValidation.looksLikeEmail("a@b."))
        assertFalse(AuthValidation.looksLikeEmail(""))
    }
}
