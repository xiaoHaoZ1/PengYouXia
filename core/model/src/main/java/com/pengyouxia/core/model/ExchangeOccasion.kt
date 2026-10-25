package com.pengyouxia.core.model

/**
 * 人情往来的场合。
 *
 * 枚举名会写进数据库与备份文件，以后不得改名（可增不可改）。
 */
enum class ExchangeOccasion(val displayName: String) {
    BIRTHDAY("生日"),
    WEDDING("结婚"),
    BABY("生孩子"),
    HOUSEWARMING("乔迁"),
    SICK_VISIT("探病"),
    FESTIVAL("过节"),
    OTHER("其他"),
}
