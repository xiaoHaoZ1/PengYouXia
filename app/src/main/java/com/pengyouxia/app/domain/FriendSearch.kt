package com.pengyouxia.app.domain

import com.pengyouxia.app.data.FriendListItem
import com.pengyouxia.app.domain.pinyin.PinyinIndex
import com.pengyouxia.app.domain.pinyin.PinyinProvider
import com.pengyouxia.core.model.MemoCategory

/**
 * 朋友搜索：名字/昵称、拼音（全拼包含或声母前缀）、怎么认识的、备忘内容。
 * 每个朋友最多一条结果；本批不搜往来。
 */
class FriendSearch(
    private val pinyin: PinyinProvider = PinyinIndex,
    private val ordering: FriendOrdering = FriendOrdering(pinyin),
) {

    /** 命中片段：[highlight] 是命中词在 [text] 内的下标区间（含前缀与「…」之后的位置）。 */
    data class SearchSnippet(val text: String, val highlight: IntRange)

    data class FriendSearchResult(val item: FriendListItem, val snippet: SearchSnippet?)

    /** [rawQuery] 空白时返回空列表（界面回退到正常列表）。 */
    fun search(items: List<FriendListItem>, rawQuery: String): List<FriendSearchResult> {
        val q = rawQuery.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        // 拼音匹配用的查询形：去空格、纯 ASCII 字母才启用
        val qLetters = q.filter { it != ' ' }
        val pinyinQuery = qLetters.takeIf {
            it.isNotEmpty() && it.all { c -> c in 'a'..'z' } && pinyin.available
        }
        return ordering.sort(items).mapNotNull { item ->
            match(item, q, pinyinQuery)?.let { FriendSearchResult(item, it.snippet) }
        }
    }

    /** 命中结果；[snippet] 为 null 表示名字/昵称/拼音命中（界面显示普通第二行）。 */
    private data class Hit(val snippet: SearchSnippet?)

    /** 返回该朋友是否命中；不命中返回 null。 */
    private fun match(item: FriendListItem, q: String, qLetters: String?): Hit? {
        val friend = item.friend
        // ① 名字 / 昵称文字包含
        if (friend.name.contains(q, ignoreCase = true) ||
            friend.nickname?.contains(q, ignoreCase = true) == true
        ) {
            return Hit(null)
        }
        // ② 拼音匹配（全拼包含 / 声母前缀），只对纯字母查询启用
        if (qLetters != null) {
            val forms = listOfNotNull(
                pinyin.of(friend.name),
                friend.nickname?.let { pinyin.of(it) },
            )
            if (forms.any { it.full.contains(qLetters) || it.initials.startsWith(qLetters) }) {
                return Hit(null)
            }
        }
        // ③ 怎么认识的
        friend.metNote?.let { note ->
            snippetOf(note, q, prefix = "怎么认识的：")?.let { return Hit(it) }
        }
        // ④ 备忘：按分类声明顺序、组内 createdAt 倒序，取第一条命中
        for (category in MemoCategory.entries) {
            item.memos
                .filter { it.category == category }
                .sortedByDescending { it.createdAt }
                .forEach { memo ->
                    snippetOf(memo.content, q, prefix = "备忘·${category.displayName}：")
                        ?.let { return Hit(it) }
                }
        }
        return null
    }

    /**
     * 在 [text] 中找 [q]（ignoreCase），截取命中处前后各约 12 个字符，
     * 前后被截断时加「…」；边界不切开代理对。
     */
    private fun snippetOf(text: String, q: String, prefix: String): SearchSnippet? {
        val hit = text.indexOf(q, ignoreCase = true)
        if (hit < 0) return null
        val hitEnd = hit + q.length
        var start = (hit - 12).coerceAtLeast(0)
        var end = (hitEnd + 12).coerceAtMost(text.length)
        // 边界不切开代理对：起点落在低代理位就向外扩一位，终点同理
        if (start > 0 && text[start].isLowSurrogate()) start -= 1
        if (end < text.length && text[end].isLowSurrogate()) end += 1
        val head = if (start > 0) "…" else ""
        val tail = if (end < text.length) "…" else ""
        val result = prefix + head + text.substring(start, end) + tail
        val highlightStart = prefix.length + head.length + (hit - start)
        return SearchSnippet(result, highlightStart until highlightStart + q.length)
    }
}
