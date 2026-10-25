package com.pengyouxia.app.ui.mine

import com.pengyouxia.app.FakeIdGenerator
import com.pengyouxia.app.FakeSettingsRepository
import com.pengyouxia.app.FakeTimeSource
import com.pengyouxia.app.MainDispatcherRule
import com.pengyouxia.app.data.FriendRepository
import com.pengyouxia.app.data.db.AppDatabase
import com.pengyouxia.app.data.demo.DemoDataSeeder
import com.pengyouxia.app.newTestDb
import com.pengyouxia.app.newRepo
import com.pengyouxia.core.model.MemoCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MineViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var db: AppDatabase
    private lateinit var repo: FriendRepository
    private lateinit var vm: MineViewModel
    private val settings = FakeSettingsRepository(MemoCategory.LIKES)

    @Before
    fun setUp() {
        db = newTestDb()
        repo = newRepo(db, FakeTimeSource(), FakeIdGenerator())
        vm = MineViewModel(DemoDataSeeder(repo), repo)
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `填充演示数据后 12 位朋友`() = runTest {
        vm.fillDemoData()
        val items = repo.observeFriendList().first { it.isNotEmpty() }
        // Room 发流在真实线程，等全部写完
        val list = repo.observeFriendList().first { it.size == 12 }
        assertEquals(12, list.size)
        // 含两位重名「小王」
        assertEquals(2, list.count { it.friend.name == "小王" })
        // 有已送出的「想要」
        assertTrue(list.any { item -> item.memos.any { it.category == MemoCategory.WANTS && it.done } })
        assertTrue(items.isNotEmpty())
    }

    @Test
    fun `清空后列表为空且设置不受影响`() = runTest {
        vm.fillDemoData()
        repo.observeFriendList().first { it.size == 12 }
        vm.clearAll()
        val list = repo.observeFriendList().first { it.isEmpty() }
        assertTrue(list.isEmpty())
        assertEquals(MemoCategory.LIKES, settings.lastMemoCategory.first())
    }
}
