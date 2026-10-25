package com.pengyouxia.core.model

/**
 * 生日：公历或农历的月日，年份可选。
 *
 * 只校验取值范围（月份 1–12、公历日 1–31、农历日 1–30、闰月只允许农历）；
 * 日期是否真实存在与农历换算不在此处（M2）。
 */
data class Birthday(
    val calendar: CalendarType,
    val year: Int?,
    val month: Int,
    val day: Int,
    val leapMonth: Boolean = false,
) {
    init {
        require(month in 1..12) { "month 必须在 1..12：$month" }
        val maxDay = if (calendar == CalendarType.SOLAR) 31 else 30
        require(day in 1..maxDay) {
            "${calendar.displayName}的 day 必须在 1..$maxDay：$day"
        }
        require(!leapMonth || calendar == CalendarType.LUNAR) { "leapMonth 只允许农历" }
    }
}
