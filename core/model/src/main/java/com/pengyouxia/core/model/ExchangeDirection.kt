package com.pengyouxia.core.model

/**
 * 人情往来的方向：我给出或对方给我。
 *
 * 枚举名会写进数据库与备份文件，以后不得改名（可增不可改）。
 */
enum class ExchangeDirection(val displayName: String) {
    OUT("我给"),
    IN("他给"),
}
