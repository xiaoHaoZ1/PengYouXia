package com.pengyouxia.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** 浅色方案。供 [PyxTheme] 与对比度测试读取。 */
val PyxLightColorScheme = lightColorScheme(
    primary = PyxPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = PyxPrimaryContainerLight,
    onPrimaryContainer = PyxOnPrimaryContainerLight,
    // secondary 映射到 accentStrong：Material 组件默认用 secondary 时文字对比度也达标
    secondary = PyxAccentStrongLight,
    onSecondary = PyxOnAccentStrongLight,
    secondaryContainer = PyxAccentContainerLight,
    onSecondaryContainer = PyxOnAccentContainerLight,
    tertiary = PyxPrimaryLight,
    onTertiary = Color.White,
    tertiaryContainer = PyxPrimaryContainerLight,
    onTertiaryContainer = PyxOnPrimaryContainerLight,
    error = PyxErrorLight,
    onError = Color.White,
    errorContainer = PyxErrorContainerLight,
    onErrorContainer = PyxOnErrorContainerLight,
    background = PyxBackgroundLight,
    onBackground = PyxOnBackgroundLight,
    surface = PyxSurfaceContainerLight,
    onSurface = PyxOnBackgroundLight,
    surfaceContainerLowest = PyxSurfaceContainerLight,
    surfaceContainerLow = PyxSurfaceContainerLight,
    surfaceContainer = PyxSurfaceContainerLight,
    surfaceContainerHigh = PyxSurfaceContainerHighLight,
    surfaceContainerHighest = PyxSurfaceContainerHighestLight,
    surfaceVariant = PyxSurfaceContainerHighestLight,
    onSurfaceVariant = PyxOnSurfaceVariantLight,
    outline = PyxOutlineLight,
    outlineVariant = PyxOutlineVariantLight,
    surfaceTint = PyxPrimaryLight
)

/** 深色方案。供 [PyxTheme] 与对比度测试读取。 */
val PyxDarkColorScheme = darkColorScheme(
    primary = PyxPrimaryDark,
    onPrimary = PyxOnPrimaryDark,
    primaryContainer = PyxPrimaryContainerDark,
    onPrimaryContainer = PyxOnPrimaryContainerDark,
    secondary = PyxAccentStrongDark,
    onSecondary = PyxOnAccentStrongDark,
    secondaryContainer = PyxAccentContainerDark,
    onSecondaryContainer = PyxOnAccentContainerDark,
    tertiary = PyxPrimaryDark,
    onTertiary = PyxOnPrimaryDark,
    tertiaryContainer = PyxPrimaryContainerDark,
    onTertiaryContainer = PyxOnPrimaryContainerDark,
    error = PyxErrorDark,
    onError = PyxOnErrorDark,
    errorContainer = PyxErrorContainerDark,
    onErrorContainer = PyxOnErrorContainerDark,
    background = PyxBackgroundDark,
    onBackground = PyxOnBackgroundDark,
    surface = PyxSurfaceContainerDark,
    onSurface = PyxOnBackgroundDark,
    surfaceContainerLowest = PyxSurfaceContainerDark,
    surfaceContainerLow = PyxSurfaceContainerDark,
    surfaceContainer = PyxSurfaceContainerDark,
    surfaceContainerHigh = PyxSurfaceContainerHighDark,
    surfaceContainerHighest = PyxSurfaceContainerHighestDark,
    surfaceVariant = PyxSurfaceContainerHighestDark,
    onSurfaceVariant = PyxOnSurfaceVariantDark,
    outline = PyxOutlineDark,
    outlineVariant = PyxOutlineVariantDark,
    surfaceTint = PyxPrimaryDark
)

/** 圆角：10 / 14 / 14 / 20 四档。 */
private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(14.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

/** 品牌扩展色（随亮暗切换）。 */
data class PyxColors(
    /** 强调色（暖橙）：只做装饰（图标、描边、点缀），不得作为小字颜色或白字的底色。 */
    val accent: Color,
    /** 强调色高对比变体：用于文字与实心徽章底，对应 colorScheme.secondary。 */
    val accentStrong: Color,
    /** accentStrong 上的文字色，对应 colorScheme.onSecondary。 */
    val onAccentStrong: Color,
    /** 强调色浅底，对应 colorScheme.secondaryContainer。 */
    val accentSoft: Color,
    /** 人情「我给」用色（= accentStrong）。 */
    val give: Color,
    /** 人情「他给」用色（= primary）。 */
    val receive: Color,
    /** 页头底色（与背景同色）。 */
    val pageHeaderBackground: Color,
    /** 卡片与分隔线的发丝描边（= outlineVariant）。 */
    val cardBorder: Color,
)

val LightPyxColors = PyxColors(
    accent = PyxAccentLight,
    accentStrong = PyxAccentStrongLight,
    onAccentStrong = PyxOnAccentStrongLight,
    accentSoft = PyxAccentContainerLight,
    give = PyxAccentStrongLight,
    receive = PyxPrimaryLight,
    pageHeaderBackground = PyxBackgroundLight,
    cardBorder = PyxOutlineVariantLight,
)

val DarkPyxColors = PyxColors(
    accent = PyxAccentDark,
    accentStrong = PyxAccentStrongDark,
    onAccentStrong = PyxOnAccentStrongDark,
    accentSoft = PyxAccentContainerDark,
    give = PyxAccentStrongDark,
    receive = PyxPrimaryDark,
    pageHeaderBackground = PyxBackgroundDark,
    cardBorder = PyxOutlineVariantDark,
)

val LocalPyxColors = staticCompositionLocalOf { LightPyxColors }

/**
 * 朋友侠主题：青绿主色 + 暖橙强调的固定配色，跟随系统深浅色，不做动态取色。
 */
@Composable
fun PyxTheme(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalPyxColors provides if (dark) DarkPyxColors else LightPyxColors) {
        MaterialTheme(
            colorScheme = if (dark) PyxDarkColorScheme else PyxLightColorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
