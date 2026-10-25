package com.pengyouxia.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
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
 * 页头：与背景同色的平底，标题绘制在状态栏之下（内部 statusBarsPadding）。
 * [actions] 放标题右侧的操作（如「+」按钮）。
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
                .padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f).semantics { heading() }
            )
            actions()
        }
    }
}
