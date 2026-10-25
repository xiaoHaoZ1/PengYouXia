package com.pengyouxia.app.designsystem

import com.pengyouxia.core.designsystem.avatarInitial
import org.junit.Assert.assertEquals
import org.junit.Test

/** avatarInitial：trim、空名兜底、按 Unicode 码点取首字符。 */
class AvatarInitialTest {

    @Test
    fun `中文名取首字`() {
        assertEquals("王", avatarInitial("王小明"))
    }

    @Test
    fun `英文名取首字母`() {
        assertEquals("A", avatarInitial("Alex"))
    }

    @Test
    fun `首尾空白先 trim`() {
        assertEquals("李", avatarInitial("  李雷 "))
    }

    @Test
    fun `空名与全空白返回问号`() {
        assertEquals("?", avatarInitial(""))
        assertEquals("?", avatarInitial("   "))
    }

    @Test
    fun `代理对表情不截半`() {
        val initial = avatarInitial("😀小明")
        assertEquals("😀", initial)
        assertEquals(2, initial.length) // 一个码点 = 两个 UTF-16 char
    }

    @Test
    fun `生僻字代理对不截半`() {
        assertEquals("𠀾", avatarInitial("𠀾哥"))
    }
}
