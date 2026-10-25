package com.pengyouxia.app.data

import com.pengyouxia.core.model.MemoCategory
import kotlinx.coroutines.flow.Flow

/** 朋友档案与备忘的唯一写入路径；多表写入都在事务内。 */
interface FriendRepository {

    /** 全部朋友及各自备忘（列表页用）。 */
    fun observeFriendList(): Flow<List<FriendListItem>>

    /** 单个朋友详情；朋友不存在（被删）时发 null。 */
    fun observeFriendDetail(id: String): Flow<FriendDetail?>

    /** 删除该朋友将连带删除的备忘/往来数量。 */
    fun observeDeleteImpact(friendId: String): Flow<DeleteImpact>

    /** 新增朋友，返回新 id。 */
    suspend fun addFriend(draft: FriendDraft): FriendResult<String>

    /** 修改朋友（生日列保持不变）。 */
    suspend fun updateFriend(id: String, draft: FriendDraft): FriendResult<Unit>

    /** 删除朋友（备忘、往来经外键级联删除）。 */
    suspend fun deleteFriend(id: String): FriendResult<Unit>

    suspend fun addMemo(friendId: String, category: MemoCategory, content: String): FriendResult<String>

    /** 修改备忘内容与分类；从 WANTS 改到其他分类时 done 清为 false。 */
    suspend fun updateMemo(memoId: String, category: MemoCategory, content: String): FriendResult<Unit>

    suspend fun deleteMemo(memoId: String): FriendResult<Unit>

    /** 标记「想要」已送出；非 WANTS 分类返回 [FriendError.DoneOnlyForWants]。 */
    suspend fun setMemoDone(memoId: String, done: Boolean): FriendResult<Unit>

    /** 清空三张表（测试工具用），一个事务。 */
    suspend fun clearAll()
}
