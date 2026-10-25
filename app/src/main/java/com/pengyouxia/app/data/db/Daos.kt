package com.pengyouxia.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {
    @Query("SELECT * FROM friends")
    fun observeAll(): Flow<List<FriendEntity>>

    @Query("SELECT * FROM friends WHERE id = :id")
    fun observeById(id: String): Flow<FriendEntity?>

    @Query("SELECT * FROM friends WHERE id = :id")
    suspend fun getById(id: String): FriendEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: FriendEntity)

    @Update
    suspend fun update(entity: FriendEntity)

    /** 仅刷新 updated_at：备忘写操作带动朋友行，不读改写避免覆盖并发编辑。 */
    @Query("UPDATE friends SET updated_at = :updatedAt WHERE id = :id")
    suspend fun touch(id: String, updatedAt: Long)

    @Query("DELETE FROM friends WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM friends")
    suspend fun deleteAll()
}

@Dao
interface MemoDao {
    @Query("SELECT * FROM memos")
    fun observeAll(): Flow<List<MemoEntity>>

    @Query("SELECT * FROM memos WHERE friend_id = :friendId")
    fun observeForFriend(friendId: String): Flow<List<MemoEntity>>

    @Query("SELECT * FROM memos WHERE id = :id")
    suspend fun getById(id: String): MemoEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: MemoEntity)

    @Update
    suspend fun update(entity: MemoEntity)

    @Query("DELETE FROM memos WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM memos WHERE friend_id = :friendId")
    fun countForFriend(friendId: String): Flow<Int>

    @Query("DELETE FROM memos")
    suspend fun deleteAll()
}

/** 往来 DAO：M1 仅供测试插入与影响计数，M3 扩展业务方法。 */
@Dao
interface ExchangeDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: ExchangeEntity)

    @Query("SELECT COUNT(*) FROM exchanges WHERE friend_id = :friendId")
    fun countForFriend(friendId: String): Flow<Int>

    @Query("DELETE FROM exchanges")
    suspend fun deleteAll()
}
