package com.pengyouxia.app.ui.frienddetail

import androidx.lifecycle.SavedStateHandle
import com.pengyouxia.app.FakeIdGenerator
import com.pengyouxia.app.FakeSettingsRepository
import com.pengyouxia.app.FakeTimeSource
import com.pengyouxia.app.MainDispatcherRule
import com.pengyouxia.app.data.FriendDraft
import com.pengyouxia.app.data.FriendRepository
import com.pengyouxia.app.data.FriendResult
import com.pengyouxia.app.data.db.AppDatabase
import com.pengyouxia.app.newTestDb
import com.pengyouxia.app.newRepo
import com.pengyouxia.core.model.Memo
import com.pengyouxia.core.model.MemoCategory
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
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
class FriendDetailViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var db: AppDatabase
    private lateinit var repo: FriendRepository
    private val time = FakeTimeSource()
    private lateinit var friendId: String

    private fun newVm(settings: FakeSettingsRepository = FakeSettingsRepository()) =
        FriendDetailViewModel(
            SavedStateHandle(mapOf("id" to friendId)), repo, settings, time
        )

    @Before
    fun setUp() = runTest {
        db = newTestDb()
        repo = newRepo(db, time, FakeIdGenerator())
        friendId = (repo.addFriend(FriendDraft("王小明", null, null)) as FriendResult.Success).value
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `openNewMemo 默认分类取自设置`() = runTest {
        val vm = newVm(FakeSettingsRepository(MemoCategory.LIKES))
        vm.openNewMemo()
        advanceUntilIdle()
        assertEquals(MemoCategory.LIKES, vm.editor.value!!.category)
        assertNull(vm.editor.value!!.editingMemoId)
    }

    @Test
    fun `saveMemo 后设置更新为本次分类`() = runTest {
        val settings = FakeSettingsRepository()
        val vm = newVm(settings)
        backgroundScope.launch { vm.uiState.collect {} }
        vm.openNewMemo()
        advanceUntilIdle()
        vm.updateEditorCategory(MemoCategory.WANTS)
        vm.updateEditorContent("想要耳机")
        vm.saveMemo()
        // 仓库写库走真实线程，挂起等待设置被更新
        assertEquals(MemoCategory.WANTS, settings.lastMemoCategory.first { it == MemoCategory.WANTS })
        assertNull(vm.editor.value)
    }

    @Test
    fun `分组排序空组不出现且想要已送出置末`() {
        fun m(id: String, cat: MemoCategory, createdAt: Long, done: Boolean = false) =
            Memo(id, "f", cat, "c$id", done, createdAt, createdAt)
        val groups = memoGroupsOf(
            listOf(
                m("a", MemoCategory.WANTS, 3, done = true),
                m("b", MemoCategory.WANTS, 2),
                m("c", MemoCategory.WANTS, 1),
                m("d", MemoCategory.LIKES, 1),
                m("e", MemoCategory.LIKES, 5),
            )
        )
        assertEquals(listOf(MemoCategory.LIKES, MemoCategory.WANTS), groups.map { it.category })
        assertEquals(listOf("e", "d"), groups[0].memos.map { it.id })
        // 未送出按时间倒序在前，已送出在末
        assertEquals(listOf("b", "c", "a"), groups[1].memos.map { it.id })
    }

    @Test
    fun `formatMemoDate 同年与往年`() {
        val zone = ZoneId.of("Asia/Shanghai")
        val now = Instant.parse("2026-10-25T04:00:00Z").toEpochMilli()
        val sameYear = Instant.parse("2026-03-12T04:00:00Z").toEpochMilli()
        val lastYear = Instant.parse("2025-12-01T04:00:00Z").toEpochMilli()
        assertEquals("3月12日", formatMemoDate(sameYear, now, zone))
        assertEquals("2025年12月1日", formatMemoDate(lastYear, now, zone))
    }

    @Test
    fun `朋友被删后 gone`() = runTest {
        val vm = newVm()
        // stateIn(WhileSubscribed) 需要收集才会更新
        backgroundScope.launch { vm.uiState.collect {} }
        // Room 发流走真实线程，挂起等待而非 advanceUntilIdle
        vm.uiState.first { it.friend != null }
        repo.deleteFriend(friendId)
        assertTrue(vm.uiState.first { it.gone }.gone)
    }
}
