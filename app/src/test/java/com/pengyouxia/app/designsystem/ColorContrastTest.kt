package com.pengyouxia.app.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import com.pengyouxia.core.designsystem.theme.PyxDarkColorScheme
import com.pengyouxia.core.designsystem.theme.DarkPyxColors
import com.pengyouxia.core.designsystem.theme.PyxLightColorScheme
import com.pengyouxia.core.designsystem.theme.LightPyxColors
import com.pengyouxia.core.designsystem.theme.PyxColors
import java.util.Locale
import kotlin.math.pow
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

/**
 * WCAG 2.x 对比度回归：所有「文字/底色」组合必须 ≥4.5。
 * 色值取 [PyxLightColorScheme]/[PyxDarkColorScheme] 与 [LightPyxColors]/[DarkPyxColors]，
 * 与运行时主题同一份定义。
 */
class ColorContrastTest {

    /** sRGB 分量 → 线性分量。 */
    private fun linear(c: Double) =
        if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)

    /** 相对亮度。 */
    private fun luminance(color: Color): Double {
        val r = linear(color.red.toDouble())
        val g = linear(color.green.toDouble())
        val b = linear(color.blue.toDouble())
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    /** 对比度比值（亮侧/暗侧）。 */
    private fun ratio(fg: Color, bg: Color): Double {
        val l1 = luminance(fg)
        val l2 = luminance(bg)
        val (hi, lo) = if (l1 >= l2) l1 to l2 else l2 to l1
        return (hi + 0.05) / (lo + 0.05)
    }

    /** 生成一套配色的全部受检组合（名字, 前景, 背景）。 */
    private fun combos(scheme: ColorScheme, pyx: PyxColors) = listOf(
        "onBackground on background" to (scheme.onBackground to scheme.background),
        "onSurface on surface" to (scheme.onSurface to scheme.surface),
        "onSurfaceVariant on background" to (scheme.onSurfaceVariant to scheme.background),
        "onSurfaceVariant on surface" to (scheme.onSurfaceVariant to scheme.surface),
        "onPrimary on primary" to (scheme.onPrimary to scheme.primary),
        "onPrimaryContainer on primaryContainer" to
            (scheme.onPrimaryContainer to scheme.primaryContainer),
        "give on background" to (pyx.give to scheme.background),
        "give on surface" to (pyx.give to scheme.surface),
        "receive on background" to (pyx.receive to scheme.background),
        "receive on surface" to (pyx.receive to scheme.surface),
        "onAccentStrong on accentStrong" to (pyx.onAccentStrong to pyx.accentStrong),
        "onSecondaryContainer on secondaryContainer" to
            (scheme.onSecondaryContainer to scheme.secondaryContainer),
        // secondary 已映射 accentStrong，Material 默认用 secondary 的场景一并校验
        "onSecondary on secondary" to (scheme.onSecondary to scheme.secondary),
    )

    @Test
    fun `所有文字底色组合对比度不低于 4-5`() {
        val failures = mutableListOf<String>()
        for ((schemeName, scheme, pyx) in listOf(
            Triple("浅色", PyxLightColorScheme, LightPyxColors),
            Triple("深色", PyxDarkColorScheme, DarkPyxColors),
        )) {
            for ((name, pair) in combos(scheme, pyx)) {
                val (fg, bg) = pair
                val r = ratio(fg, bg)
                println(
                    String.format(Locale.US, "%s\t%s\t%.2f", schemeName, name, r)
                )
                if (r < 4.5) {
                    failures.add(
                        String.format(Locale.US, "%s %s = %.2f", schemeName, name, r)
                    )
                }
            }
        }
        if (failures.isNotEmpty()) {
            fail("对比度不足 4.5 的组合：\n" + failures.joinToString("\n"))
        }
    }

    @Test
    fun `对比度计算自检`() {
        assertEquals(21.00, ratio(Color.White, Color.Black), 0.01)
        assertEquals(1.00, ratio(Color.White, Color.White), 0.01)
        // 返工前的旧 accent：验证它在浅底上确实不达标（≈2.91）
        assertEquals(2.91, ratio(Color.White, Color(0xFFE8792F)), 0.01)
    }
}
