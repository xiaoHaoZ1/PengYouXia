package com.pengyouxia.app.domain

import com.pengyouxia.app.data.FriendListItem
import com.pengyouxia.app.domain.pinyin.PinyinIndex
import com.pengyouxia.app.domain.pinyin.PinyinProvider
import com.pengyouxia.core.model.Friend
import com.pengyouxia.core.model.Memo
import com.pengyouxia.core.model.MemoCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** 朋友搜索：文字子串、拼音、怎么认识的与备忘片段截取。 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FriendSearchTest {

    private val search = FriendSearch()

    private fun friend(
        id: String, name: String, nickname: String? = null, metNote: String? = null,
    ) = Friend(id, name, nickname, metNote, null, 0L, 0L)

    private fun memo(
        id: String, friendId: String, category: MemoCategory,
        content: String, createdAt: Long = 0L,
    ) = Memo(id, friendId, category, content, false, createdAt, createdAt)

    private fun item(friend: Friend, memos: List<Memo> = emptyList()) =
        FriendListItem(friend, memos)

    // ---------- 文字与拼音 ----------

    @Test
    fun `中文子串命中名字`() {
        val r = search.search(listOf(item(friend("1", "王小明"))), "小明")
        assertEquals("王小明", r.single().item.friend.name)
        assertNull(r.single().snippet)
    }

    @Test
    fun `昵称命中`() {
        val r = search.search(listOf(item(friend("1", "王小明", nickname = "明明"))), "明")
        assertEquals(1, r.size)
        assertNull(r.single().snippet)
    }

    @Test
    fun `声母缩写与全拼命中`() {
        val items = listOf(item(friend("1", "老王")))
        assertEquals(1, search.search(items, "lw").size)
        assertEquals(1, search.search(items, "laowang").size)
    }

    @Test
    fun `拼音查询大小写不敏感`() {
        val items = listOf(item(friend("1", "老王")))
        assertEquals(1, search.search(items, "LW").size)
    }

    @Test
    fun `英文名命中`() {
        val items = listOf(item(friend("1", "Alex")))
        assertEquals(1, search.search(items, "alex").size)
        assertEquals(1, search.search(items, "ALE").size)
    }

    @Test
    fun `空白查询返回空`() {
        val items = listOf(item(friend("1", "王小明")))
        assertTrue(search.search(items, "   ").isEmpty())
        assertTrue(search.search(items, "").isEmpty())
    }

    @Test
    fun `拼音不可用时纯字母查询不命中中文名`() {
        val no = FriendSearch(pinyin = NoPinyin, ordering = FriendOrdering(NoPinyin))
        assertTrue(no.search(listOf(item(friend("1", "老王"))), "lw").isEmpty())
    }

    private object NoPinyin : PinyinProvider {
        override val available = false
        override fun of(text: String) = PinyinIndex.Form(text.lowercase(), "")
    }

    // ---------- 片段截取 ----------

    @Test
    fun `怎么认识的命中带前缀片段`() {
        val r = search.search(
            listOf(item(friend("1", "王大", metNote = "大学篮球社认识的"))), "篮球"
        ).single()
        val snip = r.snippet!!
        assertTrue(snip.text.startsWith("怎么认识的："))
        assertEquals("篮球", snip.text.substring(snip.highlight))
    }

    @Test
    fun `备忘命中带分类前缀`() {
        val it = item(
            friend("1", "王大"),
            listOf(memo("m1", "1", MemoCategory.LIKES, "喜欢打羽毛球")),
        )
        val snip = search.search(listOf(it), "羽毛").single().snippet!!
        assertTrue(snip.text.startsWith("备忘·喜好："))
        assertEquals("羽毛", snip.text.substring(snip.highlight))
    }

    @Test
    fun `长内容前后截断加省略号且高亮正确`() {
        val content = "前".repeat(40) + "目标词" + "后".repeat(40)
        val it = item(friend("1", "王大"), listOf(memo("m1", "1", MemoCategory.OTHER, content)))
        val snip = search.search(listOf(it), "目标词").single().snippet!!
        assertTrue(snip.text.startsWith("备忘·其他：…"))
        assertTrue(snip.text.endsWith("…"))
        assertEquals("目标词", snip.text.substring(snip.highlight))
    }

    @Test
    fun `短内容不加省略号`() {
        val it = item(friend("1", "王大"), listOf(memo("m1", "1", MemoCategory.FAMILY, "有只猫")))
        val snip = search.search(listOf(it), "猫").single().snippet!!
        assertEquals("备忘·家人：有只猫", snip.text)
    }

    @Test
    fun `截断边界不切开代理对`() {
        // 目标在 index 23，窗口起点 11 恰落在 😀（index 10-11）的低代理位，
        // 实现应向外扩一位完整包含表情；若切半，段首会是孤立低代理字符
        val content = "字".repeat(10) + "😀" + "字".repeat(11) + "目标" + "尾".repeat(5)
        val it = item(friend("1", "王大"), listOf(memo("m1", "1", MemoCategory.RECENT, content)))
        val snip = search.search(listOf(it), "目标").single().snippet!!
        assertEquals("目标", snip.text.substring(snip.highlight))
        assertTrue(snip.text.removePrefix("备忘·近况：…").startsWith("😀"))
    }

    @Test
    fun `备忘按分类顺序取第一条`() {
        // 家人的备忘排在近况之前（声明顺序），即使近况更新
        val it = item(
            friend("1", "王大"),
            listOf(
                memo("m1", "1", MemoCategory.RECENT, "有只猫", createdAt = 99L),
                memo("m2", "1", MemoCategory.FAMILY, "家里养猫", createdAt = 1L),
            ),
        )
        val snip = search.search(listOf(it), "猫").single().snippet!!
        assertTrue(snip.text.startsWith("备忘·家人："))
    }

    @Test
    fun `结果按名字排序每人一条`() {
        val items = listOf(
            item(friend("b", "李雷", metNote = "社团")),
            item(friend("a", "陈晓", metNote = "社团")),
        )
        val r = search.search(items, "社团")
        assertEquals(listOf("陈晓", "李雷"), r.map { it.item.friend.name })
    }
}
