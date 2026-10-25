package com.pengyouxia.app.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/** 返回一个弹「即将推出」Toast 的点击回调，供本批次的占位交互复用。 */
@Composable
internal fun rememberComingSoon(): () -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { Toast.makeText(context, "即将推出", Toast.LENGTH_SHORT).show() }
    }
}
