package com.pengyouxia.app.data

import org.junit.Assert.assertTrue
import org.junit.Test

class SystemTimeSourceTest {

    @Test
    fun `连续调用严格递增`() {
        val time = SystemTimeSource()
        var prev = time.now()
        repeat(100) {
            val next = time.now()
            assertTrue("$next <= $prev", next > prev)
            prev = next
        }
    }
}
