package com.pengyouxia.core.model

/** 各文本输入的长度上限，按 Unicode 码点数计（表情、生僻字算 1 个）。 */
object InputLimits {
    const val NAME = 30
    const val NICKNAME = 30
    const val MET_NOTE = 100
    const val MEMO = 500
}

/** 必填文本：仅去首尾空白（含全角空格，trim 覆盖 U+3000）。 */
fun normalizeRequired(raw: String): String = raw.trim()

/** 可选文本：trim 后为空视为未填写，存 null。 */
fun normalizeOptional(raw: String?): String? = raw?.trim()?.takeIf { it.isNotEmpty() }

/** 文本的 Unicode 码点数（代理对算 1）。 */
fun String.codePointLength(): Int = codePointCount(0, length)

/** [text] 的码点数是否超过 [max]。 */
fun exceedsLimit(text: String, max: Int): Boolean = text.codePointLength() > max
