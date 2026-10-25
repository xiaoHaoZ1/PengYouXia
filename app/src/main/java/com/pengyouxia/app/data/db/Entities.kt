package com.pengyouxia.app.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 朋友。生日拆成可空列（M1 无编辑界面，M2 使用）；
 * 枚举一律存 name 字符串，不用 TypeConverter。
 */
@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey val id: String,
    val name: String,
    val nickname: String?,
    @ColumnInfo(name = "met_note") val metNote: String?,
    @ColumnInfo(name = "birthday_calendar") val birthdayCalendar: String?,
    @ColumnInfo(name = "birthday_year") val birthdayYear: Int?,
    @ColumnInfo(name = "birthday_month") val birthdayMonth: Int?,
    @ColumnInfo(name = "birthday_day") val birthdayDay: Int?,
    @ColumnInfo(name = "birthday_leap", defaultValue = "0") val birthdayLeap: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)

/** 朋友备忘。删除朋友时级联删除。 */
@Entity(
    tableName = "memos",
    foreignKeys = [
        ForeignKey(
            entity = FriendEntity::class,
            parentColumns = ["id"],
            childColumns = ["friend_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("friend_id")],
)
data class MemoEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "friend_id") val friendId: String,
    val category: String,
    val content: String,
    @ColumnInfo(defaultValue = "0") val done: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)

/**
 * 人情往来（M1 只用于删除影响计数与级联；M3 写业务）。
 * [sourceMemoId] 由「想要」备忘转化而来时指向该备忘，备忘删除后置空。
 */
@Entity(
    tableName = "exchanges",
    foreignKeys = [
        ForeignKey(
            entity = FriendEntity::class,
            parentColumns = ["id"],
            childColumns = ["friend_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = MemoEntity::class,
            parentColumns = ["id"],
            childColumns = ["source_memo_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("friend_id"), Index("source_memo_id")],
)
data class ExchangeEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "friend_id") val friendId: String,
    /** ExchangeDirection.name */
    val direction: String,
    /** ExchangeKind.name */
    val kind: String,
    /** 金额（分），非金钱往来为 null。 */
    @ColumnInfo(name = "amount_cents") val amountCents: Long?,
    /** 礼物/请吃饭的内容描述。 */
    val item: String?,
    /** ExchangeOccasion.name */
    val occasion: String?,
    /** 发生日期，LocalDate.toEpochDay()。 */
    @ColumnInfo(name = "date_epoch_day") val dateEpochDay: Long,
    val note: String?,
    @ColumnInfo(name = "source_memo_id") val sourceMemoId: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
