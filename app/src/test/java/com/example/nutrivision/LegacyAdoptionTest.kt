package com.example.nutrivision

import com.example.nutrivision.domain.validation.LegacyAdoption
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Dữ liệu của tuần 3 nằm dưới user_id = "local". Chỉ được nhận vào tài khoản mới
 * khi email trùng — nếu không, người thứ hai đăng ký trên cùng máy sẽ thấy dữ liệu của người thứ nhất.
 */
class LegacyAdoptionTest {

    @Test
    fun `email trung thi nhan du lieu cu`() {
        assertTrue(
            LegacyAdoption.emailMatches(
                legacyEmail = "kiemthu.tuan4@nutrivision-test.vn",
                currentEmail = "kiemthu.tuan4@nutrivision-test.vn",
            )
        )
    }

    @Test
    fun `so sanh khong phan biet hoa thuong va khoang trang`() {
        assertTrue(
            LegacyAdoption.emailMatches(
                legacyEmail = "  KiemThu.Tuan4@NutriVision-Test.vn ",
                currentEmail = "kiemthu.tuan4@nutrivision-test.vn",
            )
        )
    }

    @Test
    fun `email khac thi khong nhan`() {
        assertFalse(
            LegacyAdoption.emailMatches(
                legacyEmail = "kiemthu.tuan4@nutrivision-test.vn",
                currentEmail = "kiemthu2@nutrivision-test.vn",
            )
        )
    }

    @Test
    fun `chua co ho so cu thi khong nhan`() {
        assertFalse(LegacyAdoption.emailMatches(null, "kiemthu2@nutrivision-test.vn"))
        assertFalse(LegacyAdoption.emailMatches("   ", "kiemthu2@nutrivision-test.vn"))
    }

    @Test
    fun `email dang nhap rong thi khong nhan`() {
        assertFalse(LegacyAdoption.emailMatches("kiemthu.tuan4@nutrivision-test.vn", ""))
    }
}
