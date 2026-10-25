package com.pengyouxia.app.screenshots

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.pengyouxia.app.R
import com.pengyouxia.core.designsystem.CountdownBadge
import com.pengyouxia.core.designsystem.DateTile
import com.pengyouxia.core.designsystem.EmptyState
import com.pengyouxia.core.designsystem.FriendAvatar
import com.pengyouxia.core.designsystem.PageHeader
import com.pengyouxia.core.designsystem.PyxCard
import com.pengyouxia.core.designsystem.PyxDimens
import com.pengyouxia.core.designsystem.SectionHeader
import com.pengyouxia.core.designsystem.theme.LocalPyxColors

/**
 * 组件样例页（仅测试源码）：把设计系统各组件排在一页上供截图审查。
 * 所有人名均为合成名字。
 */
@Composable
fun ComponentGallery() {
    val pyx = LocalPyxColors.current
    // Surface 提供 LocalContentColor=onBackground：样例页没有 Scaffold，
    // 不写死颜色的文字/图标需要它才能在深色下取到亮色
    Surface(
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
        PageHeader("页头示例") {
            IconButton(onClick = {}) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
        }
        Column(Modifier.padding(horizontal = PyxDimens.pageHorizontal)) {
            SectionHeader("卡片")
            PyxCard(Modifier.fillMaxWidth()) {
                Text("这是一张 PyxCard，白底、发丝描边、无阴影。")
            }

            SectionHeader("空状态")
            PyxCard(Modifier.fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                EmptyState(
                    title = "还没有朋友",
                    message = "记下朋友的生日、喜好和往来",
                    actionLabel = "添加第一位朋友",
                    onAction = {}
                )
            }

            SectionHeader("日期与倒计时")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DateTile(3, 12)
                DateTile(10, 25, highlight = true)
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CountdownBadge(0)
                CountdownBadge(1)
                CountdownBadge(12)
            }

            SectionHeader("头像")
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AvatarSample("王小明")
                AvatarSample("Alex")
                AvatarSample("😀小明")
                AvatarSample("")
            }

            SectionHeader("人情用色")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("我给 ¥800", style = MaterialTheme.typography.titleMedium, color = pyx.give)
                Spacer(Modifier.width(24.dp))
                Text("他给 ¥600", style = MaterialTheme.typography.titleMedium, color = pyx.receive)
                Spacer(Modifier.width(24.dp))
                // accent 只做装饰：色块示例，不用它写字
                Box(
                    Modifier
                        .size(40.dp, 16.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(pyx.accent)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "accent · 仅装饰",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SectionHeader("应用图标")
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(colorResource(R.color.ic_launcher_background)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
        }
        }
    }
}

/** 头像样例：FriendAvatar + 下方小字标注名字。 */
@Composable
private fun AvatarSample(name: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FriendAvatar(name = name, size = 40.dp)
        Spacer(Modifier.height(4.dp))
        Text(
            name.ifEmpty { "（空）" },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
