package com.pengyouxia.core.designsystem

import androidx.compose.ui.unit.dp

/** 全 App 统一的页面间距。 */
object PyxDimens {
    /** 页面内容与页头标题的水平边距。 */
    val pageHorizontal = 16.dp

    /** 页头标题行高度（不含状态栏）。 */
    val pageHeaderHeight = 64.dp

    /**
     * 页头右侧 actions 区到屏幕右缘的距离。
     * TextButton 文字右内边距 12dp → 文字右缘恰好落在 16dp 内容线；
     * 图标按钮请用 [PageHeaderIconButton]：24dp 图标框内缩 12dp 后
     * 框右缘在 16dp，再由组件内 offset 把字形右缘拉回 16dp。
     */
    val headerActionEndPadding = 4.dp
}
