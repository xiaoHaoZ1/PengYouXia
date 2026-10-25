package com.pengyouxia.app.ui.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pengyouxia.app.data.FriendListItem
import com.pengyouxia.app.data.FriendRepository
import com.pengyouxia.app.domain.FriendOrdering
import com.pengyouxia.app.domain.FriendSearch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** 朋友页状态：[results] 为 null 表示未在搜索（显示分组列表）。 */
data class FriendsUiState(
    val loaded: Boolean = false,
    val isEmpty: Boolean = true,
    val query: String = "",
    val groups: List<FriendOrdering.FriendGroup> = emptyList(),
    val results: List<FriendSearch.FriendSearchResult>? = null,
)

@OptIn(FlowPreview::class)
@HiltViewModel
class FriendsViewModel @Inject constructor(
    repository: FriendRepository,
) : ViewModel() {

    /** 搜索输入（输入框立即读它，不经过防抖）。 */
    val query = MutableStateFlow("")

    private val ordering = FriendOrdering()
    private val search = FriendSearch(ordering = ordering)

    val uiState: StateFlow<FriendsUiState> = combine(
        repository.observeFriendList(),
        query.debounce(150),
    ) { items, q ->
        FriendsUiState(
            loaded = true,
            isEmpty = items.isEmpty(),
            query = q,
            groups = if (q.isBlank()) ordering.group(items) else emptyList(),
            results = if (q.isBlank()) null else search.search(items, q),
        )
    }
        // 排序与搜索都在后台算
        .flowOn(Dispatchers.Default)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            FriendsUiState(),
        )

    fun onQueryChange(value: String) {
        query.value = value
    }
}

/**
 * 列表第二行：最新备忘 → 怎么认识的 → 无（不显示）。
 * 业务规则不写在 Composable 里，单列出来可测。
 */
fun secondLine(item: FriendListItem): String? =
    item.latestMemo?.content ?: item.friend.metNote
