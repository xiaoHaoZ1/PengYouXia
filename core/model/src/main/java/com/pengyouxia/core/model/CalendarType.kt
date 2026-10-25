package com.pengyouxia.core.model

/**
 * 生日的历法类型。
 *
 * 枚举名会写进数据库与备份文件，以后不得改名（可增不可改）。
 */
enum class CalendarType(val displayName: String) {
    SOLAR("公历"),
    LUNAR("农历"),
}
