package com.pengyouxia.app.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.pengyouxia.app.data.db.AppDatabase
import com.pengyouxia.app.data.db.ExchangeEntity
import com.pengyouxia.app.data.db.FriendEntity
import com.pengyouxia.app.data.db.MemoEntity
import com.pengyouxia.core.model.MemoCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** FriendRepository：in-memory Room + 可推进时间 + 递增 ID。 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FriendRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: FriendRepositoryImpl
    private val time = FakeTimeSource()
    private val ids = FakeIdGenerator()

    private class FakeTimeSource(var now: Long = 1_000L) : TimeSource {
        override fun now(): Long = now
    }

    private class FakeIdGenerator(private var next: Int = 0) : IdGenerator {
        override fun newId(): String = "id-${++next}"
    }

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repo = FriendRepositoryImpl(db, ids, time)
    }

    @After
    fun tearDown() = db.close()

    private suspend fun addFriend(
        name: String = "王小明",
        nickname: String? = null,
        metNote: String? = null,
    ): String {
        val r = repo.addFriend(FriendDraft(name, nickname, metNote))
        assertTrue(r is FriendResult.Success)
        return (r as FriendResult.Success).value
    }

    private suspend fun friends() = repo.observeFriendList().first()

    // ---------- 新增 / 校验 ----------

    @Test
    fun `新增朋友规范化输入`() = runTest {
        val id = addFriend("  王小明  ", "  ", " 大学室友 ")
        val f = friends().single { it.friend.id == id }.friend
        assertEquals("王小明", f.name)
        assertNull(f.nickname)
        assertEquals("大学室友", f.metNote)
        assertEquals(1_000L, f.createdAt)
        assertEquals(1_000L, f.updatedAt)
    }

    @Test
    fun `名字空与超长返回对应错误`() = runTest {
        assertEquals(
            FriendResult.Failure(FriendError.NameRequired),
            repo.addFriend(FriendDraft("   ", null, null))
        )
        assertEquals(
            FriendResult.Failure(FriendError.NameTooLong),
            repo.addFriend(FriendDraft("汉".repeat(31), null, null))
        )
    }

    @Test
    fun `昵称与怎么认识的超长返回错误`() = runTest {
        assertEquals(
            FriendResult.Failure(FriendError.NicknameTooLong),
            repo.addFriend(FriendDraft("小王", "长".repeat(31), null))
        )
        assertEquals(
            FriendResult.Failure(FriendError.MetNoteTooLong),
            repo.addFriend(FriendDraft("小王", null, "长".repeat(101)))
        )
    }

    @Test
    fun `重名允许`() = runTest {
        addFriend("小王")
        addFriend("小王")
        assertEquals(2, friends().count { it.friend.name == "小王" })
    }

    // ---------- 修改 / 删除 ----------

    @Test
    fun `修改朋友保留生日列`() = runTest {
        db.friendDao().insert(
            FriendEntity(
                id = "f1", name = "王小明", nickname = null, metNote = null,
                birthdayCalendar = "SOLAR", birthdayYear = 1990,
                birthdayMonth = 3, birthdayDay = 12, birthdayLeap = false,
                createdAt = 1L, updatedAt = 1L,
            )
        )
        time.now = 2_000L
        val r = repo.updateFriend("f1", FriendDraft("王小明", "明明", "同事"))
        assertTrue(r is FriendResult.Success)
        val e = db.friendDao().getById("f1")!!
        assertEquals("明明", e.nickname)
        assertEquals("SOLAR", e.birthdayCalendar)
        assertEquals(1990, e.birthdayYear)
        assertEquals(3, e.birthdayMonth)
        assertEquals(12, e.birthdayDay)
        assertEquals(2_000L, e.updatedAt)
    }

    @Test
    fun `更新不存在的朋友返回 FriendNotFound`() = runTest {
        assertEquals(
            FriendResult.Failure(FriendError.FriendNotFound),
            repo.updateFriend("nope", FriendDraft("x", null, null))
        )
        assertEquals(
            FriendResult.Failure(FriendError.FriendNotFound),
            repo.deleteFriend("nope")
        )
    }

    @Test
    fun `删除朋友级联删除备忘与往来`() = runTest {
        val fid = addFriend()
        repo.addMemo(fid, MemoCategory.LIKES, "喜欢喝茶")
        insertExchange(fid)
        repo.deleteFriend(fid)
        assertTrue(friends().isEmpty())
        assertEquals(0, db.memoDao().countForFriend(fid).first())
        assertEquals(0, db.exchangeDao().countForFriend(fid).first())
    }

    @Test
    fun `DeleteImpact 计数`() = runTest {
        val fid = addFriend()
        repo.addMemo(fid, MemoCategory.LIKES, "a")
        repo.addMemo(fid, MemoCategory.WANTS, "b")
        insertExchange(fid)
        insertExchange(fid)
        assertEquals(DeleteImpact(2, 2), repo.observeDeleteImpact(fid).first())
    }

    // ---------- 备忘 ----------

    @Test
    fun `备忘增改删`() = runTest {
        val fid = addFriend()
        val mid = (repo.addMemo(fid, MemoCategory.FAMILY, " 有个女儿 ") as FriendResult.Success).value
        var detail = repo.observeFriendDetail(fid).first()!!
        assertEquals("有个女儿", detail.memos.single().content)

        repo.updateMemo(mid, MemoCategory.LIKES, "喜欢爬山")
        detail = repo.observeFriendDetail(fid).first()!!
        assertEquals(MemoCategory.LIKES, detail.memos.single().category)
        assertEquals("喜欢爬山", detail.memos.single().content)

        repo.deleteMemo(mid)
        assertTrue(repo.observeFriendDetail(fid).first()!!.memos.isEmpty())
        assertEquals(
            FriendResult.Failure(FriendError.MemoNotFound),
            repo.deleteMemo(mid)
        )
    }

    @Test
    fun `备忘内容空与超长返回错误`() = runTest {
        val fid = addFriend()
        assertEquals(
            FriendResult.Failure(FriendError.MemoEmpty),
            repo.addMemo(fid, MemoCategory.OTHER, "   ")
        )
        assertEquals(
            FriendResult.Failure(FriendError.MemoTooLong),
            repo.addMemo(fid, MemoCategory.OTHER, "长".repeat(501))
        )
    }

    @Test
    fun `想要改到其他分类时 done 清为 false`() = runTest {
        val fid = addFriend()
        val mid = (repo.addMemo(fid, MemoCategory.WANTS, "想要耳机") as FriendResult.Success).value
        repo.setMemoDone(mid, true)
        repo.updateMemo(mid, MemoCategory.LIKES, "喜欢耳机")
        assertEquals(false, repo.observeFriendDetail(fid).first()!!.memos.single().done)
    }

    @Test
    fun `非想要不能设已送出`() = runTest {
        val fid = addFriend()
        val mid = (repo.addMemo(fid, MemoCategory.LIKES, "x") as FriendResult.Success).value
        assertEquals(
            FriendResult.Failure(FriendError.DoneOnlyForWants),
            repo.setMemoDone(mid, true)
        )
    }

    @Test
    fun `写备忘同时更新所属朋友 updatedAt`() = runTest {
        val fid = addFriend()
        time.now = 5_000L
        val mid = (repo.addMemo(fid, MemoCategory.LIKES, "x") as FriendResult.Success).value
        assertEquals(5_000L, db.friendDao().getById(fid)!!.updatedAt)
        time.now = 6_000L
        repo.updateMemo(mid, MemoCategory.LIKES, "y")
        assertEquals(6_000L, db.friendDao().getById(fid)!!.updatedAt)
        time.now = 7_000L
        repo.deleteMemo(mid)
        assertEquals(7_000L, db.friendDao().getById(fid)!!.updatedAt)
    }

    @Test
    fun `写备忘不回写朋友的其他字段`() = runTest {
        val fid = addFriend("王小明")
        repo.updateFriend(fid, FriendDraft("王小明改", "明明", "同事"))
        time.now = 8_000L
        repo.addMemo(fid, MemoCategory.LIKES, "喜欢爬山")
        val e = db.friendDao().getById(fid)!!
        assertEquals("王小明改", e.name)
        assertEquals("明明", e.nickname)
        assertEquals("同事", e.metNote)
        assertEquals(8_000L, e.updatedAt)
    }

    // ---------- 读取健壮性 ----------

    @Test
    fun `未知备忘分类读为 OTHER 不崩溃`() = runTest {
        db.friendDao().insert(friendEntity("f1"))
        db.memoDao().insert(memoEntity("m1", "f1", category = "BOGUS"))
        assertEquals(
            MemoCategory.OTHER,
            repo.observeFriendDetail("f1").first()!!.memos.single().category
        )
    }

    @Test
    fun `非法生日列读为无生日`() = runTest {
        db.friendDao().insert(
            friendEntity("f1").copy(
                birthdayCalendar = "SOLAR", birthdayMonth = 13, birthdayDay = 1
            )
        )
        db.friendDao().insert(
            friendEntity("f2").copy(
                birthdayCalendar = "WEIRD", birthdayMonth = 3, birthdayDay = 1
            )
        )
        assertNull(friends().single { it.friend.id == "f1" }.friend.birthday)
        assertNull(friends().single { it.friend.id == "f2" }.friend.birthday)
    }

    @Test
    fun `合法生日列读回 Birthday`() = runTest {
        db.friendDao().insert(
            friendEntity("f1").copy(
                birthdayCalendar = "LUNAR", birthdayYear = null,
                birthdayMonth = 8, birthdayDay = 15, birthdayLeap = true
            )
        )
        val b = friends().single().friend.birthday!!
        assertEquals(8, b.month)
        assertEquals(15, b.day)
        assertTrue(b.leapMonth)
    }

    @Test
    fun `朋友不存在时详情发 null`() = runTest {
        assertNull(repo.observeFriendDetail("nope").first())
    }

    // ---------- 列表 ----------

    @Test
    fun `latestMemo 取最新且平局按 id`() = runTest {
        val fid = addFriend()
        val m1 = memoEntity("m1", fid, createdAt = 10, content = "旧")
        val m2 = memoEntity("m2", fid, createdAt = 20, content = "新")
        val m3 = memoEntity("m9", fid, createdAt = 20, content = "同刻id大")
        db.memoDao().insert(m1); db.memoDao().insert(m2); db.memoDao().insert(m3)
        assertEquals("同刻id大", friends().single().latestMemo!!.content)
    }

    @Test
    fun `clearAll 清空三张表`() = runTest {
        val fid = addFriend()
        repo.addMemo(fid, MemoCategory.LIKES, "x")
        insertExchange(fid)
        repo.clearAll()
        assertTrue(friends().isEmpty())
        assertEquals(0, db.memoDao().countForFriend(fid).first())
        assertEquals(0, db.exchangeDao().countForFriend(fid).first())
    }

    // ---------- 工具 ----------

    private fun friendEntity(id: String) = FriendEntity(
        id = id, name = "测试$id", nickname = null, metNote = null,
        birthdayCalendar = null, birthdayYear = null,
        birthdayMonth = null, birthdayDay = null, birthdayLeap = false,
        createdAt = 1L, updatedAt = 1L,
    )

    private fun memoEntity(
        id: String, friendId: String,
        category: String = MemoCategory.LIKES.name,
        content: String = "备忘$id", createdAt: Long = 1L,
    ) = MemoEntity(
        id = id, friendId = friendId, category = category, content = content,
        done = false, createdAt = createdAt, updatedAt = createdAt,
    )

    private suspend fun insertExchange(friendId: String) {
        db.exchangeDao().insert(
            ExchangeEntity(
                id = "ex-${ids.newId()}", friendId = friendId,
                direction = "OUT", kind = "MONEY", amountCents = 80000,
                item = null, occasion = "BIRTHDAY", dateEpochDay = 20000,
                note = null, sourceMemoId = null,
                createdAt = 1L, updatedAt = 1L,
            )
        )
    }
}
