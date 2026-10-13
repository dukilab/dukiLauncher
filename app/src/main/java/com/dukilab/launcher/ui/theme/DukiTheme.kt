package com.dukilab.launcher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** テーマの選び方。設定画面で切り替える。 */
enum class ThemeMode { System, Light, Dark }

val LocalDukiColors = staticCompositionLocalOf { DukiColors.Light }
val LocalDukiTypography = staticCompositionLocalOf { DukiTypography() }

/** dukiLab. のテーマ。色・文字・形をまとめて流す。 */
@Composable
fun DukiTheme(
    mode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit,
) {
    val dark = when (mode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    val colors = if (dark) DukiColors.Dark else DukiColors.Light
    // Material 3 は土台（テキスト入力やメニュー）にだけ使う。色はdukiLab.のトークンに置き換える。
    val scheme = if (dark) {
        darkColorScheme(
            primary = colors.ink, onPrimary = colors.surface,
            background = colors.surface, onBackground = colors.ink,
            surface = colors.surfaceRaised, onSurface = colors.ink,
            surfaceVariant = colors.surfaceSunken, onSurfaceVariant = colors.inkMuted,
            outline = colors.lineStrong, outlineVariant = colors.line,
        )
    } else {
        lightColorScheme(
            primary = colors.ink, onPrimary = colors.surface,
            background = colors.surface, onBackground = colors.ink,
            surface = colors.surfaceRaised, onSurface = colors.ink,
            surfaceVariant = colors.surfaceSunken, onSurfaceVariant = colors.inkMuted,
            outline = colors.lineStrong, outlineVariant = colors.line,
        )
    }
    val square = RoundedCornerShape(0.dp)
    val selection = TextSelectionColors(handleColor = colors.ink, backgroundColor = colors.ink.copy(alpha = 0.25f))
    MaterialTheme(
        colorScheme = scheme,
        shapes = Shapes(square, square, square, square, square),
    ) {
        CompositionLocalProvider(
            LocalDukiColors provides colors,
            LocalDukiTypography provides DukiTypography(),
            LocalContentColor provides colors.ink,
            LocalTextSelectionColors provides selection,
            content = content,
        )
    }
}

/** `Duki.colors.ink` のように使う。 */
object Duki {
    val colors: DukiColors
        @Composable @ReadOnlyComposable get() = LocalDukiColors.current
    val type: DukiTypography
        @Composable @ReadOnlyComposable get() = LocalDukiTypography.current
}

