package com.pengyouxia.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 校验各枚举的声明顺序、显示名与唯一性。
 * 枚举名写进数据库与备份文件，本表即兼容契约，改动前先确认影响。
 */
class EnumsTest {

    @Test
    fun `memoCategory 顺序与显示名`() {
        assertEquals(
            listOf(
                "FAMILY" to "家人",
                "LIKES" to "喜好",
                "WANTS" to "想要",
                "RECENT" to "近况",
                "OTHER" to "其他",
            ),
            MemoCategory.entries.map { it.name to it.displayName },
        )
    }

    @Test
    fun `exchangeDirection 顺序与显示名`() {
        assertEquals(
            listOf("OUT" to "我给", "IN" to "他给"),
            ExchangeDirection.entries.map { it.name to it.displayName },
        )
    }

    @Test
    fun `exchangeKind 顺序与显示名`() {
        assertEquals(
            listOf("MONEY" to "钱", "GIFT" to "礼物", "MEAL" to "请吃饭"),
            ExchangeKind.entries.map { it.name to it.displayName },
        )
    }

    @Test
    fun `exchangeOccasion 顺序与显示名`() {
        assertEquals(
            listOf(
                "BIRTHDAY" to "生日",
                "WEDDING" to "结婚",
                "BABY" to "生孩子",
                "HOUSEWARMING" to "乔迁",
                "SICK_VISIT" to "探病",
                "FESTIVAL" to "过节",
                "OTHER" to "其他",
            ),
            ExchangeOccasion.entries.map { it.name to it.displayName },
        )
    }

    @Test
    fun `calendarType 顺序与显示名`() {
        assertEquals(
            listOf("SOLAR" to "公历", "LUNAR" to "农历"),
            CalendarType.entries.map { it.name to it.displayName },
        )
    }

    @Test
    fun `所有枚举内 name 与 displayName 唯一`() {
        val all: List<List<Pair<String, String>>> = listOf(
            MemoCategory.entries.map { it.name to it.displayName },
            ExchangeDirection.entries.map { it.name to it.displayName },
            ExchangeKind.entries.map { it.name to it.displayName },
            ExchangeOccasion.entries.map { it.name to it.displayName },
            CalendarType.entries.map { it.name to it.displayName },
        )
        for (entries in all) {
            assertEquals(entries.size, entries.map { it.first }.distinct().size)
            assertEquals(entries.size, entries.map { it.second }.distinct().size)
        }
    }
}
