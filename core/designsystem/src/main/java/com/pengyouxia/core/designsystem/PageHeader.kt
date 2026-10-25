package com.pengyouxia.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.pengyouxia.core.designsystem.theme.LocalPyxColors

/**
 * 页头：与背景同色的平底，标题行固定 [PyxDimens.pageHeaderHeight] 高
 * （不含状态栏，内部 statusBarsPadding），标题垂直居中——
 * 有无 [actions] 标题位置完全一致。
 * [actions] 放标题右侧的操作（如「+」IconButton）。
 */
@Composable
fun PageHeader(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(LocalPyxColors.current.pageHeaderBackground)
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .height(PyxDimens.pageHeaderHeight)
                .padding(start = PyxDimens.pageHorizontal),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f).semantics { heading() }
            )
            // 图标右缘对齐 16dp 内容线：IconButton 内缩 12dp + Add 字形左右各留
            // 5dp（24dp 视口内路径占 x=5..19），12+5−1=16，故 actions 整体右移 1dp。
            Row(Modifier.offset(x = 1.dp)) { actions() }
        }
    }
}
