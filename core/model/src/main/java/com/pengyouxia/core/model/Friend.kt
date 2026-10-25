package com.pengyouxia.core.model

/** 朋友档案。 */
data class Friend(
    val id: String,
    val name: String,
    val nickname: String?,
    /** 怎么认识的（一句话）。 */
    val metNote: String?,
    val birthday: Birthday?,
    val createdAt: Long,
    val updatedAt: Long,
)
