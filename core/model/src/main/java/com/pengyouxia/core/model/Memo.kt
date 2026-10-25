package com.pengyouxia.core.model

/** 朋友备忘；[done] 仅对 [MemoCategory.WANTS] 有效（已送出）。 */
data class Memo(
    val id: String,
    val friendId: String,
    val category: MemoCategory,
    val content: String,
    val done: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)
