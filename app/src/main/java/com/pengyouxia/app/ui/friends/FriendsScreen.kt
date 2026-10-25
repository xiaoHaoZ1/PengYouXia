package com.pengyouxia.app.ui.friends

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pengyouxia.app.domain.FriendSearch
import com.pengyouxia.core.designsystem.EmptyState
import com.pengyouxia.core.designsystem.FriendAvatar
import com.pengyouxia.core.designsystem.PageHeader
import com.pengyouxia.core.designsystem.PageHeaderIconButton
import com.pengyouxia.core.designsystem.PyxCard
import com.pengyouxia.core.designsystem.PyxDimens
import com.pengyouxia.core.designsystem.theme.LocalPyxColors

/** 「朋友」一级页：搜索 + 拼音分组列表。 */
@Composable
fun FriendsScreen(
    onAdd: () -> Unit,
    onOpenFriend: (String) -> Unit,
    viewModel: FriendsViewModel = hiltViewModel(),
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize()) {
        PageHeader("朋友") {
            PageHeaderIconButton(Icons.Filled.Add, "添加朋友", onAdd)
        }
        if (ui.loaded && ui.isEmpty && ui.results == null) {
            // 没有任何朋友时不显示搜索框（没有可搜内容）
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    title = "还没有朋友",
                    message = "记下朋友的生日、喜好和往来，下次见面不用再想",
                    actionLabel = "添加第一位朋友",
                    onAction = onAdd
                )
            }
        } else {
            SearchField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                onClear = { viewModel.onQueryChange("") },
            )
            val results = ui.results
            if (results != null) {
                SearchResults(results, ui.query, onOpenFriend)
            } else {
                FriendList(ui.groups, onOpenFriend)
            }
        }
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PyxDimens.pageHorizontal),
        placeholder = { Text("搜名字、昵称、备忘") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Filled.Close, contentDescription = "清除")
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
    )
}

@Composable
private fun FriendList(
    groups: List<com.pengyouxia.app.domain.FriendOrdering.FriendGroup>,
    onOpenFriend: (String) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(
            horizontal = PyxDimens.pageHorizontal, vertical = 12.dp
        )
    ) {
        groups.forEach { group ->
            item(key = "h-${group.key}") {
                if (group.key != null) {
                    Text(
                        group.key,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
            }
            item(key = "g-${group.key}") {
                PyxCard(contentPadding = PaddingValues(0.dp)) {
                    group.items.forEachIndexed { i, item ->
                        if (i > 0) {
                            HorizontalDivider(color = LocalPyxColors.current.cardBorder)
                        }
                        FriendRow(
                            name = item.friend.name,
                            nickname = item.friend.nickname,
                            secondLine = secondLine(item)?.let { AnnotatedString(it) },
                            onClick = { onOpenFriend(item.friend.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResults(
    results: List<FriendSearch.FriendSearchResult>,
    query: String,
    onOpenFriend: (String) -> Unit,
) {
    if (results.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "没有找到“${query.trim()}”",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }
    val highlightColor = MaterialTheme.colorScheme.primary
    LazyColumn(
        contentPadding = PaddingValues(
            horizontal = PyxDimens.pageHorizontal, vertical = 12.dp
        )
    ) {
        item {
            PyxCard(contentPadding = PaddingValues(0.dp)) {
                results.forEachIndexed { i, result ->
                    if (i > 0) {
                        HorizontalDivider(color = LocalPyxColors.current.cardBorder)
                    }
                    val line = result.snippet?.let { snip ->
                        buildAnnotatedString {
                            append(snip.text)
                            addStyle(
                                SpanStyle(
                                    color = highlightColor,
                                    fontWeight = FontWeight.Bold,
                                ),
                                snip.highlight.first, snip.highlight.last + 1,
                            )
                        }
                    } ?: secondLine(result.item)?.let { AnnotatedString(it) }
                    FriendRow(
                        name = result.item.friend.name,
                        nickname = result.item.friend.nickname,
                        secondLine = line,
                        onClick = { onOpenFriend(result.item.friend.id) },
                    )
                }
            }
        }
    }
}

/** 朋友行：头像 + 名字（含昵称）+ 第二行摘要。 */
@Composable
private fun FriendRow(
    name: String,
    nickname: String?,
    secondLine: AnnotatedString?,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FriendAvatar(name = name, size = 40.dp)
        Spacer(Modifier.width(12.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (nickname != null) {
                    Text(
                        " $nickname",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (secondLine != null) {
                Text(
                    secondLine,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
