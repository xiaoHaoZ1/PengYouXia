package com.pengyouxia.app.data

import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

/** 当前时间（epoch millis），可注入以便测试固定时间。 */
interface TimeSource {
    fun now(): Long
}

/**
 * 系统时间，进程内**严格递增**：同一进程连续写入的行时间戳可排序
 * （毫秒时钟在同一次 tick 内会平局，平局时用 id 兜底不稳定）。
 */
class SystemTimeSource @Inject constructor() : TimeSource {
    private val last = AtomicLong(0)

    override fun now(): Long = last.updateAndGet { prev ->
        maxOf(System.currentTimeMillis(), prev + 1)
    }
}
