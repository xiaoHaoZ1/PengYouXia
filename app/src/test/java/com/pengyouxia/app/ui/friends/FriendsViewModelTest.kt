package com.pengyouxia.app.ui.friends

import com.pengyouxia.app.FakeIdGenerator
import com.pengyouxia.app.FakeTimeSource
import com.pengyouxia.app.MainDispatcherRule
import com.pengyouxia.app.data.FriendDraft
import com.pengyouxia.app.data.FriendRepository
import com.pengyouxia.app.data.FriendResult
import com.pengyouxia.app.data.db.AppDatabase
import com.pengyouxia.app.newTestDb
import com.pengyouxia.app.newRepo
import com.pengyouxia.core.model.MemoCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
class FriendsViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var db: AppDatabase
    private lateinit var repo: FriendRepository
    private val time = FakeTimeSource()
    private lateinit var vm: FriendsViewModel

    @Before
    fun setUp() {
        db = newTestDb()
        repo = newRepo(db, time, FakeIdGenerator())
        vm = FriendsViewModel(repo)
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `输入查询后 results 非空且命中`() = runTest {
        // stateIn(WhileSubscribed) 需要收集才会更新
        backgroundScope.launch { vm.uiState.collect {} }
        repo.addFriend(FriendDraft("王小明", null, null))
        repo.addFriend(FriendDraft("李雷", null, null))
        advanceUntilIdle()
        vm.onQueryChange("小明")
        // 防抖 150ms 内还没出结果
        advanceTimeBy(160)
        advanceUntilIdle()
        val state = vm.uiState.first { it.results != null }
        assertEquals(listOf("王小明"), state.results!!.map { it.item.friend.name })
    }

    @Test
    fun `清空查询回到分组列表`() = runTest {
        backgroundScope.launch { vm.uiState.collect {} }
        val id = (repo.addFriend(FriendDraft("王小明", null, null)) as FriendResult.Success).value
        repo.addMemo(id, MemoCategory.LIKES, "露营")
        advanceUntilIdle()
        vm.onQueryChange("露营")
        advanceTimeBy(160)
        assertNotNull(vm.uiState.first { it.results != null }.results)
        vm.onQueryChange("")
        advanceTimeBy(160)
        val state = vm.uiState.first { it.results == null && it.loaded }
        assertNull(state.results)
        assertTrue(state.groups.isNotEmpty())
    }
}
