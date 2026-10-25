package com.pengyouxia.app.data

import com.pengyouxia.core.model.InputLimits

/** 数据层操作结果：成功携带值，失败携带中文错误。 */
sealed interface FriendResult<out T> {
    data class Success<T>(val value: T) : FriendResult<T>
    data class Failure(val error: FriendError) : FriendResult<Nothing>
}

/** 业务错误（界面直接展示 message）。 */
sealed class FriendError(val message: String) {
    data object NameRequired : FriendError("请填写名字")
    data object NameTooLong : FriendError("名字最多 ${InputLimits.NAME} 个字")
    data object NicknameTooLong : FriendError("昵称最多 ${InputLimits.NICKNAME} 个字")
    data object MetNoteTooLong : FriendError("“怎么认识的”最多 ${InputLimits.MET_NOTE} 个字")
    data object MemoEmpty : FriendError("备忘内容不能为空")
    data object MemoTooLong : FriendError("备忘最多 ${InputLimits.MEMO} 个字")
    data object FriendNotFound : FriendError("朋友不存在")
    data object MemoNotFound : FriendError("备忘不存在")
    data object DoneOnlyForWants : FriendError("只有“想要”可以标记已送出")
}
