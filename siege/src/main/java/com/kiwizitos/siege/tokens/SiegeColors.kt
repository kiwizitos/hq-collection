package com.kiwizitos.siege.tokens

import androidx.compose.ui.graphics.Color

/**
 * Paleta de cores do Design System Siege.
 * Dark mode premium com 4 accent colours vibrantes.
 *
 * Uso semântico dos accents:
 *  - [AccentPink]  → UI primária (botões, bordas, destaques) · status **Lendo**
 *  - [AccentCyan]  → UI secundária (campos, ações)          · status **Tenho**
 *  - [AccentGreen] → Conclusão / conquista                  · status **Lido**
 *  - [AccentAmber] → Desejo / em fila                       · status **Quero**
 */
object SiegeColors {

    // ====== ACCENT COLORS (Principais do Design) ======

    /** Accent Pink/Magenta — botões primários, destaques, borders · Lendo */
    val AccentPink = Color(0xFFFF6B9D)

    /** Accent Cyan — botões secundários, campos, actions · Tenho */
    val AccentCyan = Color(0xFF00E5FF)

    /** Accent Green — conclusão, conquista · Lido */
    val AccentGreen = Color(0xFF69F0AE)

    /** Accent Amber — desejo, wishlist · Quero */
    val AccentAmber = Color(0xFFFFAB40)

    /** Variant mais escura do Pink para containers */
    val AccentPinkContainer = Color(0xFFFF4081)

    /** Variant mais escura do Cyan para containers */
    val AccentCyanContainer = Color(0xFF00B8D4)

    /** Variant mais escura do Green para containers */
    val AccentGreenContainer = Color(0xFF00C853)

    /** Variant mais escura do Amber para containers */
    val AccentAmberContainer = Color(0xFFFF8F00)

    // ====== BACKGROUNDS & SURFACES (Dark Mode Premium) ======
    /** Background principal - Preto profundo */
    val BackgroundDark = Color(0xFF121212)
    
    /** Surface para cards e painéis - Cinza escuro médio */
    val SurfaceDark = Color(0xFF1E1E1E)
    
    /** Surface variant para cards de destaque */
    val SurfaceVariantDark = Color(0xFF252525)
    
    /** Surface elevada para cards premium */
    val SurfaceElevatedDark = Color(0xFF2A2A2A)

    // ====== TEXT COLORS (Hierarquia tipográfica) ======
    /** Texto principal - Headlines e títulos (quase branco) */
    val TextPrimary = Color(0xFFF5F5F5)
    
    /** Texto secundário - Body text e descrições */
    val TextSecondary = Color(0xFFE0E0E0)
    
    /** Texto terciário - Labels, captions e status */
    val TextTertiary = Color(0xFFAAAAAA)
    
    /** Texto disabled */
    val TextDisabled = Color(0xFF888888)

    // ====== SEMANTIC COLORS ======
    /** Sucesso */
    val Success = Color(0xFF4CAF50)
    
    /** Atenção/Warning */
    val Warning = Color(0xFFFFC107)
    
    /** Erro */
    val Error = Color(0xFFFF5252)
    
    /** Info */
    val Info = Color(0xFF2196F3)

    // ====== DIVIDERS & BORDERS ======
    val Outline = Color(0xFF424242)
    val OutlineVariant = Color(0xFF303030)

    // ====== STATUS BADGES ======
    /** Badge "RARO" */
    val BadgeRare = Color(0xFFFFD700)
    
    /** Badge "POSSUÍDA" */
    val BadgePossessed = Color(0xFF00E676)
    
    /** Badge "LIDA" */
    val BadgeRead = Color(0xFF00B8D4)

    // ====== MATERIAL 3 MAPPING (Light Mode - Fallback) ======
    val Primary = AccentPink
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = AccentPinkContainer
    val OnPrimaryContainer = Color(0xFFFFFFFF)

    val Secondary = AccentCyan
    val OnSecondary = Color(0xFF000000)
    val SecondaryContainer = AccentCyanContainer
    val OnSecondaryContainer = Color(0xFFFFFFFF)

    val Tertiary = Color(0xFFBB86FC)
    val OnTertiary = Color(0xFF000000)
    val TertiaryContainer = Color(0xFF6200EE)
    val OnTertiaryContainer = Color(0xFFFFFFFF)

    val Background = Color(0xFFFFFBFE)
    val OnBackground = Color(0xFF1C1B1F)
    val Surface = Color(0xFFFFFBFE)
    val OnSurface = Color(0xFF1C1B1F)
    val SurfaceVariant = Color(0xFFE7E0EC)
    val OnSurfaceVariant = Color(0xFF49454E)
    val ErrorColor = Error
    val OnError = Color(0xFFFFFFFF)

    // ====== DARK THEME MAPPING ======
    val PrimaryDark = AccentPink
    val OnPrimaryDark = Color(0xFFFFFFFF)
    val PrimaryContainerDark = AccentPinkContainer
    val OnPrimaryContainerDark = Color(0xFFFFFFFF)

    val SecondaryDark = AccentCyan
    val OnSecondaryDark = Color(0xFF000000)
    val SecondaryContainerDark = AccentCyanContainer
    val OnSecondaryContainerDark = Color(0xFFFFFFFF)

    val TertiaryDark = Color(0xFFBB86FC)
    val OnTertiaryDark = Color(0xFF000000)
    val TertiaryContainerDark = Color(0xFF6200EE)
    val OnTertiaryContainerDark = Color(0xFFFFFFFF)

    val OnBackgroundDark = TextPrimary
    val OnSurfaceDark = TextPrimary
    val OnSurfaceVariantDark = TextSecondary
    val OutlineDark = Outline
}

