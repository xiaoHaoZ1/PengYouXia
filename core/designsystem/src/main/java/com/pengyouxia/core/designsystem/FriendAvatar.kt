package com.pengyouxia.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

/**
 * 朋友圆形头像：主色浅底 + 名字首字符。
 * 字号随 [size] 缩放（约 42%），用 dp 换算的 sp，不随系统字号二次放大。
 */
@Composable
fun FriendAvatar(name: String, size: Dp, modifier: Modifier = Modifier) {
    val fontSize: TextUnit = with(LocalDensity.current) { (size * 0.42f).toSp() }
    Box(
        modifier = modifier.size(size).clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            avatarInitial(name),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = fontSize,
            fontWeight = MaterialTheme.typography.titleMedium.fontWeight
        )
    }
}

/**
 * 取头像显示的首字符：trim 后取第一个 Unicode 码点。
 * 按码点取（非按 char），避免把代理对表情或生僻字截成半个乱码；
 * 带 ZWJ 的组合表情只取第一个码点，可接受。名字为空返回 "?"。
 */
fun avatarInitial(name: String): String {
    val t = name.trim()
    if (t.isEmpty()) return "?"
    return String(Character.toChars(t.codePointAt(0)))
}
