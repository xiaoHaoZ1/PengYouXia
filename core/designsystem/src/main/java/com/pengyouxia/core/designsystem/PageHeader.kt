package com.pengyouxia.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pengyouxia.core.designsystem.theme.LocalPyxColors

/**
 * 页头：与背景同色的平底，标题行固定 [PyxDimens.pageHeaderHeight] 高
 * （不含状态栏，内部 statusBarsPadding），标题垂直居中。
 * [onBack] 非空时左侧放返回箭头；[actions] 放标题右侧操作。
 * 右缘对齐见 [PyxDimens.headerActionEndPadding]。
 */
@Composable
fun PageHeader(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
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
                // 有返回箭头时箭头字形左缘落在 16dp 内容线上：
                // IconButton 内缩 12dp + ArrowBack 字形在 24dp 视口内 x≈4..20 → 行 start 0dp
                .padding(
                    start = if (onBack != null) 0.dp else PyxDimens.pageHorizontal,
                    end = PyxDimens.headerActionEndPadding,
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            }
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).semantics { heading() }
            )
            actions()
        }
    }
}

/**
 * 页头专用图标按钮：配合 [PyxDimens.headerActionEndPadding]=4dp，
 * 24dp 图标框右缘落在 16dp 内容线；Material 填充图标字形通常再内缩
 * （Add 字形 x≈5..19），这里 `offset(x = 5.dp)` 把字形右缘拉回 16dp。
 * 仅限页头 actions 内使用，页面其余位置的图标按钮不要用。
 */
@Composable
fun PageHeaderIconButton(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector,
            contentDescription = contentDescription,
            modifier = Modifier.offset(x = 5.dp),
        )
    }
}
