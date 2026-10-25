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

private val LightColors = lightColorScheme(
    primary = PyxPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = PyxPrimaryContainerLight,
    onPrimaryContainer = PyxOnPrimaryContainerLight,
    secondary = PyxAccentLight,
    onSecondary = Color.White,
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

private val DarkColors = darkColorScheme(
    primary = PyxPrimaryDark,
    onPrimary = PyxOnPrimaryDark,
    primaryContainer = PyxPrimaryContainerDark,
    onPrimaryContainer = PyxOnPrimaryContainerDark,
    secondary = PyxAccentDark,
    onSecondary = PyxOnAccentDark,
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
    /** 强调色（暖橙），对应 colorScheme.secondary。 */
    val accent: Color,
    /** 强调色上的文字色，对应 colorScheme.onSecondary。 */
    val onAccent: Color,
    /** 强调色浅底，对应 colorScheme.secondaryContainer。 */
    val accentSoft: Color,
    /** 人情「我给」用色（= accent）。 */
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
    onAccent = Color.White,
    accentSoft = PyxAccentContainerLight,
    give = PyxAccentLight,
    receive = PyxPrimaryLight,
    pageHeaderBackground = PyxBackgroundLight,
    cardBorder = PyxOutlineVariantLight,
)

val DarkPyxColors = PyxColors(
    accent = PyxAccentDark,
    onAccent = PyxOnAccentDark,
    accentSoft = PyxAccentContainerDark,
    give = PyxAccentDark,
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
            colorScheme = if (dark) DarkColors else LightColors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
