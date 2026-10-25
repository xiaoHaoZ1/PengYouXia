package com.pengyouxia.app.ui.frienddetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pengyouxia.app.data.FriendResult
import com.pengyouxia.app.data.FriendRepository
import com.pengyouxia.app.data.TimeSource
import com.pengyouxia.app.data.settings.SettingsRepository
import com.pengyouxia.core.model.Friend
import com.pengyouxia.core.model.InputLimits
import com.pengyouxia.core.model.Memo
import com.pengyouxia.core.model.MemoCategory
import com.pengyouxia.core.model.exceedsLimit
import com.pengyouxia.core.model.normalizeRequired
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 一个分类的备忘分组。 */
data class MemoGroup(val category: MemoCategory, val memos: List<Memo>)

/**
 * 备忘分组排序（纯函数）：按分类声明顺序、空分类不出现；
 * 组内 createdAt 倒序；「想要」组里已送出的排到组末。
 */
fun memoGroupsOf(memos: List<Memo>): List<MemoGroup> =
    MemoCategory.entries.mapNotNull { category ->
        val group = memos.filter { it.category == category }
        if (group.isEmpty()) return@mapNotNull null
        val sorted = if (category == MemoCategory.WANTS) {
            group.sortedWith(compareBy<Memo> { it.done }.thenByDescending { it.createdAt })
        } else {
            group.sortedByDescending { it.createdAt }
        }
        MemoGroup(category, sorted)
    }

/** 同年显示「10月25日」，往年「2025年10月25日」。 */
fun formatMemoDate(epochMillis: Long, nowMillis: Long, zone: ZoneId): String {
    val date = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
    val nowYear = Instant.ofEpochMilli(nowMillis).atZone(zone).year
    return if (date.year == nowYear) "${date.monthValue}月${date.dayOfMonth}日"
    else "${date.year}年${date.monthValue}月${date.dayOfMonth}日"
}

/** 详情页状态：[gone] 表示朋友已被删除。 */
data class DetailUiState(
    val loading: Boolean = true,
    val gone: Boolean = false,
    val friend: Friend? = null,
    val memoGroups: List<MemoGroup> = emptyList(),
    val nowMillis: Long = 0L,
)

/** 记备忘弹层状态；null 表示弹层关闭。 */
data class MemoEditorState(
    val editingMemoId: String?,
    val category: MemoCategory,
    val content: String,
    val saving: Boolean = false,
    val error: String? = null,
) {
    /** 内容规范化后非空且未超限才可保存。 */
    val canSave: Boolean
        get() {
            val t = normalizeRequired(content)
            return !saving && t.isNotEmpty() && !exceedsLimit(t, InputLimits.MEMO)
        }

    val contentError: String?
        get() = if (exceedsLimit(normalizeRequired(content), InputLimits.MEMO)) {
            "最多 ${InputLimits.MEMO} 个字"
        } else null
}

@HiltViewModel
class FriendDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: FriendRepository,
    private val settings: SettingsRepository,
    private val time: TimeSource,
) : ViewModel() {

    val friendId: String = checkNotNull(savedStateHandle["id"])

    val uiState: StateFlow<DetailUiState> = repository.observeFriendDetail(friendId)
        .map { detail ->
            if (detail == null) DetailUiState(loading = false, gone = true)
            else DetailUiState(
                loading = false,
                friend = detail.friend,
                memoGroups = memoGroupsOf(detail.memos),
                nowMillis = time.now(),
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailUiState())

    /** 弹层状态；null = 关闭。 */
    val editor = MutableStateFlow<MemoEditorState?>(null)

    fun openNewMemo() {
        viewModelScope.launch {
            val category = settings.lastMemoCategory.first()
            editor.value = MemoEditorState(null, category, "")
        }
    }

    fun openMemo(memo: Memo) {
        editor.value = MemoEditorState(memo.id, memo.category, memo.content)
    }

    fun dismissEditor() {
        editor.value = null
    }

    fun updateEditorContent(content: String) {
        editor.value = editor.value?.copy(content = content, error = null)
    }

    fun updateEditorCategory(category: MemoCategory) {
        editor.value = editor.value?.copy(category = category)
    }

    fun saveMemo() {
        val e = editor.value ?: return
        if (!e.canSave) return
        editor.value = e.copy(saving = true)
        viewModelScope.launch {
            val content = normalizeRequired(e.content)
            val result = if (e.editingMemoId == null) {
                repository.addMemo(friendId, e.category, content)
            } else {
                repository.updateMemo(e.editingMemoId, e.category, content)
            }
            when (result) {
                is FriendResult.Success -> {
                    settings.setLastMemoCategory(e.category)
                    editor.value = null
                }
                is FriendResult.Failure ->
                    editor.value = editor.value?.copy(saving = false, error = result.error.message)
            }
        }
    }

    fun deleteMemo() {
        val e = editor.value ?: return
        val memoId = e.editingMemoId ?: return
        viewModelScope.launch {
            val result = repository.deleteMemo(memoId)
            when (result) {
                is FriendResult.Success -> editor.value = null
                is FriendResult.Failure ->
                    editor.value = editor.value?.copy(saving = false, error = result.error.message)
            }
        }
    }

    fun toggleDone(memoId: String, done: Boolean) {
        viewModelScope.launch { repository.setMemoDone(memoId, done) }
    }
}
