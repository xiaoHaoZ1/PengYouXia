package com.pengyouxia.app.ui.mine

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.pengyouxia.app.BuildConfig
import com.pengyouxia.app.ui.rememberComingSoon
import com.pengyouxia.core.designsystem.PageHeader
import com.pengyouxia.core.designsystem.PyxDimens
import com.pengyouxia.core.designsystem.PyxCard
import com.pengyouxia.core.designsystem.SectionHeader
import com.pengyouxia.core.designsystem.theme.LocalPyxColors

/** 「我的」一级页：生日提醒、数据、关于三个分组；debug 下另有测试工具。 */
@Composable
fun MineScreen(
    viewModel: MineViewModel = hiltViewModel(),
) {
    val comingSoon = rememberComingSoon()
    val context = LocalContext.current
    var showClearConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.toasts.collect {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }
    Column(Modifier.fillMaxSize()) {
        PageHeader("我的")
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PyxDimens.pageHorizontal)
        ) {
            SectionHeader("生日提醒")
            PyxCard(contentPadding = PaddingValues(0.dp)) {
                MineRow("提醒设置", supporting = "提前几天、几点提醒", onClick = comingSoon)
            }

            SectionHeader("数据")
            PyxCard(contentPadding = PaddingValues(0.dp)) {
                MineRow("导出备份", supporting = "把全部资料存成一个文件", onClick = comingSoon)
                CardDivider()
                MineRow("从备份恢复", supporting = "用备份文件覆盖当前资料", onClick = comingSoon)
            }

            SectionHeader("关于")
            PyxCard(contentPadding = PaddingValues(0.dp)) {
                MineRow("版本", trailingText = BuildConfig.VERSION_NAME)
                CardDivider()
                Text(
                    "朋友侠不联网，资料只存在这台手机上。换手机前请导出备份。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }

            if (BuildConfig.DEBUG) {
                SectionHeader("测试工具")
                PyxCard(contentPadding = PaddingValues(0.dp)) {
                    MineRow(
                        "填充演示数据",
                        supporting = "追加 12 位合成朋友与备忘",
                        onClick = viewModel::fillDemoData
                    )
                    CardDivider()
                    MineRow(
                        "清空全部数据",
                        supporting = "删除全部朋友、备忘和往来，不清设置",
                        titleColor = MaterialTheme.colorScheme.error,
                        onClick = { showClearConfirm = true }
                    )
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("清空全部数据？") },
            text = { Text("将删除全部朋友、备忘和往来记录，无法恢复。设置不受影响。") },
            confirmButton = {
                TextButton(onClick = { showClearConfirm = false; viewModel.clearAll() }) {
                    Text("清空", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("取消")
                }
            },
        )
    }
}

/** 卡内发丝分隔线。 */
@Composable
private fun CardDivider() {
    HorizontalDivider(color = LocalPyxColors.current.cardBorder)
}

/**
 * 「我的」页列表行：标题 + 可选副文字，右侧可选 chevron 或静态文字。
 * [onClick] 为空时整行不可点（如「版本」行）。
 */
@Composable
private fun MineRow(
    title: String,
    supporting: String? = null,
    trailingText: String? = null,
    titleColor: androidx.compose.ui.graphics.Color =
        MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null
) {
    val rowModifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 56.dp)
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }
        .padding(16.dp)
    Row(rowModifier, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = titleColor)
            if (supporting != null) {
                Text(
                    supporting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (trailingText != null) {
            Text(
                trailingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (onClick != null) {
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

