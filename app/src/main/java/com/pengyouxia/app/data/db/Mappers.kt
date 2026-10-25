package com.pengyouxia.app.data.db

import android.util.Log
import com.pengyouxia.core.model.Birthday
import com.pengyouxia.core.model.CalendarType
import com.pengyouxia.core.model.Friend
import com.pengyouxia.core.model.Memo
import com.pengyouxia.core.model.MemoCategory

private const val TAG = "DbMappers"

fun FriendEntity.toDomain(): Friend = Friend(
    id = id,
    name = name,
    nickname = nickname,
    metNote = metNote,
    birthday = toBirthday(),
    createdAt = createdAt,
    updatedAt = updatedAt,
)

/**
 * 生日列 → [Birthday]；calendar 为 null 视为无生日；
 * 未知历法或非法数值不崩溃，记日志并视为无生日。
 */
private fun FriendEntity.toBirthday(): Birthday? {
    val cal = birthdayCalendar ?: return null
    val type = runCatching { CalendarType.valueOf(cal) }.getOrElse {
        Log.w(TAG, "未知 birthday_calendar=$cal，朋友 $id 按无生日处理")
        return null
    }
    val month = birthdayMonth ?: return null
    val day = birthdayDay ?: return null
    return runCatching { Birthday(type, birthdayYear, month, day, birthdayLeap) }
        .getOrElse {
            Log.w(TAG, "朋友 $id 生日列非法（$cal $month/$day），按无生日处理")
            null
        }
}

fun MemoEntity.toDomain(): Memo = Memo(
    id = id,
    friendId = friendId,
    category = toMemoCategory(),
    content = content,
    done = done,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

/** 未知分类归入 OTHER，不崩溃。 */
private fun MemoEntity.toMemoCategory(): MemoCategory =
    runCatching { MemoCategory.valueOf(category) }.getOrElse {
        Log.w(TAG, "未知 memo category=$category，备忘 $id 归入 OTHER")
        MemoCategory.OTHER
    }
