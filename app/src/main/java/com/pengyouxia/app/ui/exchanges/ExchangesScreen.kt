package com.pengyouxia.app.ui.exchanges

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

/** 「人情」一级页：当前为无数据的空状态。 */
@Composable
fun ExchangesScreen() {
    val comingSoon = rememberComingSoon()
    Column(Modifier.fillMaxSize()) {
        PageHeader("人情") {
            IconButton(onClick = comingSoon) {
                Icon(Icons.Filled.Add, contentDescription = "记一笔")
            }
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyState(
                title = "还没有往来记录",
                message = "随礼、送礼物、请吃饭，都可以记在这里"
            )
        }
    }
}
