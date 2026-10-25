package com.pengyouxia.app.ui.friends.edit

import androidx.lifecycle.SavedStateHandle
import com.pengyouxia.app.FakeIdGenerator
import com.pengyouxia.app.FakeTimeSource
import com.pengyouxia.app.MainDispatcherRule
import com.pengyouxia.app.data.FriendDraft
import com.pengyouxia.app.data.FriendResult
import com.pengyouxia.app.data.db.AppDatabase
import com.pengyouxia.app.newTestDb
import com.pengyouxia.app.newRepo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FriendEditViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private lateinit var db: AppDatabase
    private val time = FakeTimeSource()

    @After
    fun tearDown() {
        if (::db.isInitialized) db.close()
    }

    private fun newVm(id: String? = null): FriendEditViewModel {
        db = newTestDb()
        val handle = SavedStateHandle()
        if (id != null) handle["id"] = id
        return FriendEditViewModel(handle, newRepo(db, time, FakeIdGenerator()))
    }

    /** stateIn(WhileSubscribed) 需要有人收集才会更新，后台收集以便断言 .value。 */
    private fun kotlinx.coroutines.CoroutineScope.watch(vm: FriendEditViewModel) {
        launch { vm.canSave.collect {} }
        launch { vm.nameError.collect {} }
        launch { vm.hasUnsavedChanges.collect {} }
    }

    @Test
    fun `名字为空或全空格不可保存`() = runTest {
        val vm = newVm()
        backgroundScope.watch(vm)
        advanceUntilIdle()
        assertFalse(vm.canSave.value)
        vm.name.value = "   "
        advanceUntilIdle()
        assertFalse(vm.canSave.value)
        vm.name.value = "王小明"
        advanceUntilIdle()
        assertTrue(vm.canSave.value)
    }

    @Test
    fun `超限不可保存且有错误文案`() = runTest {
        val vm = newVm()
        backgroundScope.watch(vm)
        advanceUntilIdle()
        vm.name.value = "长".repeat(31)
        advanceUntilIdle()
        assertFalse(vm.canSave.value)
        assertEquals("最多 30 个字", vm.nameError.value)
    }

    @Test
    fun `新增页改动与还原`() = runTest {
        val vm = newVm()
        backgroundScope.watch(vm)
        advanceUntilIdle()
        assertFalse(vm.hasUnsavedChanges.value)
        vm.name.value = "王小明"
        advanceUntilIdle()
        assertTrue(vm.hasUnsavedChanges.value)
        vm.name.value = ""
        advanceUntilIdle()
        assertFalse(vm.hasUnsavedChanges.value)
    }

    @Test
    fun `编辑页加载后未改动`() = runTest {
        db = newTestDb()
        val repo = newRepo(db, time, FakeIdGenerator())
        val id = (repo.addFriend(FriendDraft("王小明", "明明", "同事")) as FriendResult.Success).value
        val vm = FriendEditViewModel(SavedStateHandle(mapOf("id" to id)), repo)
        backgroundScope.watch(vm)
        // Room 发流走真实线程，挂起等待初始值载入
        assertEquals("王小明", vm.name.first { it.isNotEmpty() })
        advanceUntilIdle()
        assertFalse(vm.hasUnsavedChanges.value)
        vm.nickname.value = "别的小明"
        advanceUntilIdle()
        assertTrue(vm.hasUnsavedChanges.value)
    }

    @Test
    fun `保存成功发 Saved 事件`() = runTest {
        val vm = newVm()
        backgroundScope.watch(vm)
        advanceUntilIdle()
        vm.name.value = "王小明"
        advanceUntilIdle()
        vm.save()
        // 仓库写库走真实线程，用挂起等待事件而非 advanceUntilIdle
        val event = vm.events.first()
        assertTrue(event is EditEvent.Saved)
    }

    @Test
    fun `删除确认文案四种情况`() {
        assertEquals("将同时删除 2 条备忘、3 条往来记录，无法恢复。", deleteConfirmMessage(2, 3))
        assertEquals("将同时删除 2 条备忘，无法恢复。", deleteConfirmMessage(2, 0))
        assertEquals("将同时删除 3 条往来记录，无法恢复。", deleteConfirmMessage(0, 3))
        assertEquals("删除后无法恢复。", deleteConfirmMessage(0, 0))
    }
}
