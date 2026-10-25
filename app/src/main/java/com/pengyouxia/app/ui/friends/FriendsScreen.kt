package com.pengyouxia.app.ui.friends

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pengyouxia.app.ui.rememberComingSoon
import com.pengyouxia.core.designsystem.EmptyState
import com.pengyouxia.core.designsystem.PageHeader

/** 「朋友」一级页：当前为无数据的空状态。 */
@Composable
fun FriendsScreen() {
    val comingSoon = rememberComingSoon()
    Column(Modifier.fillMaxSize()) {
        PageHeader("朋友") {
            IconButton(onClick = comingSoon) {
                Icon(Icons.Filled.Add, contentDescription = "添加朋友")
            }
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyState(
                title = "还没有朋友",
                message = "记下朋友的生日、喜好和往来，下次见面不用再想",
                actionLabel = "添加第一位朋友",
                onAction = comingSoon
            )
        }
    }
}
