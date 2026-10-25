package com.pengyouxia.app.ui.mine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pengyouxia.app.data.FriendRepository
import com.pengyouxia.app.data.demo.DemoDataSeeder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * 我的页 ViewModel：目前只有「测试工具」的填充/清空。
 * 一次性事件发 Toast 文案。
 */
@HiltViewModel
class MineViewModel @Inject constructor(
    private val seeder: DemoDataSeeder,
    private val repository: FriendRepository,
) : ViewModel() {

    private val _toasts = Channel<String>(Channel.BUFFERED)
    val toasts = _toasts.receiveAsFlow()

    fun fillDemoData() {
        viewModelScope.launch {
            val count = seeder.seed()
            _toasts.send("已添加 $count 位演示朋友")
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearAll()
            _toasts.send("已清空全部数据")
        }
    }
}
