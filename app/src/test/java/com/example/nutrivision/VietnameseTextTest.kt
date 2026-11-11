package com.example.nutrivision

import com.example.nutrivision.domain.text.guessFoodKind
import com.example.nutrivision.domain.text.toSearchKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tìm kiếm không dấu là yêu cầu bắt buộc của tuần 3 nên cần test riêng. */
class VietnameseTextTest {

    @Test
    fun `Bo dau tieng Viet`() {
        assertEquals("pho bo tai nam", "Phở bò tái nạm".toSearchKey())
        assertEquals("bun bo hue", "Bún bò Huế".toSearchKey())
        assertEquals("com tam suon nuong", "Cơm tấm sườn nướng".toSearchKey())
        assertEquals("dau hu chien", "Đậu hũ chiên".toSearchKey())
    }

    @Test
    fun `Bo ky tu dac biet va khoang trang thua`() {
        assertEquals("banh mi thit", "Bánh mì   thịt!!!".toSearchKey())
        assertEquals("tra sua tran chau", "Trà sữa (trân châu)".toSearchKey())
    }

    @Test
    fun `Chuoi rong van an toan`() {
        assertEquals("", "   ".toSearchKey())
        assertEquals("", "".toSearchKey())
    }

    @Test
    fun `Doan do uong tu ten`() {
        assertEquals("drink", "Trà sữa trân châu".guessFoodKind())
        assertEquals("drink", "Cà phê sữa đá".guessFoodKind())
        assertEquals("dish", "Phở bò tái".guessFoodKind())
    }

    @Test
    fun `Search key khong con dau cach dau cuoi`() {
        val key = "  Bún chả Hà Nội  ".toSearchKey()
        assertTrue(key == "bun cha ha noi")
    }
}
