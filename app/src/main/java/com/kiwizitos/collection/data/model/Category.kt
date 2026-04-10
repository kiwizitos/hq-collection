package com.kiwizitos.collection.data.model

import androidx.compose.ui.graphics.Color
import com.kiwizitos.siege.tokens.SiegeColors
import kotlinx.serialization.Serializable

/**
 * Categoria de um item na galeria do usuário.
 *
 * Valores espelham o `category_enum` da tabela `user_items` no Supabase.
 */
@Serializable
enum class Category {
    TEM,
    LIDO,
    LENDO,
    QUERO;

    /** Rótulo exibido nos badges e chips da UI. */
    val displayLabel: String
        get() = when (this) {
            TEM   -> "Tenho"
            LIDO  -> "Lido"
            LENDO -> "Lendo"
            QUERO -> "Quero"
        }

    /** Cor de destaque associada a cada categoria, usando tokens do Siege. */
    val badgeColor: Color
        get() = when (this) {
            TEM   -> SiegeColors.AccentCyan
            LIDO  -> SiegeColors.AccentPink
            LENDO -> SiegeColors.AccentCyan
            QUERO -> SiegeColors.AccentPink
        }
}

