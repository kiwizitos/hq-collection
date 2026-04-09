package com.kiwizitos.siege.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Conjunto de cores sensíveis ao tema do Siege Design System.
 *
 * Acesse via [SiegeTheme.colors] dentro de qualquer composable envolto por [SiegeTheme]:
 * ```
 * val colors = SiegeTheme.colors
 * Text(color = colors.textPrimary)
 * Box(modifier = Modifier.background(colors.surface))
 * ```
 */
@Immutable
data class SiegeThemeColors(
    // ── Superfícies ───────────────────────────────────────────────────────────
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val surfaceElevated: Color,

    // ── Texto ─────────────────────────────────────────────────────────────────
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textDisabled: Color,

    // ── Contornos ─────────────────────────────────────────────────────────────
    val outline: Color,
    val outlineVariant: Color,
)

// ── Valores por tema ──────────────────────────────────────────────────────────

internal val SiegeLightColors = SiegeThemeColors(
    background     = Color(0xFFF5F5F5),
    surface        = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEEEEEE),
    surfaceElevated = Color(0xFFE8E8E8),

    textPrimary    = Color(0xFF1A1A1A),
    textSecondary  = Color(0xFF4A4A4A),
    textTertiary   = Color(0xFF7A7A7A),
    textDisabled   = Color(0xFFAAAAAA),

    outline        = Color(0xFFCCCCCC),
    outlineVariant = Color(0xFFE0E0E0),
)

internal val SiegeDarkColors = SiegeThemeColors(
    background     = Color(0xFF121212),
    surface        = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF252525),
    surfaceElevated = Color(0xFF2A2A2A),

    textPrimary    = Color(0xFFF5F5F5),
    textSecondary  = Color(0xFFE0E0E0),
    textTertiary   = Color(0xFFAAAAAA),
    textDisabled   = Color(0xFF888888),

    outline        = Color(0xFF424242),
    outlineVariant = Color(0xFF303030),
)

// ── CompositionLocal ──────────────────────────────────────────────────────────

internal val LocalSiegeColors = staticCompositionLocalOf { SiegeLightColors }

