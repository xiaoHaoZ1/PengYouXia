package com.pengyouxia.app.ui.friends.edit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pengyouxia.core.designsystem.PageHeader
import com.pengyouxia.core.designsystem.PyxDimens

/** 添加/编辑朋友页：三个字段 + 保存；编辑时底部有删除入口。 */
@Composable
fun FriendEditScreen(
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    onDeleted: () -> Unit,
    viewModel: FriendEditViewModel = hiltViewModel(),
) {
    val name by viewModel.name.collectAsStateWithLifecycle()
    val nickname by viewModel.nickname.collectAsStateWithLifecycle()
    val metNote by viewModel.metNote.collectAsStateWithLifecycle()
    val canSave by viewModel.canSave.collectAsStateWithLifecycle()
    val nameError by viewModel.nameError.collectAsStateWithLifecycle()
    val nicknameError by viewModel.nicknameError.collectAsStateWithLifecycle()
    val metNoteError by viewModel.metNoteError.collectAsStateWithLifecycle()
    val saveError by viewModel.saveError.collectAsStateWithLifecycle()
    val hasUnsaved by viewModel.hasUnsavedChanges.collectAsStateWithLifecycle()
    val deleteImpact by viewModel.deleteImpact.collectAsStateWithLifecycle()

    var showDiscard by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is EditEvent.Saved -> onSaved(event.id)
                EditEvent.Deleted -> onDeleted()
            }
        }
    }

    fun tryBack() {
        if (hasUnsaved) showDiscard = true else onBack()
    }
    BackHandler { tryBack() }

    Column(Modifier.fillMaxSize()) {
        PageHeader(
            title = if (viewModel.isNew) "添加朋友" else "编辑朋友",
            onBack = { tryBack() },
        ) {
            TextButton(onClick = viewModel::save, enabled = canSave) {
                Text("保存")
            }
        }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PyxDimens.pageHorizontal)
        ) {
            val focusRequester = remember { FocusRequester() }
            val keyboard = LocalSoftwareKeyboardController.current
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
                keyboard?.show()
            }
            Spacer(Modifier.height(8.dp))
            // TextFieldValue 适配层：编辑页载入已有值时光标停在末尾
            var nameField by remember { mutableStateOf(TextFieldValue("")) }
            LaunchedEffect(name) {
                if (nameField.text != name) {
                    nameField = TextFieldValue(name, TextRange(name.length))
                }
            }
            OutlinedTextField(
                value = nameField,
                onValueChange = {
                    nameField = it
                    viewModel.name.value = it.text
                },
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                label = { Text("名字") },
                isError = nameError != null,
                supportingText = { nameError?.let { Text(it) } },
                singleLine = true,
            )
            OutlinedTextField(
                value = nickname,
                onValueChange = { viewModel.nickname.value = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("昵称") },
                isError = nicknameError != null,
                supportingText = { nicknameError?.let { Text(it) } },
                singleLine = true,
            )
            OutlinedTextField(
                value = metNote,
                onValueChange = { viewModel.metNote.value = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("怎么认识的") },
                isError = metNoteError != null,
                supportingText = { metNoteError?.let { Text(it) } },
                singleLine = true,
            )
            saveError?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (!viewModel.isNew) {
                Spacer(Modifier.height(24.dp))
                TextButton(
                    onClick = { showDeleteConfirm = true },
                ) {
                    Text("删除这位朋友", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showDiscard) {
        AlertDialog(
            onDismissRequest = { showDiscard = false },
            title = { Text("放弃修改？") },
            confirmButton = {
                TextButton(onClick = { showDiscard = false; onBack() }) {
                    Text("放弃")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscard = false }) {
                    Text("继续编辑")
                }
            },
        )
    }

    if (showDeleteConfirm) {
        val impact = deleteImpact
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("删除这位朋友？") },
            text = {
                Text(deleteConfirmMessage(impact?.memoCount ?: 0, impact?.exchangeCount ?: 0))
            },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; viewModel.delete() }) {
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
