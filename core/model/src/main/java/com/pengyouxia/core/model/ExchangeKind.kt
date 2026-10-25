package com.pengyouxia.core.model

/**
 * 人情往来的形式。
 *
 * 枚举名会写进数据库与备份文件，以后不得改名（可增不可改）。
 */
enum class ExchangeKind(val displayName: String) {
    MONEY("钱"),
    GIFT("礼物"),
    MEAL("请吃饭"),
}
