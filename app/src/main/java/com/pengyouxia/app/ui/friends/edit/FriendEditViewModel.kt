package com.pengyouxia.app.ui.friends.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pengyouxia.app.data.DeleteImpact
import com.pengyouxia.app.data.FriendDraft
import com.pengyouxia.app.data.FriendRepository
import com.pengyouxia.app.data.FriendResult
import com.pengyouxia.core.model.InputLimits
import com.pengyouxia.core.model.exceedsLimit
import com.pengyouxia.core.model.normalizeOptional
import com.pengyouxia.core.model.normalizeRequired
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 编辑页一次性事件。 */
sealed interface EditEvent {
    /** 保存成功；新增时携带新 id（跳转详情用），编辑时 id 为原 id。 */
    data class Saved(val id: String) : EditEvent
    /** 删除成功。 */
    data object Deleted : EditEvent
}

@HiltViewModel
class FriendEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: FriendRepository,
) : ViewModel() {

    /** null 表示新增。 */
    val friendId: String? = savedStateHandle["id"]
    val isNew get() = friendId == null

    val name = MutableStateFlow("")
    val nickname = MutableStateFlow("")
    val metNote = MutableStateFlow("")

    /** 编辑页是否已加载完初始值（未加载完成前不算"未保存改动"）。 */
    private val loaded = MutableStateFlow(false)
    private val initial = MutableStateFlow(FriendDraft("", null, null))
    private val saving = MutableStateFlow(false)

    /** 保存失败的错误文案（成功后清空）。 */
    val saveError = MutableStateFlow<String?>(null)

    private val _events = Channel<EditEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    /** 删除影响计数（编辑页底部「删除这位朋友」用；新增页不存在）。 */
    val deleteImpact: StateFlow<DeleteImpact?> =
        if (friendId != null) {
            repository.observeDeleteImpact(friendId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
        } else MutableStateFlow(null)

    init {
        if (friendId != null) {
            viewModelScope.launch {
                repository.observeFriendDetail(friendId).collect { detail ->
                    if (detail != null && !loaded.value) {
                        val d = FriendDraft(
                            detail.friend.name,
                            detail.friend.nickname,
                            detail.friend.metNote,
                        )
                        initial.value = d
                        name.value = d.name
                        nickname.value = d.nickname ?: ""
                        metNote.value = d.metNote ?: ""
                        loaded.value = true
                    }
                }
            }
        } else {
            loaded.value = true
        }
    }

    /** 当前输入（trim 后）与初始值不同（昵称/怎么认识的空串等价 null）。 */
    val hasUnsavedChanges: StateFlow<Boolean> = combine(
        loaded, name, nickname, metNote, initial,
    ) { l, n, nn, mn, init ->
        if (!l) false
        else n.trim() != init.name ||
            normalizeOptional(nn) != init.nickname ||
            normalizeOptional(mn) != init.metNote
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private fun fieldError(text: String, max: Int): String? =
        if (exceedsLimit(text.trim(), max)) "最多 $max 个字" else null

    val nameError: StateFlow<String?> = name
        .map { fieldError(it, InputLimits.NAME) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val nicknameError: StateFlow<String?> = nickname
        .map { fieldError(it, InputLimits.NICKNAME) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val metNoteError: StateFlow<String?> = metNote
        .map { fieldError(it, InputLimits.MET_NOTE) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val canSave: StateFlow<Boolean> = combine(
        name, nickname, metNote, saving, loaded,
    ) { n, nn, mn, s, l ->
        l && !s && normalizeRequired(n).isNotEmpty() &&
            !exceedsLimit(n.trim(), InputLimits.NAME) &&
            !exceedsLimit(nn.trim(), InputLimits.NICKNAME) &&
            !exceedsLimit(mn.trim(), InputLimits.MET_NOTE)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun save() {
        if (!canSave.value) return
        saving.value = true
        viewModelScope.launch {
            val draft = FriendDraft(
                name = name.value,
                nickname = nickname.value,
                metNote = metNote.value,
            )
            val result = if (friendId != null) {
                repository.updateFriend(friendId, draft)
            } else {
                repository.addFriend(draft)
            }
            saving.value = false
            when (result) {
                is FriendResult.Success -> {
                    saveError.value = null
                    _events.send(EditEvent.Saved(friendId ?: result.value as String))
                }
                is FriendResult.Failure -> saveError.value = result.error.message
            }
        }
    }

    fun delete() {
        val id = friendId ?: return
        viewModelScope.launch {
            when (repository.deleteFriend(id)) {
                is FriendResult.Success -> _events.send(EditEvent.Deleted)
                is FriendResult.Failure -> saveError.value = "删除失败"
            }
        }
    }
}

/** 删除确认框正文（纯函数可测）。 */
fun deleteConfirmMessage(memoCount: Int, exchangeCount: Int): String = when {
    memoCount > 0 && exchangeCount > 0 ->
        "将同时删除 ${memoCount} 条备忘、${exchangeCount} 条往来记录，无法恢复。"
    memoCount > 0 -> "将同时删除 ${memoCount} 条备忘，无法恢复。"
    exchangeCount > 0 -> "将同时删除 ${exchangeCount} 条往来记录，无法恢复。"
    else -> "删除后无法恢复。"
}
