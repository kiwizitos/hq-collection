package com.kiwizitos.siege.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import com.kiwizitos.siege.tokens.SiegeShapes
import com.kiwizitos.siege.tokens.SiegeTypography

private val SiegeShapeScheme = Shapes(
    extraSmall = SiegeShapes.ExtraSmall,
    small = SiegeShapes.Small,
    medium = SiegeShapes.Medium,
    large = SiegeShapes.Large,
    extraLarge = SiegeShapes.ExtraLarge
)

/**
 * Tema central do Design System **Siege**.
 *
 * Envolva toda a UI com este composable para aplicar cores, tipografia e formas.
 * Acesse as cores sensíveis ao tema via [SiegeTheme.colors]:
 * ```
 * val colors = SiegeTheme.colors
 * Box(modifier = Modifier.background(colors.surface))
 * ```
 *
 * @param darkTheme Força o tema escuro. Por padrão segue a preferência do sistema.
 * @param content   Conteúdo da UI.
 */
@Composable
fun SiegeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SiegeDarkColorScheme else SiegeLightColorScheme
    val themeColors = if (darkTheme) SiegeDarkColors else SiegeLightColors

    CompositionLocalProvider(LocalSiegeColors provides themeColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SiegeTypography,
            shapes = SiegeShapeScheme,
            content = content
        )
    }
}

/**
 * Ponto de acesso estático às cores e tokens do tema ativo.
 *
 * Uso:
 * ```
 * SiegeTheme.colors.textPrimary
 * SiegeTheme.colors.surface
 * ```
 */
object SiegeTheme {
    val colors: SiegeThemeColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSiegeColors.current
}
