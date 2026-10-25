package com.pengyouxia.app.ui.frienddetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pengyouxia.core.designsystem.FriendAvatar
import com.pengyouxia.core.designsystem.PageHeader
import com.pengyouxia.core.designsystem.PyxCard
import com.pengyouxia.core.designsystem.PyxDimens
import com.pengyouxia.core.designsystem.SectionHeader
import com.pengyouxia.core.designsystem.theme.LocalPyxColors
import com.pengyouxia.core.model.MemoCategory
import java.time.ZoneId

/** 朋友详情页：头部卡 + 备忘分组 + 记备忘弹层。 */
@Composable
fun FriendDetailScreen(
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    viewModel: FriendDetailViewModel = hiltViewModel(),
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val editor by viewModel.editor.collectAsStateWithLifecycle()

    // 朋友被删除后直接返回列表
    LaunchedEffect(ui.gone) { if (ui.gone) onBack() }

    Column(Modifier.fillMaxSize()) {
        PageHeader(
            title = ui.friend?.name ?: "",
            onBack = onBack,
        ) {
            TextButton(onClick = { onEdit(viewModel.friendId) }) {
                Text("编辑")
            }
        }
        val friend = ui.friend
        if (friend != null) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = PyxDimens.pageHorizontal)
            ) {
                HeaderCard(friend.name, friend.nickname, friend.metNote)
                SectionHeader("备忘")
                MemoCard(ui, viewModel)
                Spacer(Modifier.height(12.dp))
                FilledTonalButton(onClick = viewModel::openNewMemo) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("记一条")
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (editor != null) {
        MemoEditorSheet(
            state = editor!!,
            onContentChange = viewModel::updateEditorContent,
            onCategoryChange = viewModel::updateEditorCategory,
            onSave = viewModel::saveMemo,
            onDelete = viewModel::deleteMemo,
            onDismiss = viewModel::dismissEditor,
        )
    }
}

@Composable
private fun HeaderCard(name: String, nickname: String?, metNote: String?) {
    PyxCard(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FriendAvatar(name = name, size = 56.dp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(name, style = MaterialTheme.typography.titleLarge)
                if (nickname != null) {
                    Text(
                        nickname,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (metNote != null) {
                    Text(metNote, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun MemoCard(ui: DetailUiState, viewModel: FriendDetailViewModel) {
    PyxCard(contentPadding = PaddingValues(0.dp)) {
        if (ui.memoGroups.isEmpty()) {
            Text(
                "还没有备忘，记下他的家人、喜好、想要的东西吧",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            ui.memoGroups.forEach { group ->
                Text(
                    group.category.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
                )
                group.memos.forEach { memo ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.openMemo(memo) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                memo.content,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (memo.done) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onSurface,
                                textDecoration = if (memo.done) TextDecoration.LineThrough else null,
                            )
                            Text(
                                formatMemoDate(memo.createdAt, ui.nowMillis, ZoneId.systemDefault()),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (memo.category == MemoCategory.WANTS) {
                            Checkbox(
                                checked = memo.done,
                                onCheckedChange = { viewModel.toggleDone(memo.id, it) },
                                modifier = Modifier.semantics { contentDescription = "已送出" }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemoEditorSheet(
    state: MemoEditorState,
    onContentChange: (String) -> Unit,
    onCategoryChange: (MemoCategory) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = PyxDimens.pageHorizontal)) {
            Text(
                if (state.editingMemoId == null) "记一条" else "编辑备忘",
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.height(12.dp))
            Row {
                MemoCategory.entries.forEach { category ->
                    FilterChip(
                        selected = state.category == category,
                        onClick = { onCategoryChange(category) },
                        label = { Text(category.displayName) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.content,
                onValueChange = onContentChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                isError = state.contentError != null,
                supportingText = { state.contentError?.let { Text(it) } },
            )
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (state.editingMemoId != null) {
                    TextButton(onClick = { showDeleteConfirm = true }) {
                        Text("删除", color = MaterialTheme.colorScheme.error)
                    }
                }
                Spacer(Modifier.weight(1f))
                Button(onClick = onSave, enabled = state.canSave) {
                    Text("保存")
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("删除这条备忘？") },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; onDelete() }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消")
                }
            },
        )
    }
}
