package com.kiwizitos.siege.components.foundation

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.kiwizitos.siege.theme.SiegeTheme
import com.kiwizitos.siege.tokens.SiegeIcons

/**
 * Ícone padronizado do Siege Design System.
 *
 * Wrapper sobre [Icon] do Material 3 que carrega drawables próprios via [painterResource].
 * Sempre utilize [SiegeIcons] para referenciar o ícone correto:
 *
 * ```kotlin
 * // Ícone com cor padrão do tema
 * SiegeIcon(icon = SiegeIcons.ic_home)
 *
 * // Ícone com cor customizada
 * SiegeIcon(icon = SiegeIcons.ic_user_solid, tint = SiegeColors.AccentPink)
 *
 * // Com contentDescription para acessibilidade
 * SiegeIcon(icon = SiegeIcons.ic_explore, contentDescription = "Explorar")
 * ```
 *
 * @param icon               Drawable resource — use [SiegeIcons].
 * @param modifier           Modificador opcional.
 * @param tint               Cor do ícone. Por padrão herda a cor de conteúdo local do tema.
 * @param contentDescription Descrição para acessibilidade. Null indica ícone decorativo.
 */
@Composable
fun SiegeIcon(
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    contentDescription: String? = null,
) {
    Icon(
        painter = painterResource(id = icon),
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint,
    )
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun SiegeIconPreview() {
    SiegeTheme {
        SiegeIcon(icon = SiegeIcons.ic_home_solid)
    }
}
