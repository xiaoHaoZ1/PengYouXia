package com.pengyouxia.app.data

import androidx.room.withTransaction
import com.pengyouxia.app.data.db.AppDatabase
import com.pengyouxia.app.data.db.FriendEntity
import com.pengyouxia.app.data.db.MemoEntity
import com.pengyouxia.app.data.db.toDomain
import com.pengyouxia.core.model.InputLimits
import com.pengyouxia.core.model.MemoCategory
import com.pengyouxia.core.model.exceedsLimit
import com.pengyouxia.core.model.normalizeOptional
import com.pengyouxia.core.model.normalizeRequired
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Singleton
class FriendRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val ids: IdGenerator,
    private val time: TimeSource,
) : FriendRepository {

    private val friendDao get() = db.friendDao()
    private val memoDao get() = db.memoDao()
    private val exchangeDao get() = db.exchangeDao()

    override fun observeFriendList(): Flow<List<FriendListItem>> =
        combine(friendDao.observeAll(), memoDao.observeAll()) { friends, memos ->
            val byFriend = memos.groupBy { it.friendId }
            friends.map { f ->
                FriendListItem(f.toDomain(), byFriend[f.id].orEmpty().map { it.toDomain() })
            }
        }

    override fun observeFriendDetail(id: String): Flow<FriendDetail?> =
        combine(friendDao.observeById(id), memoDao.observeForFriend(id)) { friend, memos ->
            friend?.let { FriendDetail(it.toDomain(), memos.map { m -> m.toDomain() }) }
        }

    override fun observeDeleteImpact(friendId: String): Flow<DeleteImpact> =
        combine(
            memoDao.countForFriend(friendId),
            exchangeDao.countForFriend(friendId),
        ) { memos, exchanges -> DeleteImpact(memos, exchanges) }

    override suspend fun addFriend(draft: FriendDraft): FriendResult<String> {
        draftError(draft)?.let { return FriendResult.Failure(it) }
        val checked = normalizeDraft(draft)
        val now = time.now()
        val id = ids.newId()
        friendDao.insert(
            FriendEntity(
                id = id, name = checked.name, nickname = checked.nickname,
                metNote = checked.metNote,
                birthdayCalendar = null, birthdayYear = null,
                birthdayMonth = null, birthdayDay = null, birthdayLeap = false,
                createdAt = now, updatedAt = now,
            )
        )
        return FriendResult.Success(id)
    }

    override suspend fun updateFriend(id: String, draft: FriendDraft): FriendResult<Unit> {
        draftError(draft)?.let { return FriendResult.Failure(it) }
        val checked = normalizeDraft(draft)
        // 读与写在同一事务：期间被编辑不会产生丢失更新
        return db.withTransaction {
            val old = friendDao.getById(id)
                ?: return@withTransaction FriendResult.Failure(FriendError.FriendNotFound)
            // 生日列与 createdAt 不变，M2 的编辑界面再处理生日
            friendDao.update(
                old.copy(
                    name = checked.name, nickname = checked.nickname,
                    metNote = checked.metNote, updatedAt = time.now(),
                )
            )
            FriendResult.Success(Unit)
        }
    }

    override suspend fun deleteFriend(id: String): FriendResult<Unit> {
        friendDao.getById(id) ?: return FriendResult.Failure(FriendError.FriendNotFound)
        friendDao.deleteById(id)
        return FriendResult.Success(Unit)
    }

    override suspend fun addMemo(
        friendId: String,
        category: MemoCategory,
        content: String,
    ): FriendResult<String> {
        memoContentError(content)?.let { return FriendResult.Failure(it) }
        val checked = normalizeRequired(content)
        val now = time.now()
        val id = ids.newId()
        return db.withTransaction {
            friendDao.getById(friendId)
                ?: return@withTransaction FriendResult.Failure(FriendError.FriendNotFound)
            memoDao.insert(
                MemoEntity(
                    id = id, friendId = friendId, category = category.name,
                    content = checked, done = false,
                    createdAt = now, updatedAt = now,
                )
            )
            friendDao.touch(friendId, now)
            FriendResult.Success(id)
        }
    }

    override suspend fun updateMemo(
        memoId: String,
        category: MemoCategory,
        content: String,
    ): FriendResult<Unit> {
        memoContentError(content)?.let { return FriendResult.Failure(it) }
        val checked = normalizeRequired(content)
        val now = time.now()
        return db.withTransaction {
            val memo = memoDao.getById(memoId)
                ?: return@withTransaction FriendResult.Failure(FriendError.MemoNotFound)
            memoDao.update(
                memo.copy(
                    category = category.name,
                    content = checked,
                    // 从「想要」改到其他分类时 done 失效
                    done = if (category == MemoCategory.WANTS) memo.done else false,
                    updatedAt = now,
                )
            )
            friendDao.touch(memo.friendId, now)
            FriendResult.Success(Unit)
        }
    }

    override suspend fun deleteMemo(memoId: String): FriendResult<Unit> {
        val now = time.now()
        return db.withTransaction {
            val memo = memoDao.getById(memoId)
                ?: return@withTransaction FriendResult.Failure(FriendError.MemoNotFound)
            memoDao.deleteById(memoId)
            friendDao.touch(memo.friendId, now)
            FriendResult.Success(Unit)
        }
    }

    override suspend fun setMemoDone(memoId: String, done: Boolean): FriendResult<Unit> {
        val now = time.now()
        return db.withTransaction {
            val memo = memoDao.getById(memoId)
                ?: return@withTransaction FriendResult.Failure(FriendError.MemoNotFound)
            if (memo.category != MemoCategory.WANTS.name) {
                return@withTransaction FriendResult.Failure(FriendError.DoneOnlyForWants)
            }
            memoDao.update(memo.copy(done = done, updatedAt = now))
            friendDao.touch(memo.friendId, now)
            FriendResult.Success(Unit)
        }
    }

    override suspend fun clearAll() {
        db.withTransaction {
            memoDao.deleteAll()
            exchangeDao.deleteAll()
            friendDao.deleteAll()
        }
    }

    /** 草稿规范化（在 [draftError] 校验通过后调用）。 */
    private fun normalizeDraft(draft: FriendDraft): FriendDraft = FriendDraft(
        name = normalizeRequired(draft.name),
        nickname = normalizeOptional(draft.nickname),
        metNote = normalizeOptional(draft.metNote),
    )

    private fun draftError(draft: FriendDraft): FriendError? {
        val name = normalizeRequired(draft.name)
        val nickname = normalizeOptional(draft.nickname)
        val metNote = normalizeOptional(draft.metNote)
        return when {
            name.isEmpty() -> FriendError.NameRequired
            exceedsLimit(name, InputLimits.NAME) -> FriendError.NameTooLong
            nickname != null && exceedsLimit(nickname, InputLimits.NICKNAME) ->
                FriendError.NicknameTooLong
            metNote != null && exceedsLimit(metNote, InputLimits.MET_NOTE) ->
                FriendError.MetNoteTooLong
            else -> null
        }
    }

    private fun memoContentError(content: String): FriendError? {
        val text = normalizeRequired(content)
        return when {
            text.isEmpty() -> FriendError.MemoEmpty
            exceedsLimit(text, InputLimits.MEMO) -> FriendError.MemoTooLong
            else -> null
        }
    }
}
