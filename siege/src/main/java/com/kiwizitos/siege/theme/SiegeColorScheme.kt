package com.kiwizitos.siege.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import com.kiwizitos.siege.tokens.SiegeColors

/**
 * Color scheme para modo claro (fallback).
 * O foco principal do app é dark mode.
 */
internal val SiegeLightColorScheme = lightColorScheme(
    primary = SiegeColors.Primary,
    onPrimary = SiegeColors.OnPrimary,
    primaryContainer = SiegeColors.PrimaryContainer,
    onPrimaryContainer = SiegeColors.OnPrimaryContainer,
    secondary = SiegeColors.Secondary,
    onSecondary = SiegeColors.OnSecondary,
    secondaryContainer = SiegeColors.SecondaryContainer,
    onSecondaryContainer = SiegeColors.OnSecondaryContainer,
    tertiary = SiegeColors.Tertiary,
    onTertiary = SiegeColors.OnTertiary,
    tertiaryContainer = SiegeColors.TertiaryContainer,
    onTertiaryContainer = SiegeColors.OnTertiaryContainer,
    error = SiegeColors.ErrorColor,
    onError = SiegeColors.OnError,
    background = SiegeColors.Background,
    onBackground = SiegeColors.OnBackground,
    surface = SiegeColors.Surface,
    onSurface = SiegeColors.OnSurface,
    surfaceVariant = SiegeColors.SurfaceVariant,
    onSurfaceVariant = SiegeColors.OnSurfaceVariant,
    outline = SiegeColors.Outline
)

/**
 * Color scheme para modo escuro premium.
 * Tema principal do app "Minha Coleção de Séries".
 */
internal val SiegeDarkColorScheme = darkColorScheme(
    primary = SiegeColors.PrimaryDark,
    onPrimary = SiegeColors.OnPrimaryDark,
    primaryContainer = SiegeColors.PrimaryContainerDark,
    onPrimaryContainer = SiegeColors.OnPrimaryContainerDark,
    secondary = SiegeColors.SecondaryDark,
    onSecondary = SiegeColors.OnSecondaryDark,
    secondaryContainer = SiegeColors.SecondaryContainerDark,
    onSecondaryContainer = SiegeColors.OnSecondaryContainerDark,
    tertiary = SiegeColors.TertiaryDark,
    onTertiary = SiegeColors.OnTertiaryDark,
    tertiaryContainer = SiegeColors.TertiaryContainerDark,
    onTertiaryContainer = SiegeColors.OnTertiaryContainerDark,
    background = SiegeColors.BackgroundDark,
    onBackground = SiegeColors.OnBackgroundDark,
    surface = SiegeColors.SurfaceDark,
    onSurface = SiegeColors.OnSurfaceDark,
    surfaceVariant = SiegeColors.SurfaceVariantDark,
    onSurfaceVariant = SiegeColors.OnSurfaceVariantDark,
    outline = SiegeColors.OutlineDark
)

