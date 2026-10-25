package com.pengyouxia.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/** Birthday 构造校验边界。 */
class BirthdayTest {

    private fun solar(month: Int = 6, day: Int = 15) =
        Birthday(CalendarType.SOLAR, null, month, day)

    private fun lunar(month: Int = 6, day: Int = 15, leap: Boolean = false) =
        Birthday(CalendarType.LUNAR, null, month, day, leap)

    @Test
    fun `月份边界`() {
        assertThrows(IllegalArgumentException::class.java) { solar(month = 0) }
        solar(month = 1)
        solar(month = 12)
        assertThrows(IllegalArgumentException::class.java) { solar(month = 13) }
    }

    @Test
    fun `公历日 1 到 31`() {
        solar(day = 1)
        solar(day = 31)
        assertThrows(IllegalArgumentException::class.java) { solar(day = 0) }
        assertThrows(IllegalArgumentException::class.java) { solar(day = 32) }
    }

    @Test
    fun `农历日 1 到 30`() {
        lunar(day = 1)
        lunar(day = 30)
        assertThrows(IllegalArgumentException::class.java) { lunar(day = 31) }
    }

    @Test
    fun `闰月只允许农历`() {
        lunar(leap = true)
        assertThrows(IllegalArgumentException::class.java) {
            Birthday(CalendarType.SOLAR, null, 6, 15, leapMonth = true)
        }
    }

    @Test
    fun `年份可为空不校验范围`() {
        assertEquals(null, solar().year)
        Birthday(CalendarType.SOLAR, 1990, 6, 15)
        Birthday(CalendarType.SOLAR, -1, 6, 15)
    }
}
