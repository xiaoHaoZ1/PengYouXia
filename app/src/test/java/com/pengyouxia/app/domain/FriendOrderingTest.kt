package com.pengyouxia.app.domain

import com.pengyouxia.app.data.FriendListItem
import com.pengyouxia.app.domain.pinyin.PinyinIndex
import com.pengyouxia.app.domain.pinyin.PinyinProvider
import com.pengyouxia.core.model.Friend
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** 拼音分组与组内排序；拼音不可用时退化为单一无标题分组。 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FriendOrderingTest {

    private val ordering = FriendOrdering()

    private fun item(id: String, name: String) = FriendListItem(
        Friend(id, name, null, null, null, 0L, 0L), emptyList()
    )

    /** 拼音不可用的假提供者。 */
    private object NoPinyin : PinyinProvider {
        override val available = false
        override fun of(text: String) = PinyinIndex.Form(text.lowercase(), "")
    }

    @Test
    fun `按拼音字母分组且井号在最后`() {
        val items = listOf(
            item("1", "王小明"), item("2", "李雷"), item("3", "韩梅梅"),
            item("4", "陈晓"), item("5", "Alex"), item("6", "😀小明"),
            item("7", "3班老同学"), item("8", "小王"),
        )
        val groups = ordering.group(items)
        assertEquals(listOf("A", "C", "H", "L", "W", "X", "#"), groups.map { it.key })
        assertEquals(listOf("Alex"), groups[0].items.map { it.friend.name })
        assertEquals(listOf("陈晓"), groups[1].items.map { it.friend.name })
        assertEquals(listOf("韩梅梅"), groups[2].items.map { it.friend.name })
        assertEquals(listOf("李雷"), groups[3].items.map { it.friend.name })
        // 王小明→W（王 wang）、小王→X（小 xiao）；# 组含表情与数字开头
        assertEquals(listOf("王小明"), groups[4].items.map { it.friend.name })
        assertEquals(listOf("小王"), groups[5].items.map { it.friend.name })
        assertEquals(
            setOf("😀小明", "3班老同学"),
            groups[6].items.map { it.friend.name }.toSet()
        )
    }

    @Test
    fun `同组内按名字排序且平局按 id`() {
        // 两位「小王」：名字相同，按 id 决定次序
        val items = listOf(item("b", "小王"), item("a", "小王"))
        val sorted = ordering.sort(items)
        assertEquals(listOf("a", "b"), sorted.map { it.friend.id })
    }

    @Test
    fun `拼音不可用时单一无标题分组`() {
        val no = FriendOrdering(NoPinyin)
        val groups = no.group(listOf(item("1", "王小明"), item("2", "Alex")))
        assertEquals(1, groups.size)
        assertNull(groups.single().key)
        assertEquals(2, groups.single().items.size)
    }

    @Test
    fun `空列表返回空`() {
        assertTrue(ordering.group(emptyList()).isEmpty())
    }
}
