package com.example.nutrivision.notifications

import com.example.nutrivision.data.prefs.ReminderSlotSetting
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Kiểm tra phần "thông minh" của lịch nhắc mà không cần máy ảo:
 * chọn đúng bữa gần nhất, qua giờ thì sang ngày mai, bỏ qua bữa đã tắt.
 */
class ReminderPlannerTest {

    private fun at(hour: Int, minute: Int): Calendar =
        Calendar.getInstance().apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.NOVEMBER)
            set(Calendar.DAY_OF_MONTH, 15)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

    private fun slots(
        breakfast: Triple<Boolean, Int, Int> = Triple(true, 7, 0),
        lunch: Triple<Boolean, Int, Int> = Triple(true, 11, 30),
        dinner: Triple<Boolean, Int, Int> = Triple(true, 18, 30),
    ) = listOf(
        ReminderSlotSetting(MealSlot.BREAKFAST, breakfast.first, breakfast.second, breakfast.third),
        ReminderSlotSetting(MealSlot.LUNCH, lunch.first, lunch.second, lunch.third),
        ReminderSlotSetting(MealSlot.DINNER, dinner.first, dinner.second, dinner.third),
    )

    @Test
    fun `buoi sang som thi nhac bua sang hom nay`() {
        val next = ReminderPlanner.next(at(6, 0), slots())
        assertEquals(MealSlot.BREAKFAST, next?.slot)
        assertEquals(7, next?.at?.get(Calendar.HOUR_OF_DAY))
        assertEquals(15, next?.at?.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `qua bua sang thi nhac bua trua`() {
        val next = ReminderPlanner.next(at(8, 0), slots())
        assertEquals(MealSlot.LUNCH, next?.slot)
        assertEquals(11, next?.at?.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, next?.at?.get(Calendar.MINUTE))
    }

    @Test
    fun `qua bua toi thi hen bua sang ngay mai`() {
        val next = ReminderPlanner.next(at(19, 0), slots())
        assertEquals(MealSlot.BREAKFAST, next?.slot)
        assertEquals(16, next?.at?.get(Calendar.DAY_OF_MONTH))
        assertEquals(7, next?.at?.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun `dung gio nhac thi khong hen lai bua vua chay`() {
        val next = ReminderPlanner.next(at(7, 0), slots())
        assertEquals(MealSlot.LUNCH, next?.slot)
        assertEquals(15, next?.at?.get(Calendar.DAY_OF_MONTH))
        assertEquals(11, next?.at?.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun `tat bua sang thi bo qua bua do`() {
        val next = ReminderPlanner.next(at(6, 0), slots(breakfast = Triple(false, 7, 0)))
        assertEquals(MealSlot.LUNCH, next?.slot)
    }

    @Test
    fun `gio tuy chinh duoc dung thay vi mac dinh`() {
        val next = ReminderPlanner.next(at(20, 0), slots(breakfast = Triple(true, 21, 15)))
        assertEquals(MealSlot.BREAKFAST, next?.slot)
        assertEquals(21, next?.at?.get(Calendar.HOUR_OF_DAY))
        assertEquals(15, next?.at?.get(Calendar.MINUTE))
        assertEquals(15, next?.at?.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `tat het cac bua thi khong hen gi`() {
        val next = ReminderPlanner.next(
            at(10, 0),
            slots(
                breakfast = Triple(false, 7, 0),
                lunch = Triple(false, 11, 30),
                dinner = Triple(false, 18, 30),
            ),
        )
        assertNull(next)
    }
}
