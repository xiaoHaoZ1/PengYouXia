package com.pengyouxia.core.model

/**
 * 朋友备忘的分类。
 *
 * 枚举声明顺序即界面展示顺序。
 * 枚举名会写进数据库与备份文件，以后不得改名（可增不可改）。
 */
enum class MemoCategory(val displayName: String) {
    FAMILY("家人"),
    LIKES("喜好"),
    WANTS("想要"),
    RECENT("近况"),
    OTHER("其他"),
}
