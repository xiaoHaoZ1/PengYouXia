package com.pengyouxia.app.domain

import com.pengyouxia.app.data.FriendListItem
import com.pengyouxia.app.domain.pinyin.PinyinIndex
import com.pengyouxia.app.domain.pinyin.PinyinProvider
import java.text.Collator
import java.util.Locale

/** 朋友排序与拼音字母分组。 */
class FriendOrdering(private val pinyin: PinyinProvider = PinyinIndex) {

    private val collator: Collator = Collator.getInstance(Locale.CHINA)

    /** 按名字中文排序（Collator），平局按 id 确定化。 */
    fun sort(items: List<FriendListItem>): List<FriendListItem> =
        items.sortedWith(nameThenId({ it.friend.name }, { it.friend.id }, collator = collator))

    /** 分组标题 + 成员；[key] 为 null 表示不分组（拼音不可用）。 */
    data class FriendGroup(val key: String?, val items: List<FriendListItem>)

    /**
     * 分组键：名字拼音全拼首字符，a–z → 大写字母，其余（数字、表情等）归 "#"。
     */
    fun groupKey(name: String): String {
        val first = pinyin.of(name).full.firstOrNull() ?: return "#"
        return if (first in 'a'..'z') first.uppercaseChar().toString() else "#"
    }

    /**
     * 按分组键分组：A–Z 字母序，"#" 最后，组内 [sort]。
     * 拼音不可用（API <29）时返回单一无标题分组；空列表返回空。
     */
    fun group(items: List<FriendListItem>): List<FriendGroup> {
        if (items.isEmpty()) return emptyList()
        if (!pinyin.available) return listOf(FriendGroup(null, sort(items)))
        return items.groupBy { groupKey(it.friend.name) }
            .toSortedMap(compareBy { if (it == "#") "{" else it })
            .map { (key, group) -> FriendGroup(key, sort(group)) }
    }
}

private fun <T> nameThenId(
    nameOf: (T) -> String,
    idOf: (T) -> String,
    collator: Collator,
): Comparator<T> = Comparator { a, b ->
    val byName = collator.compare(nameOf(a), nameOf(b))
    if (byName != 0) byName else idOf(a).compareTo(idOf(b))
}
