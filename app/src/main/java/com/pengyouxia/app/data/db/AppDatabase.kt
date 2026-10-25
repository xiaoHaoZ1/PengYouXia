package com.pengyouxia.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

/** 应用数据库。version=1 一次建全三张表；禁止破坏性迁移。 */
@Database(
    entities = [FriendEntity::class, MemoEntity::class, ExchangeEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun friendDao(): FriendDao
    abstract fun memoDao(): MemoDao
    abstract fun exchangeDao(): ExchangeDao
}
