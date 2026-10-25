package com.pengyouxia.app.data

import com.pengyouxia.core.model.Friend
import com.pengyouxia.core.model.Memo

/** 新增/编辑朋友的输入（M1 不含生日；更新时生日列保持不变）。 */
data class FriendDraft(
    val name: String,
    val nickname: String?,
    val metNote: String?,
)

/** 列表用：朋友 + 其全部备忘。 */
data class FriendListItem(
    val friend: Friend,
    val memos: List<Memo>,
) {
    /** 最新一条备忘：createdAt 最大，平局取 id 较大者（确定化排序）。 */
    val latestMemo: Memo?
        get() = memos.maxWithOrNull(compareBy(Memo::createdAt, Memo::id))
}

/** 朋友详情：朋友 + 全部备忘。 */
data class FriendDetail(
    val friend: Friend,
    val memos: List<Memo>,
)

/** 删除某朋友将连带删除的数据量。 */
data class DeleteImpact(
    val memoCount: Int,
    val exchangeCount: Int,
)
