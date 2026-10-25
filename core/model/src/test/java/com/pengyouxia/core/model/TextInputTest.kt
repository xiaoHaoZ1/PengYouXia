package com.pengyouxia.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** 输入规范化与码点长度上限。 */
class TextInputTest {

    @Test
    fun `必填文本 trim`() {
        assertEquals("王小明", normalizeRequired("  王小明  "))
    }

    @Test
    fun `全角空格也 trim`() {
        assertEquals("王小明", normalizeRequired("　王小明　"))
    }

    @Test
    fun `可选文本 trim 后为空归 null`() {
        assertNull(normalizeOptional("   "))
        assertNull(normalizeOptional("　　"))
        assertNull(normalizeOptional(null))
        assertEquals("小明", normalizeOptional(" 小明 "))
    }

    @Test
    fun `码点长度按码点计数`() {
        assertEquals(30, "汉".repeat(30).codePointLength())
        // 30 个表情 = 60 个 UTF-16 char，仍计 30
        assertEquals(30, "😀".repeat(30).codePointLength())
        // 生僻字（代理对）计 1
        assertEquals(2, "𠀾哥".codePointLength())
    }

    @Test
    fun `超限判断`() {
        assertFalse(exceedsLimit("汉".repeat(30), InputLimits.NAME))
        assertTrue(exceedsLimit("汉".repeat(31), InputLimits.NAME))
        assertFalse(exceedsLimit("😀".repeat(30), InputLimits.NAME))
        assertTrue(exceedsLimit("😀".repeat(31), InputLimits.NAME))
    }
}
